package error;

import node.*;
import symbol.Symbol;
import symbol.SymbolStack;
import symbol.Symbol.SymbolType;

import java.util.List;

// 这个是一个处理符号栈的方法类，进行push，pop和添加符号的操作
public class SemanticError {
    public static SymbolStack stack = SymbolStack.getSymbolStack();
    private static ErrorHandler errorHandler = ErrorHandler.getErrorHandler();
    // 全局变量记录作用域序号，从1开始
    public static int scopeLevel = 1;

    /*========添加符号========*/
    // 符号作用域序号与当前符号表作用域序号相等，函数符号
    public static Symbol addSymbol(String name, SymbolType type, int lineNum, Node node, FuncType funcType, List<Symbol> funcParams){
        if(stack.isInCurrent(name)){
            errorHandler.addError(new Error(Error.ErrorType.b,lineNum));
            return null;
        }else{
            Symbol symbol = new Symbol(name,type,stack.getScopeLevel(),lineNum,node,funcType,funcParams);
            stack.addSymbol(symbol);
            return symbol;
        }
    }
    // 变量符号
    public static Symbol addSymbol(String name, SymbolType type, int lineNum, Node node){
        if(stack.isInCurrent(name)){
            errorHandler.addError(new Error(Error.ErrorType.b,lineNum));
            return null;
        }else{
            Symbol symbol = new Symbol(name,type,stack.getScopeLevel(),lineNum,node);
            stack.addSymbol(symbol);
            return symbol;
        }
    }

    /*=======符号表入栈/出栈=========*/
    public static void addTable(Node node){
        scopeLevel++; // 添加符号表之前先将作用域序号+1
        stack.push(node,scopeLevel);
    }
    public static void popTable(){
        stack.pop();
    }

    /*=======查找符号是否在符号栈中=======*/
    public static Symbol checkSymbol(String name, int lineNum){
        if(!stack.isInStack(name)){
            errorHandler.addError(new Error(Error.ErrorType.c,lineNum));
            return null;
        }
        return stack.getSymbol(name);
    }

    // 检查函数参数的个数是否相等，d错误
    public static boolean checkFuncParamCount(Symbol symbol, int count,int lineNum){
        if(symbol.getParamsCount()!=count){
            errorHandler.addError(new Error(Error.ErrorType.d,lineNum));
            return false;
        }
        return true;
    }

    // 检查函数参数的类型是否相同，e错误
    public static void checkFuncParamsType(String name){

    }


    // 检查是否改变常量
    public static void checkChangeConst(String name,int lineNum){
        Symbol symbol = stack.getSymbol(name);
        String type = symbol.getType().toString();
        if(type.contains("Const")){
            errorHandler.addError(new Error(Error.ErrorType.h, lineNum));
        }
    }

    /*=======处理函数返回异常=========*/
    // 进入void函数
    public static void inReturnFunc(){
        stack.inReturnFunc();
    }
    public static void leaveReturnFunc(){
        stack.leaveReturnFunc();
    }

    // 无返回值的函数存在不匹配的return语句
    public static void checkReturn(int lineNum){
        if(!stack.isReturnFunc()){
            errorHandler.addError(new Error(Error.ErrorType.f,lineNum));
        }
    }

    // 需要返回值的函数缺少return语句
    public static void checkReturn(Block block){
        List<BlockItem> blockItemList = block.getBlockItemList();
        if(blockItemList!=null){
            BlockItem blockItem = blockItemList.get(blockItemList.size()-1);
            Stmt stmt = blockItem.getStmt();
            if(stmt!=null&&stmt.getType()== Stmt.StmtType.RETURN){
                return;
            }
        }
        errorHandler.addError(new Error(Error.ErrorType.g,block.getrBrace().getLineNum()));
    }

    /*========对循环的处理，break | continue=======*/
    public static void inLoop(){
        stack.inLoop();
    }
    public static void leaveLoop(){
        stack.leaveLoop();
    }
    public static void checkLoop(int lineNum){
        if (!stack.isInLoop()){
            errorHandler.addError(new Error(Error.ErrorType.m,lineNum));
        }
    }
    public static void checkFormat(String string, int count,int lineNum){
        int formatCount = 0;
        for(int i=0,j=1;j<string.length();i++,j++){
            if(string.charAt(i)=='%'&&(string.charAt(j)=='d'||string.charAt(j)=='c')){
                formatCount++;
            }
        }
        if(formatCount!=count){
            errorHandler.addError(new Error(Error.ErrorType.l,lineNum));
        }
    }
}
