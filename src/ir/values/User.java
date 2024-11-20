package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;
import java.util.List;

public abstract class User extends Value{
    // 当前操作对象所引用的所有操作数
    private List<Value> operands = new ArrayList<>();

    // 不带操作数的构造
    public User(ValueType valueType, String name, Value parent){
        super(valueType, name, parent);
    }
    // 带操作数的构造
    public User(ValueType valueType, String name, Value parent, List<Value> operands){
        super(valueType, name, parent);
        this.operands.addAll(operands);
    }

    // 增加操作数
    public void addOperand(Value value){
        this.operands.add(value);
    }
    public List<Value> getOperands(){
        return operands;
    }
}
