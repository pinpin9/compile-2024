package ir.types;

public class CharType extends ValueType{
    private IntType i8 = new IntType(8);
    public int getSize(){
        return i8.getSize();
    }

    @Override
    public String toString() {
        return i8.toString();
    }

    @Override
    public boolean isChar() {
        return true;
    }

}
