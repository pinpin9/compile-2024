package node;

import error.SemanticError;
import ir.BasicBlock;
import ir.Function;
import ir.IrSymbolTable;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.VoidType;
import symbol.Symbol;
import token.Token;

import java.util.ArrayList;

// MainFuncDef → 'int' 'main' '(' ')' Block
public class MainFuncDef extends Node{
    private Token intToken;
    private Token mainToken;
    private Token lParent;
    private Token rParent;
    private Block block;

    public MainFuncDef(Token intToken,Token mainToken,Token lParent,Token rParent,Block block){
        super(NodeType.MainFuncDef);
        this.intToken = intToken;
        this.mainToken = mainToken;
        this.lParent = lParent;
        this.rParent = rParent;
        this.block = block;
    }

    @Override
    public void print() {
        intToken.print();
        mainToken.print();
        lParent.print();
        rParent.print();
        block.print();
        printType();
    }

    @Override
    public void buildIr() {
        Function mainFunc = builder.buildFunction(false, "main", new IntType(32), new ArrayList<>());
        curFunc = mainFunc;
        // 新建一个符号表入栈
        stack.push(new IrSymbolTable());
        // 构建基本块
        BasicBlock basicBlock = builder.buildBasicBlock(curFunc);
        curBlock = basicBlock;
        block.buildIr();

        // 当前block构建完成
        stack.pop();
    }

    public void traverse() {
        SemanticError.inReturnFunc();
        SemanticError.checkReturn(block);
        SemanticError.addTable(this);
        block.traverse();
        SemanticError.popTable();
        SemanticError.leaveReturnFunc();
    }
}
