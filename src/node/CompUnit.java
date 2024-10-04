package node;

import java.io.IOException;
import java.util.List;

// CompUnit → {Decl} {FuncDef} MainFuncDef
public class CompUnit extends Node{
    private List<Decl> decl;
    private List<FuncDef> funcDef;
    private MainFuncDef mainFuncDef;

    public CompUnit(List<Decl> decl,List<FuncDef> funcDef, MainFuncDef mainFuncDef){
        super(NodeType.CompUnit);
        this.decl = decl;
        this.funcDef = funcDef;
        this.mainFuncDef = mainFuncDef;
    }

    @Override
    public void print(){
        for(Decl declItem:decl){
            declItem.print();
        }
        for(FuncDef funcDefItem:funcDef){
            funcDefItem.print();
        }
        mainFuncDef.print();
        printType();
    }
}
