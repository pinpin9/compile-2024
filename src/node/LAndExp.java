package node;

import ir.BasicBlock;
import ir.instructions.binary.Icmp;
import ir.types.constants.ConstInt;
import token.Token;

import java.util.List;

// LAndExp → EqExp | LAndExp '&&' EqExp
public class LAndExp extends Node{
    private List<EqExp> eqExps;
    private List<Token> ops;

    private BasicBlock trueBlock = null;
    private BasicBlock falseBlock = null;

    public void setTrueBlock(BasicBlock trueBlock) {
        this.trueBlock = trueBlock;
    }

    public void setFalseBlock(BasicBlock falseBlock) {
        this.falseBlock = falseBlock;
    }

    public LAndExp(List<EqExp> eqExps, List<Token> ops){
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

    // EqExp {'&&' EqExp}
    @Override
    public void buildIr() {
        for(int i = 0;i<eqExps.size();i++){
            EqExp eqExp = eqExps.get(i);
            BasicBlock nextBlock;
            if(i<eqExps.size()-1){
                nextBlock = builder.buildBasicBlock(curFunc);
            }else{
                nextBlock = trueBlock;
            }

            singleCmp = true;
            eqExp.buildIr();

            if(singleCmp){
                // 如果只有单个表达式，比如if(a)，则需要先构建icmp表达式转为i1
                // 如果有多个表达式，则本身就返回Icmp语句
                if(valueUp.getValueType().isI1()||valueUp.getValueType().isChar()){
                    valueUp = builder.buildZext(curBlock, valueUp);
                }
                valueUp = builder.buildIcmp(Icmp.Cond.NE, curBlock, valueUp, new ConstInt(0));
            }
            builder.buildBr(curBlock, valueUp, nextBlock, falseBlock);

            curBlock = nextBlock;
        }
    }

    public void traverse() {
        for(EqExp eqExp:eqExps){
            eqExp.traverse();
        }
    }
}
