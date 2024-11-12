package ir;

import ir.types.PointerType;
import ir.types.ValueType;
import ir.types.constants.Constant;

import java.util.ArrayList;
import java.util.List;

public class GlobalVariable extends User{
    // 是否为常数
    private boolean isConst;
    // 初始化
    private Constant initValue = null;
    // 定义全局变量，常量一定有初始化值，变量如果没有初始化值，则初始化为0
    public GlobalVariable(String name, boolean isConst, Constant initValue){
        super(new PointerType(initValue.getValueType()),"@"+name,Module.getInstance(),new ArrayList<Value>(){{
            add(initValue);
        }});
        this.isConst = isConst;
        this.initValue = initValue;
    }

    public Constant getInitValue(){
        return initValue;
    }
    // @a = dso_local global i32 97
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName());// @a
        stringBuilder.append(" = dso_local ");
        stringBuilder.append((isConst) ? "constant " : "global ");
        stringBuilder.append(((PointerType)getValueType()).getPointingType());
        stringBuilder.append(" ");
        stringBuilder.append(getOperands().get(0));
        return stringBuilder.toString();
    }
}
