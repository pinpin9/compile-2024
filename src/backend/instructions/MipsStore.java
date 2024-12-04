package backend.instructions;


import backend.operands.MipsOperand;

/**
 * @author zlp
 * @Discription sw | sb 方法
 * sw rt, offset(src)
 * @date 2024/11/21
 */
public class MipsStore extends MipsInstruction{
    private MipsOperand addr = null;
    private MipsOperand src = null;
    private MipsOperand offset = null;
    public void setOp1(MipsOperand op){
        addUseReg(this.src, op);
        this.src = op;
    }
    public void setOp2(MipsOperand op){
        addUseReg(this.addr, op);
        this.addr = op;
    }

    public void setOffset(MipsOperand op){
        this.offset = op;
    }

    @Override
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if(src.equals(oldReg)){
            setOp1(newReg);
        }
        if(addr.equals(oldReg)){
            setOp2(newReg);
        }
    }
    public MipsStore(MipsOperand src, MipsOperand addr, MipsOperand offset){
        setOp1(src);
        setOp2(addr);
        this.offset = offset;
    }

    @Override
    public String toString() {
//        System.out.println(getUseRegs());
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("sw ").append(src).append(", ").append(offset).append("("+ addr +")\n");
        return stringBuilder.toString();
    }
}
