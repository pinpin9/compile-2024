package node;

import token.Token;

import java.util.List;

// FuncRParams → Exp { ',' Exp }
public class FuncRParams extends Node{
    private List<Exp> expList;
    private List<Token> commas;
    public FuncRParams(List<Exp> expList,List<Token> commas) {
        super(NodeType.FuncRParams);
        this.expList = expList;
        this.commas = commas;
    }

    @Override
    public void print() {
        for(int i = 0;i < expList.size();i++){
            expList.get(i).print();
            if(i<commas.size()){
                commas.get(i).print();
            }
        }
        printType();
    }
}
