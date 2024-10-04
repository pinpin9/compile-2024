package node;

import token.Token;

// FuncFParam → BType Ident ['[' ']']
public class FuncFParam extends Node{
    private BType bType;
    private Token ident;
    private Token lBrack;
    private Token rBrack;

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
}
