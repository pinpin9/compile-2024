package node;

import frontend.ParserAnalyze;
import tools.IO;

import java.io.IOException;

public abstract class Node {
    public enum NodeType{
        AddExp,
        Block,
        BlockItem,
        BType,
        Character,
        CompUnit,
        Cond,
        ConstDecl,
        ConstDef,
        ConstExp,
        ConstInitVal,
        Decl,
        EqExp,
        Exp,
        ForStmt,
        FuncDef,
        FuncFParam,
        FuncFParams,
        FuncRParams,
        FuncType,
        InitVal,
        LAndExp,
        LOrExp,
        LVal,
        MainFuncDef,
        MulExp,
        Number,
        PrimaryExp,
        RelExp,
        Stmt,
        UnaryExp,
        UnaryOp,
        VarDecl,
        VarDef
    }
    public NodeType type;

    public Node(NodeType nodeType) {
        type = nodeType;
    }

    public abstract void print();

    public void printType() {
        System.out.println(type.toString());
        ParserAnalyze.parserOutput.output("<"+type.toString()+">");
    }
}
