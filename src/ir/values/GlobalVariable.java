package ir.values;

import backend.MipsBuilder;
import backend.values.MipsGlobalVariable;
import ir.types.ArrayType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.constants.*;

import java.util.ArrayList;
import java.util.List;

public class GlobalVariable extends User {
    // 是否为常数
    private boolean isConst;
    // 初始化
    private Constant initValue;
    // 定义全局变量，常量一定有初始化值，变量如果没有初始化值，则初始化为0
    public GlobalVariable(String name, boolean isConst, Constant initValue){
        super(new PointerType(initValue.getValueType()),"@"+name,Module.getInstance(), new ArrayList<>() {{
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
        stringBuilder.append(getName()); // @a
        stringBuilder.append(" = dso_local ");
        stringBuilder.append((isConst) ? "constant " : "global ");
        stringBuilder.append(((PointerType)getValueType()).getPointingType());
        stringBuilder.append(" ");
        stringBuilder.append(getOperands().get(0));
        return stringBuilder.toString();
    }



    //==========目标代码生成==========
    MipsBuilder mipsBuilder = MipsBuilder.getInstance();
    public void buildMips(){
        // 只需要生成global
        if(initValue instanceof ConstStr){ // 字符串
            mipsBuilder.buildGlobalVariable(getName(), ((ConstStr)initValue).getString().replace("\\0A", "\\n"));
        } else if (initValue instanceof ZeroInitializer) { // 没有初始化
            mipsBuilder.buildGlobalVariable(getName(), initValue.getValueType().getSize());
        } else if(initValue instanceof ConstInt){

            mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, new ArrayList<>(){{
                add(((ConstInt) initValue).getValue());
            }});
        } else if (initValue instanceof ConstChar) {
            mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, new ArrayList<>(){{
                add(((ConstChar)initValue).getValue()&0xff);
            }});
        } else if (initValue instanceof ConstArray){
            List<Constant> values = ((ConstArray) initValue).getValues(); // ConstArray中的值
            int initLen = values.size(); // 初始化参数的个数
            List<Integer> initArray = new ArrayList<>(); // 参数值列表
            if(((ArrayType)initValue.getValueType()).getValueType() instanceof IntType){
                for(int i = 0;i<initLen; i++){
                    initArray.add(((ConstInt)values.get(i)).getValue());
                }
                mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, initArray);
            } else {
                for(int i = 0; i < initLen; i++){
                    initArray.add(((ConstChar)values.get(i)).getValue()&0xff);
                }
                // TODO : 暂时都当做4字节来做
                mipsBuilder.buildGlobalVariable(getName(), MipsGlobalVariable.ValueType.intType, initArray);
            }
        }

    }
}
