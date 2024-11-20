package ir;

import ir.values.Value;

import java.util.Stack;

/**
 * @author zlp
 * @Discription 符号栈
 * @date 2024/10/31
 */
public class IrSymbolStack {
    // 只有一个栈，单例模式
    private static IrSymbolStack irSymbolStack = new IrSymbolStack();
    public static IrSymbolStack getInstance(){
        return irSymbolStack;
    }
    private final Stack<IrSymbolTable> stack = new Stack<>();
    private IrSymbolTable globalSymbolTable = IrSymbolTable.globalSymbolTable;
    // 初始化符号栈
    public void init(){
        stack.clear();
        stack.push(IrSymbolTable.globalSymbolTable);
    }
    // 符号表入栈
    public void push(IrSymbolTable irSymbolTable){
        stack.push(irSymbolTable);
    }
    // 符号栈出栈
    public void pop(){
        stack.pop();
    }
    // 向栈顶添加元素
    public void addSymbol(String name, Value value){
        stack.peek().addSymbol(name, value);
    }

    // 元素是否存在于栈中
    public boolean containSymbol(String name){
        for(int i=stack.size();i>=0;i--){
            if(stack.get(i).containSymbol(name)){
                return true;
            }
        }
        return false;
    }
    // 获取指定元素
    public Value getSymbol(String name){
        for(int i=stack.size()-1;i>=0;i--){
            if(stack.get(i).containSymbol(name)){
                return stack.get(i).getSymbol(name);
            }
        }
        return null;
    }

    public void addSymbolToGlobal(String name, Value value){
        globalSymbolTable.addSymbol(name, value);
    }
    // 确认函数定义在全局符号表中
    public Value getSymbolFromGlobal(String name){
        return IrSymbolTable.globalSymbolTable.getSymbol(name);
    }
    public boolean isGlobal(){
        return stack.size()==1;
    }
    public int getSize(){
        return stack.size();
    }
}
