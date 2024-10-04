package node;

import token.Token;

// ForStmt → LVal '=' Exp
public class ForStmt extends Node{
    private LVal lVal;
    private Token assign;
    private Exp exp;

    public ForStmt(LVal lVal, Token assign, Exp exp){
        super(NodeType.ForStmt);
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }

    @Override
    public void print() {
        lVal.print();
        assign.print();
        exp.print();
        printType();
    }
}
