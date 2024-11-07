package ir.types.constants;

import ir.types.ArrayType;
import ir.types.CharType;
import ir.types.ValueType;

import static tools.StrTool.getFormat;

public class ConstStr extends Constant{
    private String string;
    public ConstStr(String string) {
        super(new ArrayType(new CharType(), getFormat(string).length()+1)); //因为末尾有\0，所以len+1
        this.string = string;
    }



    @Override
    public String toString() {
        return " c"+"\""+string+"\\00\"";
    }
}
