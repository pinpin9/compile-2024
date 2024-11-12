package node;

import ir.Function;
import ir.IrSymbolStack;
import ir.types.CharType;
import ir.types.IntType;
import ir.types.PointerType;
import ir.types.VoidType;

import java.io.IOException;
import java.util.ArrayList;
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

    public void traverse() {
        for(Decl declItem:decl){
            declItem.traverse();
        }
        for(FuncDef funcDefItem:funcDef){
            funcDefItem.traverse();
        }
        mainFuncDef.traverse();
    }

    @Override
    public void buildIr() {
        // 初始化全局的符号表
        stack.init();
        // 构建链接函数
        Function.getint = builder.buildFunction(true, "getint", new IntType(32), new ArrayList<>());
        Function.getchar = builder.buildFunction(true, "getchar", new IntType(32), new ArrayList<>());
        Function.putint = builder.buildFunction(true, "putint", new VoidType(), new ArrayList<>(){{
            add(new IntType(32));
        }});
        Function.putchar = builder.buildFunction(true, "putch", new VoidType(), new ArrayList<>(){{
            add(new CharType());
        }});
        Function.putstr = builder.buildFunction(true, "putstr", new VoidType(), new ArrayList<>(){{
            add(new PointerType(new CharType()));
        }});
        for(Decl declItem:decl){
            declItem.buildIr();
        }
        for(FuncDef funcDefItem:funcDef){
            funcDefItem.buildIr();
        }
        mainFuncDef.buildIr();
    }
}
