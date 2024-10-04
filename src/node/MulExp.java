package node;

import token.Token;

import java.util.ArrayList;
import java.util.List;

// MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
public class MulExp extends Node{
    private List<UnaryExp> unaryExps;
    private List<Token> ops;

    public MulExp(List<UnaryExp> unaryExps,List<Token> ops){
        super(NodeType.MulExp);
        this.unaryExps = unaryExps;
        this.ops = ops;
    }

    @Override
    public void print() {
        for(int i=0;i<unaryExps.size();i++){
            unaryExps.get(i).print();
            printType();
            if(i<ops.size()){
                ops.get(i).print();
            }
        }
    }
}
