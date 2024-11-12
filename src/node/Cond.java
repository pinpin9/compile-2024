package node;

import ir.BasicBlock;

// Cond → LOrExp
public class Cond extends Node{
    LOrExp lOrExp;

    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;

    public void setTrueBlock(BasicBlock trueBlock){
        this.trueBlock = trueBlock;
    }
    public void setFalseBlock(BasicBlock falseBlock){
        this.falseBlock = falseBlock;
    }

    public Cond(LOrExp lOrExp){
        super(NodeType.Cond);
        this.lOrExp = lOrExp;
    }

    @Override
    public void print() {
        lOrExp.print();
        printType();
    }

    @Override
    public void buildIr() {
        lOrExp.setTrueBlock(trueBlock);
        lOrExp.setFalseBlock(falseBlock);
        lOrExp.buildIr();
    }

    public void traverse() {
        lOrExp.traverse();
    }
}
