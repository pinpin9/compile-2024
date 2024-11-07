package node;

import token.Token;

import java.util.List;

// ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
public class ConstDecl extends Node{
    private Token constToken;
    private BType bType;
    private List<ConstDef> constDefList;
    private List<Token> commas;
    private Token semicolonToken;

    public ConstDecl(Token constToken,BType bType,List<ConstDef> constDefList,List<Token> commas,Token semicolonToken){
        super(NodeType.ConstDecl);
        this.constToken = constToken;
        this.bType = bType;
        this.constDefList = constDefList;
        this.commas = commas;
        this.semicolonToken = semicolonToken;
    }

    @Override
    public void print() {
        constToken.print();
        bType.print();
        for(int i=0;i<constDefList.size();i++){
            constDefList.get(i).print();
            if(i<commas.size()){
                commas.get(i).print();
            }
        }
        semicolonToken.print();
        printType();
    }

    @Override
    public void buildIr() {
        for(ConstDef constDef:constDefList){
            constDef.setBType(bType);
            constDef.buildIr();
        }
    }

    public void traverse() {
        for (ConstDef constDef:constDefList){
            constDef.traverse();
        }
    }
}
