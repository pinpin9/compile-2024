package node;

import error.SemanticError;
import ir.BasicBlock;
import ir.Function;
import ir.IrSymbolTable;
import ir.Value;
import ir.instructions.memory.Alloca;
import ir.instructions.memory.Store;
import ir.types.CharType;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.VoidType;
import symbol.Symbol;
import symbol.Symbol.SymbolType;
import token.Token;

import java.util.ArrayList;
import java.util.List;

// FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
public class FuncDef extends Node{
    private FuncType funcType;
    private Token ident;
    private Token lParent;
    private FuncFParams funcFParams;
    private Token rParent;
    private Block block;
    public FuncDef(FuncType funcType,Token ident,Token lParent,FuncFParams funcFParams,Token rParent,Block block){
        super(NodeType.FuncDef);
        this.funcType= funcType;
        this.ident = ident;
        this.lParent = lParent;
        this.funcFParams =funcFParams;
        this.rParent = rParent;
        this.block = block;
    }

    @Override
    public void print() {
        funcType.print();
        ident.print();
        lParent.print();
        if(funcFParams!=null){
            funcFParams.print();
        }
        rParent.print();
        block.print();
        printType();
    }

    // 构建函数块的基本步骤：
    // 1. 获取参数类型(ValueType)的列表，在Function构造函数中，创建值Value
    // 2. 在Module中加入当前Function的符号和定义，设置curFunc为当前函数
    // 3. 构造一个基本块为当前函数块的第一个基本块
    // 4. 在栈中创建一个符号表为当前函数的符号表，对函数体进行构建，符号表出栈
    @Override
    public void buildIr() {
        ValueType valueType = null;
        switch (funcType.getFuncType().getValue()){
            case "int" ->{
                valueType = new IntType(32);
            }
            case "char" ->{
                valueType = new CharType();
            }
            case "void" ->{
                valueType = new VoidType();
            }
        }
        sysArgs.clear();
        if(funcFParams!=null){
            funcFParams.buildIr();
        }
        // 构建当前函数块
        Function function = builder.buildFunction(false, ident.getValue(), valueType, sysArgs);
        curFunc = function;
        stack.addSymbol(ident.getValue(), function);
        // 构建基本块
        BasicBlock basicBlock = builder.buildBasicBlock(curFunc);
        curBlock = basicBlock;
        if(funcFParams != null){
            // 构建参数列表的alloca，store指令
            buildParams();
        }
        // 函数体构建
        stack.push(new IrSymbolTable());
        block.buildIr();
        stack.pop();

    }

    private void buildParams(){
        List<FuncFParam> fParams = funcFParams.getFuncFParams();
        List<Value> params = curFunc.getParams();
        for(int i=0;i<fParams.size();i++){
            Value param = params.get(i);
            Alloca alloca = builder.buildAlloca(param.getValueType(), curBlock);
            Store store = builder.buildStore(curBlock, param, alloca);
            stack.addSymbol(fParams.get(i).getIdent().getValue(), alloca);
        }
    }

    // 函数声明，先在当前栈顶符号表中增加函数符号，再新建一个符号表
    public void traverse() {
        List<Symbol> funcParams = new ArrayList<>();
        Symbol symbol = SemanticError.addSymbol(ident.getValue(), getType(), ident.getLineNum() ,this,funcType,funcParams);
        SemanticError.addTable(this);
        // 形参的处理
        if(funcFParams!=null){
            funcFParams.traverse();
            funcParams = funcFParams.getParams();
        }
        if(symbol!=null){
            symbol.setFuncParams(funcParams);
        }

        if(!isVoidFunc()){ // 需要返回值
            SemanticError.inReturnFunc();
            SemanticError.checkReturn(block);
        }
        block.traverse();
        SemanticError.leaveReturnFunc();
        SemanticError.popTable();
    }

    private SymbolType getType(){
        switch (funcType.getFuncType().getValue()){
            case "int" ->{
                return SymbolType.IntFunc;
            }
            case "void" ->{
                return SymbolType.VoidFunc;
            }
            case "char" ->{
                return SymbolType.CharFunc;
            }
        }
        return null;
    }

    private boolean isVoidFunc(){
        return funcType.getFuncType().getValue().equals("void");
    }
}
