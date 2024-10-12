package node;

import error.SemanticError;
import symbol.Symbol;
import symbol.Symbol.SymbolType;
import symbol.SymbolStack;
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
        symbol.setFuncParams(funcParams);

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
