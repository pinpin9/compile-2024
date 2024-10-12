package symbol;

import java.util.Stack;

import node.Node;
import symbol.Symbol.SymbolType;
public class SymbolStack {
    // 单例模式，使用方式：SymbolStack.getSymbolStack获取唯一实例，利用函数对唯一实例的栈变量进行操作
    private static SymbolStack symbolStack = new SymbolStack();
    public static SymbolStack getSymbolStack(){
        return symbolStack;
    }

    private Stack<SymbolTable> stack = new Stack<>();

    // 入栈，符号表开始
    public void push(SymbolTable symbolTable){
        if(!stack.isEmpty()){
            // 添加子节点信息
            current().addSon(symbolTable);
        }
        stack.push(symbolTable);
    }
    public void push(Node node,int scopeLevel){
        // 父节点信息
        push(new SymbolTable(current(),node,scopeLevel));
    }
    // 出栈，符号表结束
    public void pop(){
        stack.pop();
    }
    // 获取第一个符号表，但是不出栈
    public SymbolTable current(){
        if (stack.isEmpty()){
            return null;
        }
        return stack.peek();
    }
    // 获取当前符号表的作用域序号
    public int getScopeLevel(){
        return current().getScopeLevel();
    }
    // 添加符号，只能加入栈顶符号表
    public void addSymbol(Symbol symbol){
        current().addSymbol(symbol);
    }

    /*=======查找符号=========*/
    // 查找符号是否存在于栈顶符号表中，应用：声明时，判断符号名是否存在
    public boolean isInCurrent(String name){
        return current().containSymbol(name);
    }
    public boolean isInStack(String name){
        for(SymbolTable item:stack){
            if(item.containSymbol(name)){
                return true;
            }
        }
        return false;
    }

    // 查找符号是否存在于整个栈中，应用：检查名字
    public boolean isInStack(String name, SymbolType type){
        for(SymbolTable item:stack){
            if(item.containSymbol(name,type)){
                return true;
            }
        }
        return false;
    }
    // 返回第一个匹配的符号
    public Symbol getSymbol(String name, SymbolType type){
        for(SymbolTable item:stack){
            if(item.containSymbol(name,type)){
                return item.getSymbol(name,type);
            }
        }
        return null;
    }

    // 返回第一个匹配的符号
    public Symbol getSymbol(String name){
        for(SymbolTable item:stack){
            if(item.containSymbol(name)){
                return item.getSymbol(name);
            }
        }
        return null;
    }

    /*=====标志是否处于int/char函数中，对return的处理======*/
    private boolean isReturnFunc = false;

    public void inReturnFunc(){
        isReturnFunc = true;
    }
    public void leaveReturnFunc(){
        isReturnFunc = false;
    }
    public boolean isReturnFunc(){
        return isReturnFunc;
    }

    /*=====标志是否处于循环中，对break，Continue的处理=====*/
    private boolean isInLoop = false;
    public boolean isInLoop(){
        return isInLoop;
    }
    public void inLoop(){
        isInLoop = true;
    }
    public void leaveLoop(){
        isInLoop = false;
    }
}
