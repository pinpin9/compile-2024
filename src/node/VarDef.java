package node;

import error.SemanticError;
import token.Token;
import symbol.Symbol.SymbolType;
// VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
public class VarDef extends Node{
    private Token ident;
    private Token lBrack;
    private ConstExp constExp;
    private Token rBrack;
    private Token assign;
    private InitVal initVal;
    private BType bType;

    public VarDef(Token ident, Token lBrack, ConstExp constExp,Token rBrack,Token assign,InitVal initVal){
        super(NodeType.VarDef);
        this.ident =ident;
        this.lBrack = lBrack;
        this.constExp = constExp;
        this.rBrack = rBrack;
        this.assign = assign;
        this.initVal = initVal;
    }

    public void setBType(BType bType){
        this.bType = bType;
    }

    @Override
    public void print() {
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            constExp.print();
            rBrack.print();
        }
        if(assign!=null){
            assign.print();
            initVal.print();
        }
        printType();
    }

    public void traverse() {
        SemanticError.addSymbol(ident.getValue(), getType(), ident.getLineNum(), this);
        if(constExp!=null){
            constExp.traverse();
        }
        if(initVal!=null){
            initVal.traverse();
        }
    }

    private SymbolType getType(){
        if(lBrack == null){ // 非数组
            if(bType.getbType().getValue().equals("int")){
                return SymbolType.Int;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.Char;
            }
        }else{ // 数组
            if(bType.getbType().getValue().equals("int")){
                return SymbolType.IntArray;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.CharArray;
            }
        }
        return null;
    }
}
