package symbol;

import node.Node;

import java.util.*;

import symbol.Symbol.SymbolType;
// 符号表
public class SymbolTable {
    private Map<String,Symbol> symbolMap = new LinkedHashMap<>();
    public SymbolTable fatherSymbolTable; // 父符号表
    private List<SymbolTable> sonSymbolTable = new ArrayList<>(); // 子符号表
    private Node node; //因为程序块对应一个Node
    private int scopeLevel; // 符号表存储当前的作用域序号

    public SymbolTable(SymbolTable fatherSymbolTable, Node node, int scopeLevel){
        this.fatherSymbolTable = fatherSymbolTable;
        this.node = node;
        this.scopeLevel = scopeLevel;
    }

    public int getScopeLevel(){
        return scopeLevel;
    }

    public void setNode(Node node){
        this.node = node;
    }

    // 增加一个符号
    public void addSymbol(Symbol symbol){
        symbolMap.put(symbol.getName(), symbol);
    }

    // 判断符号是否存在于当前符号表中
    public boolean containSymbol(String name){
        return symbolMap.containsKey(name);
    }

    // 判断给定名称和类型的符号是否存在于当前符号表中，强类型
    public boolean containSymbol(String name, SymbolType type){
        Symbol symbol = getSymbol(name);
        return symbol != null && symbol.getType() == type;
    }

    // 根据名称获取某个符号
    public Symbol getSymbol(String name){
        if(containSymbol(name)){
            return symbolMap.get(name);
        }
        return null;
    }

    // 根据名称和类型获取某个符号
    public Symbol getSymbol(String name, SymbolType type){
        if(containSymbol(name,type)){
            return symbolMap.get(name);
        }
        return null;
    }

    // 添加子符号表
    public void addSon(SymbolTable symbolTable){
        sonSymbolTable.add(symbolTable);
    }

    public void print(){
        for(Map.Entry<String, Symbol> entry:symbolMap.entrySet()){
            entry.getValue().print();
        }
        for(SymbolTable symbolTable: sonSymbolTable){
            symbolTable.print();
        }
    }
}
