package backend.operands;

import java.util.Objects;

/**
 * @author zlp
 * @Discription 虚拟寄存器
 * @date 2024/11/19
 */
public class MipsVirReg extends MipsOperand{
    private String name;
    private static int nameCnt = 0;
    private int getNameCnt(){
        return nameCnt++;
    }
    public MipsVirReg(){
        name = "vr" + getNameCnt();
    }

    @Override
    public boolean needColoring() { // 虚拟寄存器都需要着色
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){
            return false;
        }
        return Objects.equals(name,((MipsVirReg) obj).name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
