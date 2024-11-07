package node;

// ConstExp → AddExp
public class ConstExp extends Node{
    private AddExp addExp;
    public ConstExp(AddExp addExp){
        super(NodeType.ConstExp);
        this.addExp = addExp;
    }

    @Override
    public void print() {
        addExp.print();
        printType();
    }

    @Override
    public void buildIr() {
        needCalExp = true;
        addExp.buildIr();
        needCalExp = false;
    }

    public void traverse() {
        addExp.traverse();
    }
}
