package node;

import ir.Value;
import ir.instructions.memory.Alloca;
import ir.instructions.memory.Store;
import symbol.Symbol;
import token.Token;

import java.util.ArrayList;
import java.util.List;
// FuncFParams → FuncFParam { ',' FuncFParam }
public class FuncFParams extends Node{
    private List<FuncFParam> funcFParams;
    private List<Token> commas;
    private List<Symbol> params = new ArrayList<>();

    public List<Symbol> getParams(){
        return params;
    }
    public FuncFParams(List<FuncFParam> funcFParams, List<Token> commas){
        super(NodeType.FuncFParams);
        this.funcFParams = funcFParams;
        this.commas = commas;
    }

    @Override
    public void print() {
        for(int i=0;i<funcFParams.size();i++){
            funcFParams.get(i).print();
            if(i<commas.size()){
                commas.get(i).print();
            }
        }
        printType();
    }

    public List<FuncFParam> getFuncFParams(){
        return funcFParams;
    }
    @Override
    public void buildIr() {
        for(FuncFParam funcFParam:funcFParams){
            funcFParam.buildIr();
            sysArgs.add(argType);
        }
    }

    public void traverse() {
        for(FuncFParam funcFParam:funcFParams){
            params.add(funcFParam.traverse());
        }
    }
}
