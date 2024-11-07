package node;

import error.SemanticError;
import ir.instructions.memory.Alloca;
import ir.instructions.memory.Store;
import ir.types.ArrayType;
import ir.types.CharType;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.constants.*;
import token.Token;
import symbol.Symbol.SymbolType;

import java.util.ArrayList;

// VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
public class VarDef extends Node{
    private Token ident;
    private Token lBrack;
    private ConstExp constExp;
    private Token rBrack;
    private Token assign;
    private InitVal initVal;
    private BType bType;

    public VarDef(Token ident, Token lBrack, ConstExp constExp,Token rBrack,Token assign,InitVal initVal){
        super(NodeType.VarDef);
        this.ident =ident;
        this.lBrack = lBrack;
        this.constExp = constExp;
        this.rBrack = rBrack;
        this.assign = assign;
        this.initVal = initVal;
    }

    public void setBType(BType bType){
        this.bType = bType;
    }

    @Override
    public void print() {
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            constExp.print();
            rBrack.print();
        }
        if(assign!=null){
            assign.print();
            initVal.print();
        }
        printType();
    }

    @Override
    public void buildIr() {
        String name = ident.getValue();
        if(constExp == null){ // 非数组
            // 全局变量的值都是可以计算出来的
            if(stack.isGlobal()){ // 全局变量
                Constant init = null;
                if(initVal == null){
                    if(bType.getbType().getType()== Token.TokenType.INTTK){
                        init = new ConstInt(0);
                    } else{
                        init = new ConstChar(0);
                    }
                }else{
                    needCalExp = true;
                    initVal.buildIr();
                    needCalExp = false;
                    init = (Constant) valueUp;
                }
                builder.buildGlobalVariable(name, getValueType(), false, init);
            } else{ // 局部变量
                Alloca alloca = builder.buildAlloca(getValueType(), curBlock);
                // 符号表中增加该局部变量
                stack.addSymbol(name, alloca);
                if(initVal!=null){
                    initVal.buildIr();
                    Store store = builder.buildStore(curBlock, valueUp, alloca);
                }

            }
        }else{ // 数组
            constExp.buildIr();
            int dim = ((ConstInt)valueUp).getValue();
            if(stack.isGlobal()){
                // 为读取当前初始化节点，清空综合属性上传的值
                if (valueUpList!=null){
                    valueUpList.clear();
                }
                if(initVal!=null){
                    initVal.buildIr();
                }
                ArrayList<Constant> constants = new ArrayList<>();
                for(int i = 0; i<dim;i++){
                    if(i<valueUpList.size()){
                        if(isChar()){
                            constants.add(new ConstChar(((ConstInt)valueUpList.get(i)).getValue()));
                        }else {
                            constants.add((Constant) valueUpList.get(i));
                        }
                    }else{
                        if(isChar()){
                            constants.add(new ConstChar(0));
                        }else {
                            constants.add(new ConstInt(0));
                        }
                    }
                }
                ConstArray constArray = new ConstArray(constants);
                builder.buildGlobalVariable(name,getValueType(dim), false, constArray);
            }



        }
    }

    private boolean isChar(){
        return bType.getbType().getType()== Token.TokenType.CHARTK;
    }

    private ValueType getValueType(){
        switch (bType.getbType().getValue()){
            case "int" ->{
                return new IntType(32);
            }
            case "char" ->{
                return new CharType();
            }
        }
        return null;
    }
    private ValueType getValueType(int dim){
        switch (bType.getbType().getValue()){
            case "int" ->{
                return new ArrayType(new IntType(32), dim);
            }
            case "char" ->{
                return new ArrayType(new CharType(), dim);
            }
        }
        return null;
    }

    public void traverse() {
        SemanticError.addSymbol(ident.getValue(), getType(), ident.getLineNum(), this);
        if(constExp!=null){
            constExp.traverse();
        }
        if(initVal!=null){
            initVal.traverse();
        }
    }

    private SymbolType getType(){
        if(lBrack == null){ // 非数组
            if(bType.getbType().getValue().equals("int")){
                return SymbolType.Int;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.Char;
            }
        }else{ // 数组
            if(bType.getbType().getValue().equals("int")){
                return SymbolType.IntArray;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.CharArray;
            }
        }
        return null;
    }
}
