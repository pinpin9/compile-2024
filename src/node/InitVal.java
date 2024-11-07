package node;

import ir.types.constants.ConstInt;
import token.Token;

import java.util.ArrayList;
import java.util.List;

// InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
public class InitVal extends Node{
    private Exp exp = null;

    private Token lBrace = null;
    private List<Exp> expList = new ArrayList<>();
    private List<Token> commas = new ArrayList<>();
    private Token rBrace = null;

    private Token stringConst;

    public InitVal(Exp exp){
        super(NodeType.InitVal);
        this.exp = exp;
    }
    public InitVal(Token lBrace,List<Exp> expList,List<Token> commas,Token rBrace){
        super(NodeType.InitVal);
        this.lBrace = lBrace;
        this.expList = expList;
        this.commas = commas;
        this.rBrace = rBrace;
    }
    public InitVal(Token stringConst){
        super(NodeType.InitVal);
        this.stringConst = stringConst;
    }

    @Override
    public void print() {
        if(exp!=null){
            exp.print();
        } else if (stringConst!=null) {
            stringConst.print();
        }else {
            lBrace.print();
            for(int i = 0; i<expList.size();i++){
                expList.get(i).print();
                if (i<commas.size()){
                    commas.get(i).print();
                }
            }
            rBrace.print();
        }
        printType();
    }

    @Override
    public void buildIr() {
        if(exp != null){
            exp.buildIr();
        } else if (stringConst!=null) {
            if(valueUpList!=null){
                valueUpList.clear();
            }
            String string = stringConst.getValue();
            // 去掉开头结尾的"
            string = string.substring(1,string.length()-1);
            for(int i = 0; i< string.length();i++){
                char ch = string.charAt(i);
                valueUpList.add(new ConstInt(ch));
            }
        } else {
            for(Exp exp: expList){
                if(stack.isGlobal()){
                    needCalExp = true;
                    exp.buildIr();
                    needCalExp = false;
                    valueUpList.add(valueUp);
                }

            }

        }
    }

    public void traverse() {
        if(exp!=null){
            exp.traverse();
        } else if (expList!=null) {
            for(Exp exp:expList){
                exp.traverse();
            }
        }
    }
}
