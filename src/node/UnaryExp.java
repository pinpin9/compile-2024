package node;

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
}
