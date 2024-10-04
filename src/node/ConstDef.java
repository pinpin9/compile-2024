package node;

import token.Token;

import java.util.List;

// 常量定义
// 一维数组或常数
// ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
public class ConstDef extends Node{
    private Token ident;
    private Token lBrack;
    private ConstExp constExp;
    private Token rBrack;
    private Token assign;
    private ConstInitVal constInitVal;

    public ConstDef(Token ident, Token lBrack, ConstExp constExp, Token rBrack,Token assign, ConstInitVal constInitVal){
        super(NodeType.ConstDef);
        this.ident = ident;
        this.lBrack = lBrack;
        this.constExp = constExp;
        this.rBrack = rBrack;
        this.assign = assign;
        this.constInitVal = constInitVal;
    }

    @Override
    public void print() {
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            constExp.print();
            rBrack.print();
        }
        assign.print();
        constInitVal.print();
        printType();
    }
}
