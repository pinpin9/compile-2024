package node;

import token.Token;

public class UnaryOp extends Node{
    private Token token;
    public UnaryOp(Token token){
        super(NodeType.UnaryOp);
        this.token = token;
    }

    @Override
    public void print() {
        token.print();
        printType();
    }

    public Token getOp(){
        return token;
    }
    @Override
    public void buildIr() {

    }

    public void traverse() {

    }
}
