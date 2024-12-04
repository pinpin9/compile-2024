package backend.operands;

import java.util.Objects;

/**
 * @author zlp
 * @Discription 物理寄存器
 * @date 2024/11/19
 */
public class MipsPhyReg extends MipsOperand{
    private RegType regType;
    private boolean isAllocated;

    public MipsPhyReg(String name){
        this.regType = RegType.getRegType(name);
        this.isAllocated = false;
    }
    public MipsPhyReg(int index){
        this.regType = RegType.getRegType(index);
        this.isAllocated = false;
    }
    public MipsPhyReg(String name, boolean isAllocated){
        this.regType = RegType.getRegType(name);
        this.isAllocated = isAllocated;
    }
    public MipsPhyReg(int index, boolean isAllocated){
        this.regType = RegType.getRegType(index);
        this.isAllocated = isAllocated;
    }
    public MipsPhyReg(RegType regType, boolean isAllocated){
        this.regType = regType;
        this.isAllocated = isAllocated;
    }


    public static final MipsPhyReg ZERO = new MipsPhyReg("zero");
    public static final MipsPhyReg AT = new MipsPhyReg("at");
    public static final MipsPhyReg V0 = new MipsPhyReg("v0");
    public static final MipsPhyReg SP = new MipsPhyReg("sp");
    public static final MipsPhyReg RA = new MipsPhyReg("ra");


    // 获取物理寄存器的相关信息
    public int getIndex(){
        return regType.getIndex();
    }
    public String getName(){
        return regType.getName();
    }
    public RegType getRegType() {
        return regType;
    }

    @Override
    public boolean isPreColored() {
        return !isAllocated;
    }

    @Override
    public boolean needColoring() { // 如果还没被分配，则需要着色
        return !isAllocated;
    }

    @Override
    public boolean isAllocated() {
        return isAllocated;
    }

    public void setAllocated(Boolean isAllocated){
        this.isAllocated = isAllocated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MipsPhyReg reg = (MipsPhyReg) o;
        return regType == reg.regType && isAllocated == reg.isAllocated;
    }
    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), isAllocated);
    }

    @Override
    public String toString() {
        return regType.toString();
    }
}
