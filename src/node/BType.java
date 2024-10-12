package node;

import token.Token;

// BType → 'int' | 'char'
public class BType extends Node{
    private Token bType;
    public BType(Token bType){
        super(NodeType.BType);
        this.bType = bType;
    }

    public Token getbType() {
        return bType;
    }

    @Override
    public void print() {
        bType.print();
    }

    public void traverse() {

    }
}
