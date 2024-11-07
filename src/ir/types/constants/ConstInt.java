package ir.types.constants;

import ir.types.IntType;

public class ConstInt extends Constant{
    public static final ConstInt ZERO = new ConstInt(0);
    private int bits;
    private int value;

    public int getValue() {
        return value;
    }
    public ConstInt(int bits,int value){
        super(new IntType(bits));
        this.bits = bits;
        this.value = value;
    }
    public ConstInt(int value){
        super(new IntType(32));
        this.bits = 32;
        this.value = value;
    }

    @Override
    public String toString() {
        return Integer.toString(value);
    }
}
