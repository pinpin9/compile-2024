package frontend;

import error.ErrorHandler;
import error.SemanticError;
import node.CompUnit;
import settings.Settings;
import symbol.SymbolStack;
import symbol.SymbolTable;
import tools.IO;

import java.util.Stack;

public class SemanticAnalyze {
    private static SemanticAnalyze instance = new SemanticAnalyze();
    public static SemanticAnalyze getInstance() {
        return instance;
    }
    private CompUnit compUnit;
    // 错误处理
    private ErrorHandler errorHandler = ErrorHandler.getErrorHandler();
    // 输出符号表
    public static IO semanticOutput = new IO(Settings.semanticFile);


    // 根符号表
    private static SymbolTable rootSymbolTable;
    public void analyze(CompUnit compUnit){
        this.compUnit = compUnit;
        rootSymbolTable = new SymbolTable(null,compUnit, SemanticError.scopeLevel);
        SymbolStack.getSymbolStack().push(rootSymbolTable);
        compUnit.traverse();
    }
    public void print(){
        rootSymbolTable.print();
    }
}
