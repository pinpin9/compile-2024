package ir;

import ir.types.ValueType;

import java.util.HashMap;

public class IrSymbolTable {
    // 全局符号表
    public static final IrSymbolTable globalSymbolTable = new IrSymbolTable();

    private HashMap<String,Value> symbolTable = new HashMap<>();
    public void addSymbol(String name,Value value){
        symbolTable.put(name, value);
    }
    public Value getSymbol(String name){
        if(symbolTable.containsKey(name)){
            return symbolTable.get(name);
        }
        return null;
    }
    public boolean containSymbol(String name){
        return symbolTable.containsKey(name);
    }
}
