package node;

import token.Token;

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
}
