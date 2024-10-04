package node;

import token.Token;

import java.util.List;
import java.util.Locale;

// LOrExp → LAndExp | LOrExp '||' LAndExp
public class LOrExp extends Node{
    private List<LAndExp> lAndExps;
    private List<Token> ops;

    public LOrExp(List<LAndExp> lAndExps, List<Token> ops){
        super(NodeType.LOrExp);
        this.lAndExps = lAndExps;
        this.ops =ops;
    }

    @Override
    public void print() {
        for(int i = 0;i<lAndExps.size();i++){
            lAndExps.get(i).print();
            printType();
            if(i<ops.size()){
                ops.get(i).print();
            }
        }
    }
}
