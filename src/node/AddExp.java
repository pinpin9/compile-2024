package node;

import token.Token;

import java.util.List;

// AddExp → MulExp | AddExp ('+' | '−') MulExp
public class AddExp extends Node{
    private List<MulExp> mulExps;
    private List<Token> ops;
    public AddExp(List<MulExp> mulExps,List<Token> ops){
        super(NodeType.AddExp);
        this.mulExps = mulExps;
        this.ops = ops;
    }

    @Override
    public void print() {
        for(int i=0;i<mulExps.size();i++){
            mulExps.get(i).print();
            printType();
            if(i<ops.size()){
                ops.get(i).print();
            }
        }
    }
}

