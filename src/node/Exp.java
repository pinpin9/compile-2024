package node;

// Exp → AddExp
public class Exp extends Node{
    private AddExp addExp;

    public Exp(AddExp addExp){
        super(NodeType.Exp);
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

    public String getVarType(){
        return addExp.getVarType();
    }
}
