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
                    SemanticError.checkFuncParamsType(symbol);
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
}
