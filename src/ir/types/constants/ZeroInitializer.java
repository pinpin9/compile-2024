package ir.types.constants;

import ir.types.ArrayType;
import ir.types.ValueType;

public class ZeroInitializer extends Constant{
    public int len;
    public ZeroInitializer(ArrayType arrayType) {
        super(arrayType);
        len = arrayType.getArrayLen();
    }

    public int getLen() {
        return len;
    }

    @Override
    public String toString() {
        return "zeroinitializer";
    }
}
