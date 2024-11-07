package node;

import error.SemanticError;
import ir.GlobalVariable;
import ir.Value;
import ir.instructions.memory.Alloca;
import ir.types.ArrayType;
import ir.types.CharType;
import ir.types.IntType;
import ir.types.ValueType;
import ir.types.constants.ConstArray;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import symbol.Symbol;
import token.Token;
import symbol.Symbol.SymbolType;

import java.util.ArrayList;
import java.util.List;

// 常量定义
// 一维数组或常数
// ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
public class ConstDef extends Node{
    private Token ident;
    private Token lBrack;
    private ConstExp constExp;
    private Token rBrack;
    private Token assign;
    private ConstInitVal constInitVal;
    private BType bType; // 存储变量的类型

    public ConstDef(Token ident, Token lBrack, ConstExp constExp, Token rBrack,Token assign, ConstInitVal constInitVal){
        super(NodeType.ConstDef);
        this.ident = ident;
        this.lBrack = lBrack;
        this.constExp = constExp;
        this.rBrack = rBrack;
        this.assign = assign;
        this.constInitVal = constInitVal;
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
        assign.print();
        constInitVal.print();
        printType();
    }

    @Override
    public void buildIr() {
        String name = ident.getValue();
        // 变量
        if(constExp == null){
            // 计算初始化值
            constInitVal.buildIr();
            if(stack.isGlobal()){ // 全局常量
                // 构建全局变量
                builder.buildGlobalVariable(name, getValueType(), true, (Constant) valueUp);
            }else { // 局部常量
                Alloca alloca = builder.buildAlloca(getValueType(), curBlock, new ConstArray(new ArrayList<>(){{
                    add((Constant) valueUp);
                }}));
//                builder.buildStore(curBlock, )
            }
        } else{ //数组
            constExp.buildIr();
            // 获取数组的长度
            int dim = ((ConstInt)valueUp).getValue();
            constInitVal.buildIr();
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
            builder.buildGlobalVariable(name,getValueType(dim), true, constArray);
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
        constInitVal.traverse();
    }

    private SymbolType getType(){
        if(lBrack==null){ // 非数组
            if (bType.getbType().getValue().equals("int")){
                return SymbolType.ConstInt;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.ConstChar;
            }
        }else { // 数组
            if (bType.getbType().getValue().equals("int")){
                return SymbolType.ConstIntArray;
            } else if (bType.getbType().getValue().equals("char")) {
                return SymbolType.ConstCharArray;
            }
        }
        return null;
    }
}
