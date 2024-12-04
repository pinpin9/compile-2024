package backend.instructions;

import backend.operands.MipsOperand;

/**
 * @author zlp
 * @Discription 位移
 * sll, srl, sra
 * @date 2024/11/21
 */
public class MipsShift extends MipsInstruction{
    public enum ShiftType{
        SLL,
        SRL,
        SRA
    }
    private ShiftType shiftType = null;
    private MipsOperand dst = null;
    private MipsOperand src = null; // 操作数
    private MipsOperand offset = null; // 偏移量，常数
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

    public MipsShift(ShiftType shiftType, MipsOperand dst, MipsOperand src, MipsOperand offset){
        this.shiftType = shiftType;
        setDst(dst);
        setSrc(src);
        this.offset = offset;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String typeStr;
        switch (shiftType){
            case SLL -> typeStr = "sll";
            case SRA -> typeStr = "sra";
            case SRL -> typeStr = "srl";
            default -> typeStr = null;
        }
        stringBuilder.append(typeStr).append(" ").append(dst).append(", ").append(src).append(", ").append(offset).append("\n");
        return stringBuilder.toString();
    }
}
