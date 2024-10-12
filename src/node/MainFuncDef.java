package node;

import error.SemanticError;
import symbol.Symbol;
import token.Token;

// MainFuncDef → 'int' 'main' '(' ')' Block
public class MainFuncDef extends Node{
    private Token intToken;
    private Token mainToken;
    private Token lParent;
    private Token rParent;
    private Block block;

    public MainFuncDef(Token intToken,Token mainToken,Token lParent,Token rParent,Block block){
        super(NodeType.MainFuncDef);
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lParent = lParent;
        this.rParent = rParent;
        this.block = block;
    }

    @Override
    public void print() {
        intToken.print();
        mainToken.print();
        lParent.print();
        rParent.print();
        block.print();
        printType();
    }

    public void traverse() {
        SemanticError.inReturnFunc();
        SemanticError.checkReturn(block);
        SemanticError.addTable(this);
        block.traverse();
        SemanticError.popTable();
        SemanticError.leaveReturnFunc();
    }
}
