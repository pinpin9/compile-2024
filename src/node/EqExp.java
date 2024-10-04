package node;

import frontend.ParserAnalyze;
import token.Token;

import java.util.List;

// EqExp → RelExp | EqExp ('==' | '!=') RelExp
public class EqExp extends Node{
    private List<RelExp> relExps;
    private List<Token> ops;

    public EqExp(List<RelExp> relExps,List<Token> ops){
        super(NodeType.EqExp);
        this.relExps = relExps;
        this.ops = ops;
    }

    @Override
    public void print() {
        for(int i=0;i<relExps.size();i++){
            relExps.get(i).print();
            printType();
            if (i<ops.size()){
                ops.get(i).print();
            }
        }
    }
}
