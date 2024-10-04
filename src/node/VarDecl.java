package node;

import token.Token;

import java.util.List;

// VarDecl → BType VarDef { ',' VarDef } ';'
public class VarDecl extends Node{
    private BType bType;
    private List<VarDef> varDefList;
    private List<Token> commas;
    private Token semicolon;

    public VarDecl(BType bType, List<VarDef> varDefList,List<Token> commas,Token semicolon){
        super(NodeType.VarDecl);
        this.bType = bType;
        this.varDefList = varDefList;
        this.commas = commas;
        this.semicolon = semicolon;
    }

    @Override
    public void print() {
        bType.print();
        for(int i=0;i<varDefList.size();i++){
            varDefList.get(i).print();
            if(i < commas.size()){
                commas.get(i).print();
            }
        }
        semicolon.print();
        printType();
    }
}
