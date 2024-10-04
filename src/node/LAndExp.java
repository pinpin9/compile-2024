package node;

import token.Token;

import java.util.List;

// LAndExp → EqExp | LAndExp '&&' EqExp
public class LAndExp extends Node{
    private List<EqExp> eqExps;
    private List<Token> ops;
    public LAndExp(List<EqExp> eqExps,List<Token> ops){
        super(NodeType.LAndExp);
        this.eqExps = eqExps;
        this.ops = ops;
    }

    @Override
    public void print() {
        for(int i=0;i<eqExps.size();i++){
            eqExps.get(i).print();
            printType();
            if(i<ops.size()){
                ops.get(i).print();
            }
        }
    }
}
