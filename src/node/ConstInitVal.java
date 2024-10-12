package node;

import token.Token;

import java.util.ArrayList;
import java.util.List;

//  常量初值
//  ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
public class ConstInitVal extends Node{
    private ConstExp constExp = null;

    private Token lBrace = null;
    private List<ConstExp> constExpList = new ArrayList<>();
    private List<Token> commas = new ArrayList<>();
    private Token rBrace = null;

    private Token stringConst = null;

    public ConstInitVal(ConstExp constExp){
        super(NodeType.ConstInitVal);
        this.constExp = constExp;
    }
    public ConstInitVal(Token lBrace, List<ConstExp> constExpList,List<Token> commas,Token rBrace){
        super(NodeType.ConstInitVal);
        this.lBrace = lBrace;
        this.constExpList = constExpList;
        this.commas = commas;
        this.rBrace = rBrace;
    }
    public ConstInitVal(Token stringConst){
        super(NodeType.ConstInitVal);
        this.stringConst = stringConst;
    }

    @Override
    public void print() {
        if(constExp!=null){
            constExp.print();
        } else if (stringConst!=null) {
            stringConst.print();
        }else {
            lBrace.print();
            for(int i = 0; i < constExpList.size(); i++){
                constExpList.get(i).print();
                if(i < commas.size()){
                    commas.get(i).print();
                }
            }
            rBrace.print();
        }
        printType();
    }

    public void traverse() {
        if(constExp!=null){
            constExp.traverse();
        }else if(constExpList!=null){
            for(ConstExp constExp:constExpList){
                constExp.traverse();
            }
        }
    }
}
