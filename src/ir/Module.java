package ir;

import ir.types.ValueType;
import ir.types.VoidType;

import java.util.ArrayList;
import java.util.List;

public class Module extends Value{
    // 全局只有一个Module，所以为单例模式
    private static Module module = new Module();

    public Module() {
        super(new VoidType(),"module",null);
    }

    public static Module getInstance(){
        return module;
    }
    // 函数模块
    private List<Function> functionList = new ArrayList<>();
    // 全局变量模块
    private List<GlobalVariable> globalVariableList = new ArrayList<>();

    // 添加函数定义
    public void addFunction(Function function){
        functionList.add(function);
    }
    // 添加全局变量声明
    public void addGlobalVariable(GlobalVariable globalVariable){
        globalVariableList.add(globalVariable);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(GlobalVariable globalVariable:globalVariableList){
            stringBuilder.append(globalVariable);
            stringBuilder.append("\n");
        }
        for (Function function:functionList){
            stringBuilder.append(function);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }
}
