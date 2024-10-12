package node;

import token.Token;

public class Character extends Node{
    private Token charConst;
    public Character(Token charConst) {
        super(NodeType.Character);
        this.charConst = charConst;
    }

    @Override
    public void print() {
        charConst.print();
        printType();
    }

    public void traverse() {

    }
}
