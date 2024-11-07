package node;

import ir.Value;
import ir.instructions.binary.*;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import token.Token;

import java.util.List;

// MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
public class MulExp extends Node{
    private List<UnaryExp> unaryExps;
    private List<Token> ops;

    public MulExp(List<UnaryExp> unaryExps,List<Token> ops){
        super(NodeType.MulExp);
        this.unaryExps = unaryExps;
        this.ops = ops;
    }

    @Override
    public void print() {
        for(int i=0;i<unaryExps.size();i++){
            unaryExps.get(i).print();
            printType();
            if(i<ops.size()){
                ops.get(i).print();
            }
        }
    }

    @Override
    public void buildIr() {
        int mul = 0;
        if(needCalExp){ // 需要计算出值
            for(int i = 0; i < unaryExps.size(); i++){
                UnaryExp unaryExp = unaryExps.get(i);
                unaryExp.buildIr();
                Value value = valueUp;
                int tempVal = 0;
                if(value instanceof ConstInt){
                    tempVal = ((ConstInt)value).getValue();
                } else if (value instanceof ConstChar) {
                    tempVal = ((ConstChar)value).getValue();
                }
                if(i > 0){
                    Token op = ops.get(i - 1);
                    if(op.getType() == Token.TokenType.MULT){
                        mul *= tempVal;
                    } else if (op.getType() == Token.TokenType.DIV) {
                        mul /= tempVal;
                    } else if (op.getType() == Token.TokenType.MOD){
                        mul %= tempVal;
                    }
                } else {
                    mul = tempVal;
                }
                valueUp = new ConstInt(mul);
            }
        } else { // 不一定需要计算出值
            unaryExps.get(0).buildIr();
            Value value1 = valueUp;
            for(int i=1;i<unaryExps.size();i++){
                unaryExps.get(i).buildIr();
                Value value2 = valueUp;
                Token op = ops.get(i-1);
                if(value1 instanceof Constant && value2 instanceof Constant){
                    value1 = getResult(value1, value2, op);
                }else {
                    if(op.getType() == Token.TokenType.MULT){
                        Mul mult = builder.buildMul(curBlock, value1, value2);
                        value1 = mult;
                    } else if (op.getType() == Token.TokenType.DIV) {
                        Sdiv sdiv = builder.buildSdiv(curBlock, value1,value2);
                        value1 = sdiv;
                    } else {
                        Srem srem = builder.buildSrem(curBlock, value1, value2);
                        value1 = srem;
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
        if(op.getType() == Token.TokenType.MULT){
            return new ConstInt(val1*val2);
        } else if (op.getType() == Token.TokenType.DIV) {
            return new ConstInt(val1/val2);
        } else {
            return new ConstInt(val1%val2);
        }
    }

    public void traverse() {
        for(UnaryExp unaryExp:unaryExps){
            unaryExp.traverse();
        }
    }

    public String getVarType() {
        return unaryExps.get(0).getVarType();
    }
}
