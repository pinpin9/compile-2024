package node;

import error.SemanticError;
import ir.values.Value;
import ir.instructions.memory.Alloca;
import ir.instructions.memory.Getelementptr;
import ir.types.*;
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
                    init = bType.getbType().getType() == Token.TokenType.INTTK ? new ConstInt(0) : new ConstChar(0);
                }else{
                    needCalExp = true;
                    initVal.buildIr();
                    needCalExp = false;
                    init = (Constant) valueUp;
                    if(bType.getbType().getType() == Token.TokenType.CHARTK){
                        init = new ConstChar(((ConstInt)init).getValue());
                    }
                }
                builder.buildGlobalVariable(name, false, init);
            } else { // 局部变量
                Alloca alloca = builder.buildAlloca(getValueType(), curBlock);
                // 符号表中增加该局部变量
                stack.addSymbol(name, alloca);
                // 得到的可能是load，add，getelementptr，constant等
                if(initVal!=null){
                    initVal.buildIr();
                    if(alloca.getAllocatedType() instanceof CharType && valueUp.getValueType() instanceof IntType){
                        if(valueUp instanceof ConstInt){
                            valueUp = new ConstChar(((ConstInt)valueUp).getValue());
                        }else{
                            valueUp = builder.buildTrunc(curBlock, valueUp);
                        }
                    } else if (alloca.getAllocatedType() instanceof IntType && valueUp.getValueType() instanceof CharType) {
                        if(valueUp instanceof ConstChar){
                            valueUp = new ConstInt(((ConstChar)valueUp).getValue());
                        } else{
                            valueUp = builder.buildZext(curBlock, valueUp);
                        }
                    }
                    builder.buildStore(curBlock, valueUp, alloca);
                }
            }
        }else{ // 数组
            constExp.buildIr();
            int dim = ((ConstInt)valueUp).getValue();
            if(stack.isGlobal()){ // 全局数组变量
                // 为读取当前初始化节点，清空综合属性上传的值
                if (valueUpList!=null){
                    valueUpList.clear();
                }
                // 初始化值，构建常量数组
                Constant constArray = null;
                if(initVal!=null){ // 有初始化
                    initVal.buildIr();
                    // 存储计算出来的数组初始化值
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
                    constArray = new ConstArray(constants);
                    ((ConstArray)constArray).setInitLen(valueUpList.size()); // 设置初始化值的个数
                } else { // 没有初始化，利用zeroInitializer进行初始化
                    constArray = new ZeroInitializer(new ArrayType(getValueType(), dim));
                }
                builder.buildGlobalVariable(name,false, constArray);
            } else { // 局部数组变量
                Alloca alloca = builder.buildAlloca(getValueType(dim), curBlock);
                stack.addSymbol(name, alloca);
                Getelementptr base = null;
                if(valueUpList != null){
                    valueUpList.clear();
                }
                if(initVal!=null){
                    initVal.buildIr();
                }
                if(valueUpList.size()>0){
                    base = builder.buildGetElementPtr(curBlock, alloca, new ConstInt(0), new ConstInt(0));
                }
                for(int i = 0; i < valueUpList.size(); i++){
                    Value addr = builder.buildGetElementPtr(curBlock, base, new ConstInt(i));
                    Value value = valueUpList.get(i);
                    if(((PointerType)addr.getValueType()).getPointingType() instanceof IntType && value.getValueType() instanceof CharType){
                        if(value instanceof ConstChar){
                            value = new ConstInt(((ConstChar)value).getValue());
                        }else{
                            value = builder.buildZext(curBlock, value);
                        }
                    } else if(((PointerType)addr.getValueType()).getPointingType() instanceof CharType && value.getValueType() instanceof IntType){
                        if(value instanceof ConstInt){
                            value = new ConstChar(((ConstInt)value).getValue());
                        }else{
                            value = builder.buildTrunc(curBlock, value);
                        }
                    }
                    builder.buildStore(curBlock, value, addr);
                }
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
