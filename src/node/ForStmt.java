package node;

import error.SemanticError;
import ir.Value;
import ir.types.CharType;
import ir.types.IntType;
import ir.types.PointerType;
import token.Token;

// ForStmt → LVal '=' Exp
public class ForStmt extends Node{
    private LVal lVal;
    private Token assign;
    private Exp exp;

    public ForStmt(LVal lVal, Token assign, Exp exp){
        super(NodeType.ForStmt);
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
    }

    @Override
    public void print() {
        lVal.print();
        assign.print();
        exp.print();
        printType();
    }

    @Override
    public void buildIr() {
        lValAtLeft = true;
        lVal.buildIr();
        lValAtLeft = false;
        Value lValue = valueUp;
        exp.buildIr();
        Value value = valueUp;
        if(((PointerType)lValue.getValueType()).getPointingType() instanceof CharType && value.getValueType() instanceof IntType){
            value = builder.buildTrunc(curBlock, value);
        } else if (((PointerType)lValue.getValueType()).getPointingType() instanceof IntType && value.getValueType() instanceof CharType) {
            value = builder.buildZext(curBlock, value);
        }
        builder.buildStore(curBlock, value, lValue);
    }

    public void traverse() {
        lVal.traverse();
        SemanticError.checkChangeConst(lVal.getIdent().getValue(), lVal.getIdent().getLineNum());
        exp.traverse();
    }
}
