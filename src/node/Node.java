package node;

import frontend.ParserAnalyze;
import ir.*;
import ir.types.ValueType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class Node {
    public enum NodeType{
        AddExp,
        Block,
        BlockItem,
        BType,
        Character,
        CompUnit,
        Cond,
        ConstDecl,
        ConstDef,
        ConstExp,
        ConstInitVal,
        Decl,
        EqExp,
        Exp,
        ForStmt,
        FuncDef,
        FuncFParam,
        FuncFParams,
        FuncRParams,
        FuncType,
        InitVal,
        LAndExp,
        LOrExp,
        LVal,
        MainFuncDef,
        MulExp,
        Number,
        PrimaryExp,
        RelExp,
        Stmt,
        UnaryExp,
        UnaryOp,
        VarDecl,
        VarDef
    }
    // 构建操作类
    public IrBuilder builder = IrBuilder.getInstance();
    // 符号栈
    public IrSymbolStack stack = IrSymbolStack.getInstance();
    public NodeType type;

    public Node(NodeType nodeType) {
        type = nodeType;
    }

    // 遍历输出语法分析结果
    public abstract void print();
    // 遍历生成中间代码
    public abstract void buildIr();
    public void printType() {
        ParserAnalyze.parserOutput.output("<"+type.toString()+">");
    }

    // 当前函数块
    protected static Function curFunc = null;
    // 当前基本块
    protected static BasicBlock curBlock = null;
    protected static Stack<BasicBlock> endLoop = new Stack<>();
    protected static Stack<BasicBlock> nextLoop = new Stack<>();

    /*==========继承属性==========*/
    protected static boolean needCalExp = false; // 表达式可求值，求值结果为valueUp，用于全局变量的初始化部分
    protected static ValueType rtnType = null; // 函数返回类型
    protected static boolean lValAtLeft = false; // 是赋值变量还是计算变量
    protected static boolean buildFuncRParams = false;

    /*==========综合属性==========*/
    protected static List<ValueType> sysArgs = new ArrayList<>(); // 函数的参数类型数组，所有的参数，往上传
    protected static ValueType argType = null; // 函数的参数类型
    protected static Value valueUp = null; // Value类型的回传
    protected static List<Value> valueUpList = new ArrayList<>(); // Value类型数组的回传
    protected static Stack<Value> funcParams = new Stack<>(); // 函数实参
    protected static boolean singleCmp = false; // 是否只有单个符号

}
