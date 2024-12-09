package ir.types.constants;

import ir.types.ArrayType;
import ir.types.CharType;
import ir.types.ValueType;

import static tools.StrTool.getLen;

public class ConstStr extends Constant{
    private String string;

    public String getString(){
        return string;
    }

    public ConstStr(String string) {
        super(new ArrayType(new CharType(), getLen(string)+1)); // 因为末尾有\0，所以len+1
        this.string = string; // 替换\n
    }

    @Override
    public String toString() {
        return "c"+"\""+string+"\\00\"";
    }
}
