package node;

import token.Token;

// LVal → Ident ['[' Exp ']']
public class LVal extends Node{
    private Token ident;
    private Token lBrack;
    private Exp exp;
    private Token rBrack;

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
}
