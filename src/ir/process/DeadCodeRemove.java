package ir.process;

import ir.instructions.Instruction;
import ir.instructions.terminator.Br;
import ir.instructions.terminator.Ret;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Module;


/**
 * @author zlp
 * @Discription 删除return和br语句后的不可达指令
 * @date 2024/12/10
 */
public class DeadCodeRemove {
    static Module module = Module.getInstance();

    public static void analyze() {
        for(Function function : module.getFunctionList()){
            if(!function.isLibFunc()){
                for(BasicBlock block : function.getBasicBlockList()){
                    delDeadCode(block);
                }
            }
        }
    }

    public static void delDeadCode(BasicBlock block){
        Boolean delFlag = false;
        for(Instruction instruction : block.getInstructions()){
            if(delFlag){
                instruction.dropOut(); // 删除该instruction使用的操作数的被使用信息
                instruction.delFromParent(); // 在基本块中删除该代码
            }
            if (instruction instanceof Ret || instruction instanceof Br) {
                delFlag = true;
            }
        }
    }
}
