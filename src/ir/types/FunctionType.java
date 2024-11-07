package ir.types;

import ir.Value;

import java.util.ArrayList;
import java.util.List;

public class FunctionType extends ValueType{
    private List<ValueType> params = new ArrayList<>();
    private ValueType rtnType;
    public FunctionType(List<ValueType> params, ValueType rtnType){
        this.params = params;
        this.rtnType = rtnType;
    }
    public int getSize(){
        System.out.println("非法获取函数大小");
        return 0;
    }
}
