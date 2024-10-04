package node;

import token.Token;

// BType → 'int' | 'char'
public class BType extends Node{
    private Token bType;
    public BType(Token bType){
        super(NodeType.BType);
        this.bType = bType;
    }

    @Override
    public void print() {
        bType.print();
    }
}
