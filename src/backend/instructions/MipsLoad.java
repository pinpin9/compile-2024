package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @author zlp
 * @Discription lw | lb方法
 * lw $v1, 8($s0)
 * @date 2024/11/21
 */
public class MipsLoad extends MipsInstruction{
    private MipsOperand dst = null;
    private MipsOperand base = null;
    private MipsOperand offset = null;
    public void setOp1(MipsOperand op){
        addUseReg(this.base, op);
        this.base = op;
    }
    public void setOp2(MipsOperand op){
        addUseReg(this.offset, op);
        this.offset = op;
    }
    public void setDst(MipsOperand op){
        addDefReg(this.dst, op);
        this.dst = op;
    }

    @Override
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if(dst.equals(oldReg)){
            setDst(newReg);
        }
        if(base.equals(oldReg)){
            setOp1(newReg);
        }
        if(offset.equals(oldReg)){
            setOp2(newReg);
        }
    }

    public MipsLoad(MipsOperand dst, MipsOperand base, MipsOperand offset){
        setDst(dst);
        setOp1(base);
        setOp2(offset);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("lw ").append(dst).append(", ").append(offset).append("("+base+")\n");
        return stringBuilder.toString();
    }
}
