package ir;

import ir.instructions.Call;
import ir.instructions.Instruction;
import ir.instructions.binary.*;
import ir.instructions.memory.Alloca;
import ir.instructions.memory.Load;
import ir.instructions.memory.Store;
import ir.instructions.terminator.Ret;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.constants.ConstArray;
import ir.types.constants.Constant;
import node.CompUnit;
import settings.Settings;
import tools.IO;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class IrBuilder {
    private static IrBuilder irBuilder = new IrBuilder();
    public static IrBuilder getInstance(){
        return irBuilder;
    }
    private Module module = Module.getInstance();
    IrSymbolStack stack = IrSymbolStack.getInstance();

    // 命名计数器
    private int nameCnt = 0;

    // 新的作用域，计数器归零
    private void resetNameCnt(){
        nameCnt = 0;
    }

    public void buildIr(CompUnit compUnit){
        compUnit.buildIr();
    }

    public String getName(){
        return Integer.toString(nameCnt++);
    }

    /**
     * 方法描述： 构建函数模块
     * 命名计数器清零，module中增加该函数模块，栈顶符号表加入函数名
     * @param isLibFunc 是否为链接函数
     * @param name 函数名
     * @param rtnType 返回类型
     * @param args 参数类型数组
     */
    public Function buildFunction(Boolean isLibFunc, String name, ValueType rtnType, List<ValueType> args){
        resetNameCnt();
        Function function = new Function(isLibFunc, name, rtnType, args);
        module.addFunction(function);
        stack.addSymbol(name, function);
        return function;
    }
    /**
     * 方法描述： 构建基本块
     * 在所属Function模块中，新建一个基本块
     * @param function 所属Function块
     */
    public BasicBlock buildBasicBlock(Function function){
        BasicBlock block = new BasicBlock(getName(), function);
        function.addBasicBlock(block);
        return block;
    }
    /**
     * 方法描述： 添加全局变量
     * 常量 | 变量
     * @param name 变量 | 常量名称
     * @param valueType 类型，intType | charType | ArrayType
     * @param isConst 是否常量
     * @param initValue 初始化值
     */
    public GlobalVariable buildGlobalVariable(String name,ValueType valueType, boolean isConst, Constant initValue){
        GlobalVariable globalVariable = new GlobalVariable(name, valueType, isConst, initValue);
        module.addGlobalVariable(globalVariable);
        stack.addSymbol(name, globalVariable);
        return globalVariable;
    }

    /**
     * 方法描述： 构建Alloca指令
     * 无初始值
     * 因为分配空间的指令位于函数模块的首部基本块中，所以传入的基本块不是存储分配空间指令的真实块
     * @param allocatedType 分配空间的内存类型，int | char | 数组
     * @param basicBlock 当前的基本块
     */
    public Alloca buildAlloca(ValueType allocatedType, BasicBlock basicBlock){
        BasicBlock realParent = ((Function)basicBlock.getParent()).getFirstBlock();
        Alloca alloca = new Alloca(allocatedType, getName(), realParent);
        basicBlock.addHeadInstruction(alloca);
        return alloca;
    }

    /**
     * 方法描述： 构建Alloca指令
     * 有初始值
     * 因为分配空间的指令位于函数模块的首部基本块中，所以传入的基本块不是存储分配空间指令的真实块
     * @param allocatedType 分配空间的内存类型，int | char | 数组
     * @param basicBlock 当前的基本块
     * @param constArray 常数初值
     */
    public Alloca buildAlloca(ValueType allocatedType, BasicBlock basicBlock, ConstArray constArray){
        BasicBlock realParent = ((Function)basicBlock.getParent()).getFirstBlock();
        Alloca alloca = new Alloca(allocatedType, getName(), realParent, constArray);
        basicBlock.addHeadInstruction(alloca);
        return alloca;
    }

    /**
     * 方法描述： 构建Store指令
     * @param block 所属基本块
     * @param value 待存储的内容
     * @param pointer 目的地址
     */
    public Store buildStore(BasicBlock block, Value value, Value pointer){
        Store store = new Store(block, value, pointer);
        block.addTailInstruction(store);
        return store;
    }

    /**
     * 方法描述： 构建Load指令
     * @param block 所属基本块
     * @param pointer 加载的地址
     */
    public Load buildLoad(BasicBlock block, Value pointer){
        Load load = new Load(getName(), block, pointer);
        block.addTailInstruction(load);
        return load;
    }

    public Ret buildRet(BasicBlock block, Value value){
        Ret ret = new Ret(block, value);
        block.addTailInstruction(ret);
        return ret;
    }

    public Ret buildRet(BasicBlock block){
        Ret ret = new Ret(block);
        block.addTailInstruction(ret);
        return ret;
    }

    /**
     * 方法描述： 构建Add指令，+
     * @param block 所属基本块
     * @param value1 操作数1
     * @param value2 操作数2
     */
    public Add buildAdd(BasicBlock block, Value value1, Value value2){
        Add add = new Add(new IntType(32), getName(), block, value1, value2);
        block.addTailInstruction(add);
        return add;
    }

    public Sub buildSub(BasicBlock block, Value value1, Value value2){
        Sub sub = new Sub(new IntType(32), getName(), block, value1, value2);
        block.addTailInstruction(sub);
        return sub;
    }

    public Mul buildMul(BasicBlock block, Value value1, Value value2){
        Mul mul = new Mul(new IntType(32), getName(), block, value1, value2);
        block.addTailInstruction(mul);
        return mul;
    }

    public Sdiv buildSdiv(BasicBlock block, Value value1, Value value2){
        Sdiv sdiv = new Sdiv(new IntType(32), getName(), block, value1, value2);
        block.addTailInstruction(sdiv);
        return sdiv;
    }

    public Srem buildSrem(BasicBlock block, Value value1, Value value2){
        Srem srem = new Srem(new IntType(32), getName(), block, value1, value2);
        block.addTailInstruction(srem);
        return srem;
    }

    public Call buildCall(BasicBlock block, Function function, ArrayList<Value> args){
        Call call = new Call(getName(),block, function, args);
        block.addTailInstruction(call);
        return call;
    }

    // 输出
    public static IO irOutput = new IO(Settings.llvmFile);

    /**
     * 方法描述：
     */
    public void print(){
        irOutput.output(module.toString());
    }
}
