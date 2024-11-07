package node;

import ir.types.constants.ConstInt;
import token.Token;

// Number → IntConst
public class Number extends Node{
    private Token intConst;
    public Number(Token intConst){
        super(NodeType.Number);
        this.intConst = intConst;
    }

    @Override
    public void print() {
        intConst.print();
        printType();
    }

    @Override
    public void buildIr() {
        valueUp = new ConstInt(Integer.parseInt(intConst.getValue()));
    }

    public void traverse() {

    }
}
