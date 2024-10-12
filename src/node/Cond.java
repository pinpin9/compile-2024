package node;

// Cond → LOrExp
public class Cond extends Node{
    LOrExp lOrExp;

    public Cond(LOrExp lOrExp){
        super(NodeType.Cond);
        this.lOrExp = lOrExp;
    }

    @Override
    public void print() {
        lOrExp.print();
        printType();
    }

    public void traverse() {
        lOrExp.traverse();
    }
}
