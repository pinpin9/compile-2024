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

    public void traverse() {
        addExp.traverse();
    }
}
