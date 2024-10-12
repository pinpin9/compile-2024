package symbol;

import frontend.SemanticAnalyze;
import node.FuncType;
import node.Node;

import java.util.ArrayList;
import java.util.List;

public class Symbol {
    // type不仅说明是变量还是函数，也声明了变量是否为数组
    public enum SymbolType{
        ConstChar, ConstInt, ConstCharArray, ConstIntArray,
        Char, Int, CharArray, IntArray,
        VoidFunc, CharFunc, IntFunc
    }
    private String name; //名称
    private SymbolType type; //类型
    private int scopeLevel = 0; //作用范围
    private int lineNum = 0; //声明行号
    private Node node; //声明的结点
    // 函数
    private FuncType funcType = null; //返回类型
    private List<Symbol> funcParams = new ArrayList<>(); //参数列表


    public Symbol(String name, SymbolType type, int scopeLevel, int lineNum, Node node, FuncType funcType, List<Symbol> funcParams){ // 声明函数
        this.name = name;
        this.type = type;
        this.scopeLevel = scopeLevel;
        this.funcParams = funcParams;
        this.lineNum = lineNum;
        this.node = node;
        this.funcType = funcType;
    }

    public Symbol(String name, SymbolType type, int scopeLevel, int lineNum, Node node){
        this.name = name;
        this.type = type;
        this.scopeLevel = scopeLevel;
        this.lineNum = lineNum;
        this.node = node;
    }

    public int getDim(){
        if(type.toString().contains("Array")){
            return 1;
        }
        return 0;
    }
    public void setFuncParams(List<Symbol> funcParams) {
        this.funcParams = funcParams;
    }

    public String getName() {
        return name;
    }

    public SymbolType getType() {
        return type;
    }

    public int getScopeLevel() {
        return scopeLevel;
    }

    public int getLineNum() {
        return lineNum;
    }

    public Node getNode() {
        return node;
    }

    public FuncType getFuncType() {
        return funcType;
    }

    public List<Symbol> getFuncParams() {
        return funcParams;
    }

    public int getParamsCount(){
        return funcParams.size();
    }
    public void print(){
        SemanticAnalyze.semanticOutput.output(scopeLevel + " " + name + " " + type);
    }
}
