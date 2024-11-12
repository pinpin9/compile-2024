package node;

import error.SemanticError;
import ir.GlobalVariable;
import ir.Value;
import ir.instructions.memory.Getelementptr;
import ir.instructions.memory.Load;
import ir.types.*;
import ir.types.constants.ConstArray;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import symbol.Symbol;
import token.Token;

// LVal → Ident ['[' Exp ']']
public class LVal extends Node{
    private Token ident;
    private Token lBrack;
    private Exp exp;
    private Token rBrack;

    public Token getIdent(){
        return ident;
    }
    public LVal(Token ident, Token lBrack, Exp exp, Token rBrack){
        super(NodeType.LVal);
        this.ident = ident;
        this.exp = exp;
        this.lBrack = lBrack;
        this.rBrack = rBrack;
    }

    @Override
    public void print() {
        ident.print();
        if(lBrack!=null){
            lBrack.print();
            exp.print();
            rBrack.print();
        }
        printType();
    }

    @Override
    public void buildIr() {
        String name = ident.getValue();
        Value value = stack.getSymbol(name);
        // 通常为add | sub | mul | sdiv | srem
        if(value.getValueType() instanceof IntType || value.getValueType() instanceof CharType){
            valueUp = value;
        } else {
            ValueType valueType = ((PointerType)(value.getValueType())).getPointingType();
            // 全局变量 | 局部变量
            if(valueType instanceof IntType || valueType instanceof CharType){ // 指针类型，指向int | char类型的值
                // 全局非数组变量且需要计算出值
                if(needCalExp && value instanceof GlobalVariable){
//                    Constant constant = ((GlobalVariable) value).getInitValue();
//                    if(constant instanceof ConstInt){
//                        valueUp = new ConstInt(((ConstInt)constant).getValue());
//                    } else{
//                        valueUp = new ConstChar(((ConstChar)constant).getValue());
//                    }
                    valueUp = ((GlobalVariable) value).getInitValue();
                } else { // 全局非数组变量，不需要计算出值 | alloca指令
                    if(lValAtLeft){ // 在左边，赋值语句
                        valueUp = value;
                    } else {
                        // 指针类型，在使用之前需要先加载Load
                        Load load = builder.buildLoad(curBlock, value);
                        valueUp = load;
                    }
                }
            } else if (valueType instanceof ArrayType) {
                if(exp!=null){
                    Boolean savedSate = lValAtLeft;
                    lValAtLeft = false;
                    exp.buildIr();
                    lValAtLeft = savedSate;
                }
                Value ptr = valueUp;
                // 全局数组变量且需要计算出值
                if(needCalExp && value instanceof GlobalVariable){
                    ConstArray constantArray = (ConstArray) ((GlobalVariable)value).getInitValue();
                    Constant constant = constantArray.getElementByIndex(((ConstInt)ptr).getValue());
                    valueUp = constant;
                } else{
                    Getelementptr addr = null;
                    if(exp!=null){
                        addr = builder.buildGetElementPtr(curBlock, value, new ConstInt(0), ptr);
                    }else {
                        addr = builder.buildGetElementPtr(curBlock, value, new ConstInt(0), new ConstInt(0));
                    }
                    valueUp = addr;
                    if(!lValAtLeft&&(!buildFuncRParams || (buildFuncRParams && exp!=null))){
                        valueUp = builder.buildLoad(curBlock, addr);
                    }
                }
            } else if (valueType instanceof PointerType){ // 函数数组形参，才会有双重指针类型
                if(exp!=null){
                    Boolean savedSate = lValAtLeft;
                    lValAtLeft = false;
                    exp.buildIr();
                    lValAtLeft = savedSate;
                }
                Value ptr = valueUp;
                // 需要先load
                Load load = builder.buildLoad(curBlock, value);
                valueUp = load;
                if(exp != null){ // 非指针式
                    Getelementptr addr = builder.buildGetElementPtr(curBlock, load, ptr);
                    valueUp = addr;
                    if(!lValAtLeft){ // 为右边的计算式子
                        Load load1 = builder.buildLoad(curBlock, addr);
                        valueUp = load1;
                    }
                }
            }
        }
    }

    public void traverse() {
        // 检查标识符是否存在
        SemanticError.checkSymbol(ident.getValue(), ident.getLineNum());
        if(exp!=null){
            exp.traverse();
        }
    }

    public String getVarType() {
        Symbol symbol = SemanticError.getSymbol(ident.getValue());
        String type = symbol.getType().toString();
        if(type.contains("Const")){
            type = type.substring(5);
        }
        if(exp!=null&&type.contains("Array")){
            type = type.replace("Array","");
        }
        return type;
    }
}
