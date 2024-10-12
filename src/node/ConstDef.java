package node;

import error.SemanticError;
import symbol.Symbol;
import token.Token;
import symbol.Symbol.SymbolType;

import java.util.List;

// 常量定义
// 一维数组或常数
// ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
public class ConstDef extends Node{
    private Token ident;
    private Token lBrack;
    private ConstExp constExp;
    private Token rBrack;
    private Token assign;
    private ConstInitVal constInitVal;
    private BType bType; // 存储变量的类型

    public ConstDef(Token ident, Token lBrack, ConstExp constExp, Token rBrack,Token assign, ConstInitVal constInitVal){
        super(NodeType.ConstDef);
        this.ident = ident;
        this.lBrack = lBrack;
        this.constExp = constExp;
        this.rBrack = rBrack;
        this.assign = assign;
        this.constInitVal = constInitVal;
    }

    public void setBType(BType bType){
        this.bType = bType;
    }

    @Override
    public void print() {
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            constExp.print();
            rBrack.print();
        }
        assign.print();
        constInitVal.print();
        printType();
    }

    public void traverse() {
        SemanticError.addSymbol(ident.getValue(), getType(), ident.getLineNum(), this);
        if(constExp!=null){
            constExp.traverse();
        }
        constInitVal.traverse();
    }

    private SymbolType getType(){
        if(lBrack==null){ // 非数组
            if (bType.getbType().getValue().equals("int")){
                return SymbolType.ConstInt;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.ConstChar;
            }
        }else { // 数组
            if (bType.getbType().getValue().equals("int")){
                return SymbolType.ConstIntArray;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.ConstCharArray;
            }
        }
        return null;
    }
}
