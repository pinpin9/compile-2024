package ir.values;

import backend.Mc;
import backend.MipsBuilder;
import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;
import ir.IrSymbolTable;
import ir.types.ValueType;

import java.util.ArrayList;
import java.util.List;


/**
 * @author zlp
 * @Discriptionv 函数结点：具有返回值，参数列表以及下属基本块属性
 * @date 2024/11/01
 */
public class Function extends User {
    // 是否链接函数
    private boolean isLibFunc;
    // 形参列表
    private List<Value> params = new ArrayList<>();
    // 基本块列表
    private List<BasicBlock> basicBlockList = new ArrayList<>();
    // 返回值类型
    private ValueType retType;

    // 链接函数
    public static Function getint = null;
    public static Function getchar = null;
    public static Function putint = null;
    public static Function putchar = null;
    public static Function putstr = null;

    // 当前函数模块对应的符号表
    private IrSymbolTable irSymbolTable = null;

    public List<Value> getParams() {
        return params;
    }

    public List<BasicBlock> getBasicBlockList() {
        return basicBlockList;
    }

    public ValueType getRetType() {
        return retType;
    }

    public IrSymbolTable getIrSymbolTable() {
        return irSymbolTable;
    }

    public int getArgsCnt(){
        return params.size();
    }

    public void setIrSymbolTable(IrSymbolTable irSymbolTable) {
        this.irSymbolTable = irSymbolTable;
    }

    public boolean isLibFunc() {
        return isLibFunc;
    }
    public Function(Boolean isLibFunc,String name, ValueType retType, List<ValueType> argsType){
        super(retType, "@"+name, Module.getInstance());
        this.isLibFunc = isLibFunc;
        this.retType = retType;
        for(int i = 0;i<argsType.size();i++){
            params.add(new Value(argsType.get(i), "%"+i, this, i));
        }
    }
    // 添加基本块
    public void addBasicBlock(BasicBlock block){
        basicBlockList.add(block);
    }
    // 获得当前函数模块中第一个基本块
    public BasicBlock getFirstBlock(){
        return basicBlockList.get(0);
    }
    public void addSymbol(String name, Value value){
        irSymbolTable.addSymbol(name, value);
    }

    /*
    define dso_local void @bar() {
        store i32 1200, i32* @a
        ret void
    }
    declare void @putstr(i8*)
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if(isLibFunc){
            stringBuilder.append("declare ");
        }else{
            stringBuilder.append("define dso_local ");
        }
        stringBuilder.append(retType+" "+getName()+"(");
        for(int i = 0; i<params.size(); i++){
            Value value = params.get(i);
            stringBuilder.append(value.getValueType());
            if (!isLibFunc){
                stringBuilder.append(" "+value.getName());
            }
            if(i<params.size()-1){
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        if(!isLibFunc){
            stringBuilder.append("{\n");
            for(BasicBlock basicBlock:basicBlockList){
                stringBuilder.append(basicBlock);
            }
            stringBuilder.append("}");
        }
        return stringBuilder.toString();
    }

    //==========目标代码生成==========
    public void buildMips(){
        // 只有非链接函数才需要构建
        if(!isLibFunc){
            Mc.curIrFunction = this;
            MipsFunction mipsFunction = Mc.getMappedFunction(this); // 对应的mips函数块
            MipsBasicBlock firstBlock = Mc.getMappedBlock(getFirstBlock()); // 第一个基本块
            for(BasicBlock block : basicBlockList){
                block.buildMips();
            }
            // 进行序列化
            mipsFunction.blockSerialize(firstBlock);
        }
    }
}
