package node;

import ir.Value;
import ir.instructions.binary.Icmp;
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
        addExps.get(0).buildIr();
        Value lValue = valueUp;
        for(int i = 1;i<addExps.size();i++){
            singleCmp = false;
            addExps.get(i).buildIr();
            Value rValue = valueUp;
            switch (ops.get(i-1).getType()){
                case LSS -> lValue = builder.buildIcmp(Icmp.Cond.SLT, curBlock, lValue, rValue); // <
                case LEQ -> lValue = builder.buildIcmp(Icmp.Cond.SLE, curBlock, lValue, rValue); // <=
                case GRE -> lValue = builder.buildIcmp(Icmp.Cond.SGT, curBlock, lValue, rValue); // >
                case GEQ -> lValue = builder.buildIcmp(Icmp.Cond.SGE, curBlock, lValue, rValue); // >=
            }
        }
        valueUp = lValue;
    }

    public void traverse() {
        for(AddExp addExp:addExps){
            addExp.traverse();
        }
    }
}
