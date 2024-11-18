package node;

import ir.Value;
import ir.instructions.binary.Add;
import ir.instructions.binary.Sub;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
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

    public void traverse() {
        for(int i=0;i<mulExps.size();i++){
            mulExps.get(i).traverse();
        }
    }

    public String getVarType(){
        return mulExps.get(0).getVarType();
    }

    // MulExp {('+' | '-') MulExp}
    @Override
    public void buildIr() {
        if(needCalExp){ // 需要计算出值
            int sum = 0;
            for(int i = 0;i<mulExps.size();i++){
                MulExp mulExp = mulExps.get(i);
                mulExp.buildIr();
                Value value = valueUp;
                int tempVal = 0;
                if(value instanceof ConstInt){
                    tempVal = ((ConstInt)value).getValue();
                } else if (value instanceof ConstChar){
                    tempVal = ((ConstChar)value).getValue();
                }
                if(i>0){
                    Token op = ops.get(i-1);
                    if(op.getType()== Token.TokenType.PLUS){
                        sum += tempVal;
                    } else if (op.getType() == Token.TokenType.MINU) {
                        sum -= tempVal;
                    }
                } else {
                    sum += tempVal;
                }
            }
            valueUp = new ConstInt(sum);
        }else { // 不一定能够计算出值，但是如果两端都是常数，可以直接计算出来
            mulExps.get(0).buildIr();
            Value value1 = valueUp;
            for(int i = 1; i<mulExps.size(); i++){
                mulExps.get(i).buildIr();
                Value value2 = valueUp;
                Token op = ops.get(i-1);
                if(value1 instanceof Constant && value2 instanceof Constant){
                    value1 = getResult(value1, value2, op);
                }else {
                    if(op.getType() == Token.TokenType.PLUS){
                        value1 = builder.buildAdd(curBlock, value1, value2);
                    } else {
                        value1 = builder.buildSub(curBlock, value1, value2);
                    }
                }
            }
            valueUp = value1;
        }
    }

    private Constant getResult(Value value1, Value value2, Token op){
        int val1, val2;
        if(value1 instanceof ConstInt){
            val1=((ConstInt) value1).getValue();
        }else{
            val1 = ((ConstChar)value1).getValue();
        }
        if(value2 instanceof ConstInt){
            val2=((ConstInt) value2).getValue();
        }else{
            val2 = ((ConstChar)value2).getValue();
        }
        if(op.getType() == Token.TokenType.PLUS){
            return new ConstInt(val1+val2);
        }else {
            return new ConstInt(val1-val2);
        }
    }
}

