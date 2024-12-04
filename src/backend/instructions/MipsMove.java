package backend.instructions;

import backend.operands.MipsImme;
import backend.operands.MipsLabel;
import backend.operands.MipsOperand;

/**
 * @author zlp
 * @Discription li | la | move
 * li : 加载立即数
 * la : 将地址或者Label加载到寄存器中
 * move : 复制一个寄存器到另一个寄存器中
 * @date 2024/11/21
 */
public class MipsMove extends MipsInstruction{
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
    public MipsMove(MipsOperand dst, MipsOperand src){
        setDst(dst);
        setSrc(src);
    }

    public MipsOperand getDst(){
        return dst;
    }

    public MipsOperand getSrc(){
        return src;
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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if(src instanceof MipsImme){
            stringBuilder.append("li ").append(dst).append(", ").append(src).append("\n");
        } else if (src instanceof MipsLabel) {
            stringBuilder.append("la ").append(dst).append(", ").append(src).append("\n");
        } else {
            stringBuilder.append("move ").append(dst).append(", ").append(src).append("\n");
        }
        return stringBuilder.toString();
    }
}
