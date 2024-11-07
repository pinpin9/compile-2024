package ir.types;

/**
 * @author zlp
 * @Discription int类型
 * @date 2024/10/30
 */
public class IntType extends ValueType{
    private final int bits; // bit数
    public IntType(int bits){
        this.bits = bits;
    }

    @Override
    public int getSize() { // 返回字节数
        return bits/8;
    }

    @Override
    public String toString() {
        return "i"+bits;
    }
    public boolean isI1(){
        if(bits==1){
            return true;
        }
        return false;
    }
}
