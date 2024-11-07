package node;

import token.Token;

import java.util.List;

// RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
public class RelExp extends Node{
    private List<AddExp> addExps;
    private List<Token> ops;

    public RelExp(List<AddExp> addExps,List<Token> ops){
        super(NodeType.RelExp);
        this.addExps = addExps;
        this.ops = ops;
    }

    @Override
    public void print() {
        for(int i = 0; i < addExps.size(); i++){
            addExps.get(i).print();
            printType();
            if(i<ops.size()){
                ops.get(i).print();
            }
        }
    }

    @Override
    public void buildIr() {

    }

    public void traverse() {
        for(AddExp addExp:addExps){
            addExp.traverse();
        }
    }
}
