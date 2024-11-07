package ir.types.constants;

import ir.types.CharType;

public class ConstChar extends Constant{
    public static final ConstChar ZERO = new ConstChar(0);
    private int value;
    public ConstChar(int value){
        super(new CharType());
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
