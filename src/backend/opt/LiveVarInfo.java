package backend.opt;

import backend.instructions.MipsInstruction;
import backend.operands.MipsOperand;
import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author zlp
 * @Discription 活跃变量分析
 * @date 2024/11/28
 */
public class LiveVarInfo {
    private HashSet<MipsOperand> liveUse = new HashSet<>();
    private HashSet<MipsOperand> liveDef = new HashSet<>();
    private HashSet<MipsOperand> liveIn = new HashSet<>();
    private HashSet<MipsOperand> liveOut = new HashSet<>();

    /**
     * 对每个函数进行活跃变量分析
     * @return
     */
    public static HashMap<MipsBasicBlock, LiveVarInfo> liveAnalyze(MipsFunction mipsFunction){
        HashMap<MipsBasicBlock, LiveVarInfo> liveInfoMap = new HashMap<>();
        for(MipsBasicBlock block : mipsFunction.getMipsBasicBlocks()){
            LiveVarInfo liveVarInfo = new LiveVarInfo();
            liveInfoMap.put(block, liveVarInfo);
            for(MipsInstruction instruction : block.getInstructions()){
                for(MipsOperand useReg : instruction.getUseRegs()){
                    if(useReg.needColoring() && !liveVarInfo.liveDef.contains(useReg)){
                        liveVarInfo.liveUse.add(useReg);
                    }
                }
                for(MipsOperand defReg : instruction.getDefRegs()){
                    if(defReg.needColoring()){
                        liveVarInfo.liveDef.add(defReg);
                    }
                }
                liveVarInfo.liveIn.addAll(liveVarInfo.liveUse);
            }
        }

        Boolean isChanged = true;
        while (isChanged){
            isChanged = false;
            // 修改liveOut中的值
            for(MipsBasicBlock mipsBasicBlock : mipsFunction.getMipsBasicBlocks()){
                LiveVarInfo liveVarInfo = liveInfoMap.get(mipsBasicBlock);

                HashSet<MipsOperand> newLiveOut = new HashSet<>();
                if(mipsBasicBlock.getTrueBlock() != null){
                    LiveVarInfo sucBlockLiveVarInfo = liveInfoMap.get(mipsBasicBlock.getTrueBlock());
                    newLiveOut.addAll(sucBlockLiveVarInfo.liveIn);
                }

                if(mipsBasicBlock.getFalseBlock() != null){
                    LiveVarInfo sucBlockLiveVarInfo = liveInfoMap.get(mipsBasicBlock.getFalseBlock());
                    newLiveOut.addAll(sucBlockLiveVarInfo.liveIn);
                }

                if(!liveVarInfo.liveOut.equals(newLiveOut)){
                    isChanged = true;
                    liveVarInfo.liveOut = newLiveOut;
                    // liveIn = liveUse + liveOut - liveDef
                    liveVarInfo.liveIn = new HashSet<>(liveVarInfo.liveUse);
                    for(MipsOperand operand : liveVarInfo.liveOut){
                        if(!liveVarInfo.liveDef.contains(operand)){
                            liveVarInfo.liveIn.add(operand);
                        }
                    }
                }
            }
        }
        return liveInfoMap;
    }

    public HashSet<MipsOperand> getLiveOut(){
        return liveOut;
    }
}
