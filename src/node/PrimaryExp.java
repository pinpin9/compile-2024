package node;

import token.Token;

// PrimaryExp → '(' Exp ')' | LVal | Number | Character
public class PrimaryExp extends Node{
    private Token lParent = null;
    private Exp exp = null;
    private Token rParent = null;
    private LVal lVal = null;
    private Number number = null;
    private Character character = null;

    public PrimaryExp(Token lParent,Exp exp,Token rParent){
        super(NodeType.PrimaryExp);
        this.lParent = lParent;
        this.exp = exp;
        this.rParent = rParent;
    }
    public PrimaryExp(LVal lVal){
        super(NodeType.PrimaryExp);
        this.lVal = lVal;
    }
    public PrimaryExp(Number number){
        super(NodeType.PrimaryExp);
        this.number = number;
    }
    public PrimaryExp(Character character){
        super(NodeType.PrimaryExp);
        this.character = character;
    }

    @Override
    public void print() {
        if(lParent!=null){
            lParent.print();
            exp.print();
            rParent.print();
        } else if (lVal != null) {
            lVal.print();
        } else if (number!=null) {
            number.print();
        }else {
            character.print();
        }
        printType();
    }

    public void traverse() {
        if(exp!=null){
            exp.traverse();
        } else if (lVal!=null) {
            lVal.traverse();
        }
    }
}
