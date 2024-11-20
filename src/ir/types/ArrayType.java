package ir.types;


public class ArrayType extends ValueType {
    // int | char
    private ValueType valueType;
    private final int arrayLen;
    private final int size;
    public ArrayType(ValueType valueType, int arrayLen){
        this.valueType = valueType;
        this.arrayLen = arrayLen;
        size = valueType.getSize()*arrayLen;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public int getArrayLen() {
        return arrayLen;
    }

    public int getSize() {
        return size;
    }

    @Override
    public String toString() {
        return "[" + arrayLen + " x " + valueType + "]";
    }
}
