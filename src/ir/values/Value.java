package ir.values;

import ir.types.ValueType;

import java.util.ArrayList;
import java.util.List;

public class Value {
    private final int id; // 唯一标识
    private ValueType valueType; // 类型
    private String name; // 虚拟寄存器名称
    private Value parent; // 包含当前Value的父Value，表示嵌套关系，例如Instruction可能嵌套在Function中
    private List<User> userList = new ArrayList<>(); // 使用者
    private boolean isArg = false; // 是否是函数形参
    private int argNum = 0; // 是第几个函数参数

    private static int idCount=0;

    public Value(ValueType valueType, String name, Value parent){
        this.id = idCount++;
        this.valueType = valueType;
        this.name = name;
        this.parent = parent;
    }

    public Value(ValueType valueType, String name, Value parent, int i){
        this.id = idCount++;
        this.valueType = valueType;
        this.name = name;
        this.argNum = i;
        this.isArg = true;
        this.parent = parent;
    }


    public int getId() {
        return id;
    }

    public ValueType getValueType() {
        return valueType;
    }

    public String getName() {
        return name;
    }

    public List<User> getUserList() {
        return userList;
    }

    public Value getParent() {
        return parent;
    }

    public boolean isArg() {
        return isArg;
    }


    public int getArgNum() {
        return argNum;
    }

    // 增加被使用的记录，就是增加user列表中的user元素
    public void addUser(User user){
        userList.add(user);
    }

    public String getNameCnt(){
        return name.substring(1);
    }

    @Override
    public String toString(){
        return valueType + " " + name;
    }
}
