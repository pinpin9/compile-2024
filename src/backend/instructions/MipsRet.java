package backend.instructions;

import backend.operands.RegType;
import backend.values.MipsFunction;

// jr ra
public class MipsRet extends MipsInstruction{
    private MipsFunction curFunction = null;
    public MipsRet(MipsFunction curFunction){
        this.curFunction = curFunction;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        int totalSize = curFunction.getStackTotalSize();
        // 将栈指针复位
        stringBuilder.append("addiu $sp,  $sp, ").append(totalSize).append("\n");
        // 如果是主函数，则直接结束运行
        if(curFunction.getName().equals("main")){
            stringBuilder.append("\tli $v0 10\n");
            stringBuilder.append("\tsyscall\n\n");
        } else { // 非主函数，需要恢复现场，恢复寄存器
            int offset = 0;
            for(RegType regType : curFunction.getRegsNeedSave()){
                offset -= 4;
                stringBuilder.append("\t").append("lw ").append(regType).append(", ").append(offset).append("($sp)\n");
            }
            stringBuilder.append("\tjr $ra\n");
        }

        return stringBuilder.toString();
    }
}
