package node;

import error.SemanticError;
import ir.Value;
import ir.types.CharType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.ValueType;
import symbol.Symbol;
import token.Token;

// FuncFParam → BType Ident ['[' ']']
public class FuncFParam extends Node{
    private BType bType;
    private Token ident;
    private Token lBrack;
    private Token rBrack;

    public Token getIdent(){
        return ident;
    }

    public FuncFParam(BType bType,Token ident,Token lBrack,Token rBrack){
        super(NodeType.FuncFParam);
        this.bType = bType;
        this.ident = ident;
        this.lBrack = lBrack;
        this.rBrack = rBrack;
    }

    @Override
    public void print() {
        bType.print();
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            rBrack.print();
        }
        printType();
    }

    @Override
    public void buildIr() {
        ValueType valueType = null;
        switch (bType.getbType().getValue()){
            case "int"->{
                valueType = new IntType(32);
            }
            case "char" -> {
                valueType = new CharType();
            }
        }
        if(lBrack == null){
            argType = valueType;
        }else{
            argType = new PointerType(valueType);
        }
    }

    // 返回当前的参数符号，添加到FuncFParams中，存储到函数名符号中
    public Symbol traverse() {
        return SemanticError.addSymbol(ident.getValue(), getType(), ident.getLineNum(), this);
    }

    private Symbol.SymbolType getType(){
        if(lBrack == null){ // 非数组
            if(bType.getbType().getValue().equals("int")){
                return Symbol.SymbolType.Int;
            } else if (bType.getbType().getValue().equals("char")) {
                return Symbol.SymbolType.Char;
            }
        }else{ // 数组
            if(bType.getbType().getValue().equals("int")){
                return Symbol.SymbolType.IntArray;
            } else if (bType.getbType().getValue().equals("char")) {
                return Symbol.SymbolType.CharArray;
            }
        }
        return null;
    }
}
