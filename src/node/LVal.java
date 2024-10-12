package node;

import error.SemanticError;
import symbol.Symbol;
import token.Token;

// LVal → Ident ['[' Exp ']']
public class LVal extends Node{
    private Token ident;
    private Token lBrack;
    private Exp exp;
    private Token rBrack;

    public Token getIdent(){
        return ident;
    }
    public LVal(Token ident, Token lBrack, Exp exp, Token rBrack){
        super(NodeType.LVal);
        this.ident = ident;
        this.exp = exp;
        this.lBrack = lBrack;
        this.rBrack = rBrack;
    }

    @Override
    public void print() {
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            exp.print();
            rBrack.print();
        }
        printType();
    }

    public void traverse() {
        // 检查标识符是否存在
        SemanticError.checkSymbol(ident.getValue(), ident.getLineNum());
        if(exp!=null){
            exp.traverse();
        }
    }

    public String getVarType() {
        Symbol symbol = SemanticError.getSymbol(ident.getValue());
        String type = symbol.getType().toString();
        if(type.contains("Const")){
            type = type.substring(5);
        }
        if(exp!=null&&type.contains("Array")){
            type = type.replace("Array","");
        }
        return type;
    }
}
