package node;

import frontend.ParserAnalyze;
import ir.BasicBlock;
import ir.Value;
import ir.instructions.binary.Icmp;
import token.Token;

import java.util.List;

// EqExp → RelExp | EqExp ('==' | '!=') RelExp
public class EqExp extends Node{
    private List<RelExp> relExps;
    private List<Token> ops;


    public EqExp(List<RelExp> relExps, List<Token> ops){
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

    @Override
    public void buildIr() {
        relExps.get(0).buildIr();
        Value lValue = valueUp;
        for(int i = 1; i < relExps.size(); i++){
            singleCmp = false;
            relExps.get(i).buildIr();
            Value rValue = valueUp;
            if(lValue.getValueType().isI1() || lValue.getValueType().isChar()){
                lValue = builder.buildZext(curBlock, lValue);
            }
            if (rValue.getValueType().isI1() || rValue.getValueType().isChar()){
                rValue = builder.buildZext(curBlock, rValue);
            }
            switch (ops.get(i-1).getType()){
                case EQL -> lValue = builder.buildIcmp(Icmp.Cond.EQ, curBlock, lValue, rValue); // ==
                case NEQ -> lValue = builder.buildIcmp(Icmp.Cond.NE, curBlock, lValue, rValue); // !=
            }
        }
        valueUp = lValue;
    }

    public void traverse() {
        for(RelExp relExp:relExps){
            relExp.traverse();
        }
    }
}
