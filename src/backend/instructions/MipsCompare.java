package backend.instructions;

import backend.operands.MipsOperand;

public class MipsCompare extends MipsInstruction{
    private MipsCondType cond;
    private MipsOperand dst = null;
    private MipsOperand op1 = null;
    private MipsOperand op2 = null;
    private void setOp1(MipsOperand op){
        addUseReg(this.op1, op);
        this.op1 = op;
    }
    private void setOp2(MipsOperand op){
        addUseReg(this.op2, op);
        this.op2 = op;
    }
    private void setDst(MipsOperand op){
        addDefReg(this.dst, op);
        this.dst = op;
    }

    @Override
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if(dst.equals(oldReg)){
            setDst(newReg);
        }
        if(op1.equals(oldReg)){
            setOp1(newReg);
        }
        if(op2.equals(oldReg)){
            setOp2(newReg);
        }
    }
    public MipsCompare(MipsCondType cond, MipsOperand dst, MipsOperand op1, MipsOperand op2){
        this.cond = cond;
        setDst(dst);
        setOp1(op1);
        setOp2(op2);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("s"+cond+" " + dst + ", " + op1 + ", " + op2 + "\n");
        return stringBuilder.toString();
    }
}
