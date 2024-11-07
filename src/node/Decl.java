package node;

import java.io.IOException;

// Decl → ConstDecl | VarDecl
public class Decl extends Node{
    private ConstDecl constDecl;
    private VarDecl varDecl;

    public Decl(ConstDecl constDecl, VarDecl varDecl){
        super(NodeType.Decl);
        this.constDecl = constDecl;
        this.varDecl = varDecl;
    }

    @Override
    public void print(){
        if(constDecl!=null){
            constDecl.print();
        }else{
            varDecl.print();
        }
    }

    @Override
    public void buildIr() {
        if(constDecl != null){
            constDecl.buildIr();
        }else {
            varDecl.buildIr();
        }
    }

    public void traverse() {
        if(constDecl!=null){
            constDecl.traverse();
        }else{
            varDecl.traverse();
        }
    }
}
