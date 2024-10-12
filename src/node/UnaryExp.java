package node;

import error.SemanticError;
import symbol.Symbol;
import token.Token;

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
