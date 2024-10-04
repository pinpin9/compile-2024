package node;

import token.Token;

import java.util.List;
// FuncFParams → FuncFParam { ',' FuncFParam }
public class FuncFParams extends Node{
    private List<FuncFParam> funcFParams;
    private List<Token> commas;

    public FuncFParams(List<FuncFParam> funcFParams, List<Token> commas){
        super(NodeType.FuncFParams);
        this.funcFParams = funcFParams;
        this.commas = commas;
    }

    @Override
    public void print() {
        for(int i=0;i<funcFParams.size();i++){
            funcFParams.get(i).print();
            if(i<commas.size()){
                commas.get(i).print();
            }
        }
        printType();
    }
}
