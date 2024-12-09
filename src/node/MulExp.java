package node;

import backend.opt.MulOptimizer;
import ir.instructions.binary.Mul;
import ir.instructions.binary.Sdiv;
import ir.values.Value;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import token.Token;
import tools.MipsMath;

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
                unaryExps.get(i).buildIr();
                Value value = valueUp;
                int tempVal = value instanceof ConstInt ? ((ConstInt)value).getValue() : ((ConstChar)value).getValue();
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
                        value1 = builder.buildMul(curBlock, value1, value2);
                    } else if (op.getType() == Token.TokenType.DIV) {
                        value1 = builder.buildSdiv(curBlock, value1,value2);
                    } else {
                        if (value2 instanceof Constant){
                            int num = value2 instanceof ConstInt ? ((ConstInt) value2).getValue() : ((ConstChar)value2).getValue();
                            if(num == 1){
                                value1 = builder.buildSrem(curBlock, value1, value2);
                            } else {
                                Sdiv x = builder.buildSdiv(curBlock, value1, value2);
                                Mul y = builder.buildMul(curBlock, x, value2);
                                value1 = builder.buildSub(curBlock, value1, y);
                            }
                        } else {
                            Sdiv x = builder.buildSdiv(curBlock, value1, value2);
                            Mul y = builder.buildMul(curBlock, x, value2);
                            value1 = builder.buildSub(curBlock, value1, y);
                        }
                    }
                }
            }
            valueUp = value1;
        }
    }

    private Constant getResult(Value value1, Value value2, Token op){
        int val1 = value1 instanceof ConstInt ? ((ConstInt) value1).getValue() : ((ConstChar)value1).getValue();
        int val2 = value2 instanceof ConstInt ? ((ConstInt) value2).getValue() : ((ConstChar)value2).getValue();
        if(op.getType() == Token.TokenType.MULT){
            return new ConstInt(val1*val2);
        } else if (op.getType() == Token.TokenType.DIV) {
            return new ConstInt(val1/val2);
        } else if (op.getType() == Token.TokenType.MOD){
            return new ConstInt(val1%val2);
        }
        return null;
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
