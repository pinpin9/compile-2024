package backend.instructions;

import backend.operands.MipsOperand;
import backend.values.MipsBasicBlock;

// j指令, b指令
public class MipsBranch extends MipsInstruction{
    private MipsCondType cond = null;
    private MipsBasicBlock target = null;
    private MipsOperand op1 = null;
    private MipsOperand op2 = null;

    public MipsCondType getCond() {
        return cond;
    }
    public void setOp1(MipsOperand op1) {
        addUseReg(this.op1, op1);
        this.op1 = op1;
    }
    public void setOp2(MipsOperand op2) {
        addUseReg(this.op2, op2);
        this.op2 = op2;
    }
    @Override
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if(op1.equals(oldReg)){
            setOp1(newReg);
        }
        if(op2!=null&&op2.equals(oldReg)){
            setOp2(newReg);
        }
    }

    // j 无条件跳转
    public MipsBranch(MipsBasicBlock target){
        this.target = target;
    }

    // 根据两个寄存器值比较结果, 进行跳转
    public MipsBranch(MipsBasicBlock target, MipsCondType cond, MipsOperand op1, MipsOperand op2){
        this.cond = cond;
        this.target = target;
        setOp1(op1);
        setOp2(op2);
    }

    // 根据与 0 的比较结果, 进行跳转
    public MipsBranch(MipsBasicBlock target, MipsCondType cond, MipsOperand op){
        this.cond = cond;
        this.target = target;
        setOp1(op);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if(cond == null){ // 无条件跳转
            stringBuilder.append("j " + target.getName() + "\n");
        } else if (op2 == null) { // 与 0 比较的跳转语句
            stringBuilder.append("b" + cond + "z " + op1 + ", " + target.getName()+"\n");
        } else {
            stringBuilder.append("b" + cond +" "+ op1 + ", " + op2 + ", " + target.getName()+"\n");
        }
        return stringBuilder.toString();
    }
}
