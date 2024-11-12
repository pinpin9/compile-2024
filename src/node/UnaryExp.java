package node;

import error.SemanticError;
import ir.Function;
import ir.Value;
import ir.instructions.Call;
import ir.instructions.binary.Icmp;
import ir.instructions.binary.Sub;
import ir.types.CharType;
import ir.types.IntType;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import symbol.Symbol;
import token.Token;

import java.util.ArrayList;
import java.util.List;

// UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
public class UnaryExp extends Node{
    private PrimaryExp primaryExp = null;
    private Token ident = null;
    private Token lParent = null;
    private FuncRParams funcRParams = null;
    private Token rParent = null;
    private UnaryOp unaryOp = null;
    private UnaryExp unaryExp = null;
    public UnaryExp(PrimaryExp primaryExp){
        super(NodeType.UnaryExp);
        this.primaryExp = primaryExp;
    }
    public UnaryExp(Token ident,Token lParent,FuncRParams funcRParams,Token rParent){
        super(NodeType.UnaryExp);
        this.ident = ident;
        this.lParent = lParent;
        this.funcRParams = funcRParams;
        this.rParent = rParent;
    }
    public UnaryExp(UnaryOp unaryOp,UnaryExp unaryExp){
        super(NodeType.UnaryExp);
        this.unaryOp = unaryOp;
        this.unaryExp = unaryExp;
    }

    @Override
    public void print() {
        if(primaryExp!=null){
            primaryExp.print();
        } else if (unaryOp!=null) {
            unaryOp.print();
            unaryExp.print();
        } else {
          ident.print();
          lParent.print();
          if(funcRParams!=null){
              funcRParams.print();
          }
          rParent.print();
        }
        printType();
    }

    @Override
    public void buildIr() {
        if(needCalExp){ // 计算初值
             if (unaryExp != null) {
                unaryExp.buildIr();
                Value value = valueUp;
                if(unaryOp.getOp().getType()== Token.TokenType.MINU){
                    valueUp = getSubConstant(value);
                } else if (unaryOp.getOp().getType() == Token.TokenType.NOT) {
                    int num = 0;
                    if(value instanceof ConstInt){
                        num = ((ConstInt)value).getValue();
                    } else if (value instanceof ConstChar) {
                        num = ((ConstChar)value).getValue();
                    }
                    num = num==0 ? 1:0;
                    valueUp = new ConstInt(num);
                }
            } else if (primaryExp != null) {
                 primaryExp.buildIr();
            }
        }else{ // 不一定能计算出初值
            if (unaryExp!=null) {
                unaryExp.buildIr();
                Value value = valueUp;
                // '-' | '!' UnaryExp
                if(unaryOp.getOp().getType() == Token.TokenType.MINU){ // -
                    if(value instanceof Constant){
                        valueUp = getSubConstant(value);
                    } else {
                        Sub sub = builder.buildSub(curBlock, new ConstInt(0), valueUp);
                        valueUp = sub;
                    }
                } else if(unaryOp.getOp().getType() == Token.TokenType.NOT){ //
                    if(value.getValueType().isChar() || value.getValueType().isI1()){
                        value = builder.buildZext(curBlock, value);
                    }
                    valueUp = builder.buildIcmp(Icmp.Cond.EQ, curBlock, ConstInt.ZERO, value);
                    valueUp = builder.buildZext(curBlock, valueUp);
                }
            } else if (primaryExp != null) {
                primaryExp.buildIr();
            } else {
                // 函数名称
                String name = ident.getValue();
                Value function = stack.getSymbol(name);
                if(funcRParams != null){
                    buildFuncRParams = true;
                    funcRParams.buildIr();
                    buildFuncRParams = false;
                }
                List<Value> params = new ArrayList<>();
                for(int i=((Function)function).getArgsCnt()-1;i>=0;i--){
                    Value value = funcParams.pop();
                    params.add(0,value);
                }
                Call call = builder.buildCall(curBlock, (Function) function, params);
                valueUp = call;
            }
        }
    }

    private Constant getSubConstant(Value value){
        if(value instanceof ConstInt){
            return new ConstInt(-((ConstInt)value).getValue());
        } else if (value instanceof ConstChar) {
            return new ConstInt(-((ConstChar)value).getValue());
        }
        return null;
    }

    public void traverse() {
        if(primaryExp!=null){
            primaryExp.traverse();
        } else if (unaryExp!=null) {
            unaryExp.traverse();
        } else {
            // 标识符是否存在
            Symbol symbol = SemanticError.checkSymbol(ident.getValue(), ident.getLineNum());
            // 函数参数个数是否匹配
            Boolean isTrueCount = false;
            if(symbol != null){
                isTrueCount = SemanticError.checkFuncParamCount(symbol, getParamsCount(), ident.getLineNum());
            }
            // 参数类型是否匹配
            if(funcRParams!=null){
                // 函数参数类型是否匹配
                if(isTrueCount){
                    SemanticError.checkFuncParamsType(symbol,funcRParams.getExpList(), ident.getLineNum());
                }
                funcRParams.traverse();
            }
        }
    }
    public int getParamsCount(){
        if(funcRParams!=null){
            return funcRParams.getParamsCount();
        }else {
            return 0;
        }
    }

    public String getVarType(){
        if(primaryExp!=null){
            return primaryExp.getVarType();
        } else if (unaryOp!=null) {
            return unaryExp.getVarType();
        } else {
            // 此处先不检查参数的问题，仅根据函数的返回类型进行判断
            Symbol symbol = SemanticError.stack.getSymbol(ident.getValue());
            // 函数不存在不报错 在后续检测时再报错
            if(symbol==null){
                return null;
            }
            if(symbol.getType()== Symbol.SymbolType.CharFunc){
                return "Char";
            } else if (symbol.getType()== Symbol.SymbolType.IntFunc) {
                return "Int";
            }
            return null;
        }
    }
}
