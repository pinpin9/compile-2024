package backend.opt;

import backend.instructions.MipsBinary;
import backend.instructions.MipsInstruction;
import backend.instructions.MipsMove;
import backend.operands.MipsImme;
import backend.operands.MipsOperand;
import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;
import backend.values.MipsModule;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zlp
 * @Discription 窥孔优化, 删除部分冗余指令
 * 对每个基本块中的指令进行优化
 * @date 2024/12/10
 */
public class Peephole {
    // 是否还可以进行优化
    private static boolean isOpt = true;
    public static void process(){
        MipsModule mipsModule = MipsModule.getModule();
        while(isOpt){
            isOpt = false;
            // 如果进行了优化，则将isOpt改为true
            for(MipsFunction function: mipsModule.getMipsFunctions()){
                for(MipsBasicBlock block : function.getMipsBasicBlocks()){
                    optCal(block);

                }
            }
        }
    }

    /**
     * 进行计算的优化
     * 如果为 addiu $t0, $t0, 0 -> del
     * addiu $t0, $t1, 0 -> move $t0, $t1
     */
    private static void optCal(MipsBasicBlock block){
        LinkedList<MipsInstruction> newInstructions = new LinkedList<>();

        for(MipsInstruction instruction : block.getInstructions()){
            Boolean tmpOpt = false;
            if(instruction instanceof MipsBinary){ // 二元指令
                MipsBinary.BinaryType type = ((MipsBinary)instruction).getType();
                if(type == MipsBinary.BinaryType.ADDU || type == MipsBinary.BinaryType.SUBU){
                    MipsOperand dst = ((MipsBinary)instruction).getDst();
                    MipsOperand src1 = ((MipsBinary)instruction).getSrc1();
                    MipsOperand src2 = ((MipsBinary)instruction).getSrc2();
                    if(src2 instanceof MipsImme && ((MipsImme)src2).getValue() == 0){
                        tmpOpt = true;
                        if(!dst.equals(src1)){
                            MipsMove move = new MipsMove(dst, src1);
                            newInstructions.add(move);
                        }
                    }
                }
            }
            if(!tmpOpt){
                newInstructions.add(instruction);
            }else {
                isOpt = true;
            }
        }
        block.setInstructions(newInstructions);
    }

}
