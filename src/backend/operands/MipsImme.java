package backend.operands;

/**
 * @author zlp
 * @Discription 立即数类型
 * @date 2024/11/19
 */
public class MipsImme extends MipsOperand{
    private int value;
    public MipsImme(int value){
        this.value = value;
    }
    public void setValue(int value) {
        this.value = value;
    }
    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
