package backend.instructions;

import backend.operands.MipsOperand;

public class MipsMoveHI extends MipsInstruction{
    public enum MoveHIType{
        /**
         * Move From HI
         * 将 HI 寄存器中的值移动到一个通用寄存器中
         */
        MFHI("mfhi"),
        /**
         * Move To HI
         * 将一个通用寄存器中的值移动到 HI 寄存器中
         */
        MTHI("mthi");

        String name;
        MoveHIType(String name){
            this.name = name;
        }
    }

    private MoveHIType type = null;
    private MipsOperand dst = null;
    private MipsOperand src = null;

    public void setDst(MipsOperand dst) {
        addDefReg(this.dst, dst);
        this.dst = dst;
    }
    public void setSrc(MipsOperand src) {
        addUseReg(this.src, src);
        this.src = src;
    }

    @Override
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if(dst.equals(oldReg)){
            setDst(newReg);
        }
        if(src.equals(oldReg)){
            setSrc(newReg);
        }
    }

    public MipsMoveHI(MoveHIType type, MipsOperand op){
        this.type = type;
        if(type == MoveHIType.MFHI){
            setDst(op);
        }else {
            setSrc(op);
        }
    }

    @Override
    public String toString() {
        MipsOperand op = dst != null ? dst:src;
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(type).append(" ").append(op).append("\n");
        return stringBuilder.toString();
    }
}
