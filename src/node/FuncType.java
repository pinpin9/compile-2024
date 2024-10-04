package node;

import token.Token;

public class FuncType extends Node{
    private Token funcType;
    public FuncType(Token funcType){
        super(NodeType.FuncType);
        this.funcType = funcType;
    }

    @Override
    public void print() {
        funcType.print();
        printType();
    }
}
