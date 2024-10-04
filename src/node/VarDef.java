package node;

import token.Token;

// VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
public class VarDef extends Node{
    private Token ident;
    private Token lBrack;
    private ConstExp constExp;
    private Token rBrack;
    private Token assign;
    private InitVal initVal;

    public VarDef(Token ident, Token lBrack, ConstExp constExp,Token rBrack,Token assign,InitVal initVal){
        super(NodeType.VarDef);
        this.ident =ident;
        this.lBrack = lBrack;
        this.constExp = constExp;
        this.rBrack = rBrack;
        this.assign = assign;
        this.initVal = initVal;
    }

    @Override
    public void print() {
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            constExp.print();
            rBrack.print();
        }
        if(assign!=null){
            assign.print();
            initVal.print();
        }
        printType();
    }
}
