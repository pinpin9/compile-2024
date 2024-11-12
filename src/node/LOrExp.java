package node;

import ir.BasicBlock;
import token.Token;

import java.util.List;
import java.util.Locale;

// LOrExp → LAndExp | LOrExp '||' LAndExp
public class LOrExp extends Node{
    private List<LAndExp> lAndExps;
    private List<Token> ops;

    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;

    public void setTrueBlock(BasicBlock trueBlock) {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock) {
        this.falseBlock = falseBlock;
    }

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

    // LAnd { '||' LAnd }
    @Override
    public void buildIr() {
        // 如果正确，则直接进入TrueBlock，错误则为后面的LAndExp语句重新建一个基本块
        for(int i = 0;i<lAndExps.size()-1;i++){
            // lOr || lAnd
            LAndExp lOr = lAndExps.get(i);
            BasicBlock andBlock = builder.buildBasicBlock(curFunc);
            lOr.setTrueBlock(trueBlock);
            lOr.setFalseBlock(andBlock);
            lOr.buildIr();

            curBlock = andBlock;
        }

        LAndExp tailExp = lAndExps.get(lAndExps.size()-1);
        tailExp.setTrueBlock(trueBlock);
        tailExp.setFalseBlock(falseBlock);
        tailExp.buildIr();
    }

    public void traverse() {
        for(LAndExp lAndExp:lAndExps){
            lAndExp.traverse();
        }
    }
}
