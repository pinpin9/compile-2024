package ir.instructions;

import ir.BasicBlock;
import ir.Function;
import ir.Value;

import java.util.ArrayList;
import java.util.List;

// <result> = call [ret attrs] <ty> <name>(<...args>)
public class Call extends Instruction{
    /**
     * 方法描述：call指令的构造函数
     * call的ValueType和function函数的rtnType一致
     * @param basicBlock
     * @param function
     * @param args
     */
    public Call(String name, BasicBlock basicBlock, Function function, List<Value> args){
        super(function.getRetType(), name, basicBlock, new ArrayList<Value>(){{
            add(function);
            addAll(args);
        }});
    }

    // %6 = call i32 @foo(i32 %4, i32 %5)
    // call void @putint(i32 %6)
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        Function function = (Function) getOperands().get(0);
        if(!getName().isEmpty()){
            stringBuilder.append(getName()).append(" = ");
        }
        stringBuilder.append("call ").append(getValueType()).append(" ").append(function.getName()).append("(");
        for(int i=1;i<getOperands().size();i++){
            Value value = getOperands().get(i);
            stringBuilder.append(value.getValueType()).append(" ").append(value.getName());
            if(i<getOperands().size()-1){
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }
}
