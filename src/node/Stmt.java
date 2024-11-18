package node;

import error.SemanticError;
import ir.*;
import ir.instructions.Call;
import ir.instructions.Trunc;
import ir.instructions.Zext;
import ir.instructions.memory.Getelementptr;
import ir.types.*;
import ir.types.constants.ConstInt;
import token.Token;
import tools.StrTool;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/*
语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
| [Exp] ';' //有无Exp两种情况
| Block
| 'if' '(' Cond ')' Stmt [ 'else' Stmt ] // 1.有else 2.无else
| 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt // 1. 无缺省，1种情况 2.
ForStmt与Cond中缺省一个，3种情况 3. ForStmt与Cond中缺省两个，3种情况 4. ForStmt与Cond全部
缺省，1种情况
| 'break' ';' | 'continue' ';'
| 'return' [Exp] ';' // 1.有Exp 2.无Exp
| LVal '=' 'getint''('')'';'
| LVal '=' 'getchar''('')'';'
| 'printf''('StringConst {','Exp}')'';' // 1.有Exp 2.无Exp
 */
public class Stmt extends Node{
    public enum StmtType{
        LVALASSIGN, // LVal '=' Exp ';'
        EXP,        // [Exp] ';'
        Block,
        IF,
        FOR,
        BREAK,
        CONTINUE,
        RETURN,
        PRINTF,
        LVALGETINT, // LVal '=' 'getint''('')'';'
        LVALGETCHAR;
    }
    private StmtType type;
    private LVal lVal = null;
    private Token assign = null;
    private Exp exp = null;
    private Token semicolonToken = null;

    private Block block = null;

    private Token ifToken = null;
    private Token lParent = null;
    private Cond cond = null;
    private Token rParent = null;
    private Stmt stmt1 = null;
    private Token elseToken = null;
    private Stmt stmt2 = null;

    private Token forToken = null;
    private ForStmt forStmt1 = null;
    private List<Token> semiColonList = new ArrayList<>();
    private ForStmt forStmt2 = null;

    private Token breakOrContinue = null;

    private Token returnToken = null;

    private Token getIntToken = null;
    private Token getCharToken = null;

    private Token printfToken = null;
    private Token stringConst = null;
    private List<Token> commas = new ArrayList<>();
    private List<Exp> expList = new ArrayList<>();

    //=========中间代码生成=========//
    private ValueType returnType;
    public void setRtnType(ValueType valueType){
        returnType = valueType;
    }

    public StmtType getType(){
        return type;
    }


    public Stmt(StmtType type, LVal lVal, Token assign, Exp exp, Token semicolonToken){
        super(NodeType.Stmt);
        this.type = type;
        this.lVal = lVal;
        this.assign = assign;
        this.exp = exp;
        this.semicolonToken = semicolonToken;
    }

    public Stmt(StmtType type,Exp exp,Token semicolonToken){
        super(NodeType.Stmt);
        this.type = type;
        this.exp =exp;
        this.semicolonToken = semicolonToken;
    }

    public Stmt(StmtType type,Block block){
        super(NodeType.Stmt);
        this.type = type;
        this.block = block;
    }

    // 'if' '(' Cond ')' Stmt1 [ 'else' Stmt2 ] // 1.有else 2.无else
    public Stmt(StmtType type,Token ifToken,Token lParent,Cond cond,Token rParent,Stmt stmt1,Token elseToken,Stmt stmt2){
        super(NodeType.Stmt);
        this.type = type;
        this.ifToken = ifToken;
        this.lParent =lParent;
        this.cond = cond;
        this.rParent = rParent;
        this.stmt1 = stmt1;
        this.elseToken = elseToken;
        this.stmt2 = stmt2;
    }

    // 'for' '(' [ForStmt] ';' [Cond] ';' [ForStmt] ')' Stmt
    public Stmt(StmtType type, Token forToken, Token lParent, ForStmt forStmt1, List<Token> semiColonList,Cond cond,ForStmt forStmt2,Token rParent,Stmt stmt1){
        super(NodeType.Stmt);
        this.type= type;
        this.forToken = forToken;
        this.lParent = lParent;
        this.forStmt1 = forStmt1;
        this.semiColonList =semiColonList;
        this.cond = cond;
        this.forStmt2 =forStmt2;
        this.rParent = rParent;
        this.stmt1 =stmt1;
    }

    // 'break' ';' | 'continue' ';'
    public Stmt(StmtType type, Token token, Token semicolonToken){
        super(NodeType.Stmt);
        this.type = type;
        breakOrContinue = token;
        this.semicolonToken = semicolonToken;
    }

    public Stmt(StmtType type, Token returnToken, Exp exp, Token semicolonToken){
        super(NodeType.Stmt);
        this.type = type;
        this.returnToken = returnToken;
        this.exp = exp;
        this.semicolonToken = semicolonToken;
    }

    public Stmt(StmtType type, LVal lVal, Token assign, Token token, Token lParent, Token rParent, Token semicolonToken){
        super(NodeType.Stmt);
        this.type = type;
        this.lVal = lVal;
        this.assign = assign;
        if(type == StmtType.LVALGETINT){
            this.getIntToken = token;
        }else {
            this.getCharToken = token;
        }
        this.lParent = lParent;
        this.rParent = rParent;
        this.semicolonToken = semicolonToken;
    }

    // 'printf''('StringConst {','Exp}')'';' // 1.有Exp 2.无Exp
    public Stmt(StmtType type, Token printfToken, Token lParent, Token stringConst, List<Token> commas,List<Exp> expList, Token rParent, Token semicolonToken){
        super(NodeType.Stmt);
        this.type = type;
        this.printfToken= printfToken;
        this.lParent= lParent;
        this.stringConst = stringConst;
        this.commas = commas;
        this.expList = expList;
        this.rParent = rParent;
        this.semicolonToken = semicolonToken;
    }
    @Override
    public void print() {
        switch (type){
            case IF -> {
                ifToken.print();
                lParent.print();
                cond.print();
                rParent.print();
                stmt1.print();
                if(elseToken!=null){
                    elseToken.print();
                    stmt2.print();
                }
            }
            case EXP -> {
                if(exp!=null){
                    exp.print();
                }
                semicolonToken.print();
            }
            case LVALASSIGN -> {
                lVal.print();
                assign.print();
                exp.print();
                semicolonToken.print();
            }
            case Block -> {
                block.print();
            }
            case FOR -> {
                forToken.print();
                lParent.print();
                if(forStmt1!=null){
                    forStmt1.print();
                }
                semiColonList.get(0).print();
                if(cond!=null){
                    cond.print();
                }
                semiColonList.get(1).print();
                if(forStmt2!=null){
                    forStmt2.print();
                }
                rParent.print();
                stmt1.print();
            }
            case BREAK, CONTINUE -> {
                breakOrContinue.print();
                semicolonToken.print();
            }
            case RETURN -> {
                returnToken.print();
                if(exp!=null){
                    exp.print();
                }
                semicolonToken.print();
            }
            case LVALGETINT -> {
                lVal.print();
                assign.print();
                getIntToken.print();
                lParent.print();
                rParent.print();
                semicolonToken.print();
            }
            case LVALGETCHAR -> {
                lVal.print();
                assign.print();
                getCharToken.print();
                lParent.print();
                rParent.print();
                semicolonToken.print();
            }
            case PRINTF -> {
                printfToken.print();
                lParent.print();
                stringConst.print();
                for(int i=0;i<expList.size();i++){
                    commas.get(i).print();
                    expList.get(i).print();
                }
                rParent.print();
                semicolonToken.print();
            }
            default -> {

            }
        }
        printType();
    }

    @Override
    public void buildIr() {
        switch (type){
            case RETURN -> buildReturn();
            case LVALGETINT -> buildLValGetint();
            case LVALGETCHAR -> buildLValGetChar();
            case EXP -> {
                if(exp!=null){
                    exp.buildIr();
                }
            }
            case LVALASSIGN -> buildLValAssign();
            case Block -> buildBlock();
            case PRINTF -> buildPrintf();
            case CONTINUE,BREAK -> buildBreakOrContinue();
            case IF -> buildIf();
            case FOR -> buildFor();
        }
    }

    // 循环的时候根据cond进行判断，如果cond为真则进入loopBlock，为假则进入nextBlock
    private void buildFor() {
        BasicBlock condBlock = null; // 条件
        BasicBlock loopBlock = builder.buildBasicBlock(curFunc); // 循环体
        BasicBlock selfBlock = builder.buildBasicBlock(curFunc); // 循环改变语句
        BasicBlock nextBlock = builder.buildBasicBlock(curFunc); // 下一个基本块

        endLoop.push(selfBlock);
        nextLoop.push(nextBlock);
        if(forStmt1 != null){
            forStmt1.buildIr();
        }
        // 循环条件
        if(cond != null){
            condBlock = builder.buildBasicBlock(curFunc);
            builder.buildBr(curBlock, condBlock);
            cond.setTrueBlock(loopBlock);
            cond.setFalseBlock(nextBlock);
            curBlock = condBlock;
            cond.buildIr();
        } else {
            builder.buildBr(curBlock, loopBlock);
        }

        // 构建循环体
        curBlock = loopBlock;
        stmt1.buildIr();
        builder.buildBr(curBlock, selfBlock);

        // 构建循环改变条件
        curBlock = selfBlock;
        if(forStmt2!=null){
            forStmt2.buildIr();
        }
        if(cond!=null){ // 如果循环控制条件不为空，则跳转到控制
            builder.buildBr(curBlock, condBlock);
        }else{ // 否则跳转到循环体语句
            builder.buildBr(curBlock, loopBlock);
        }

        nextLoop.pop();
        endLoop.pop();
        curBlock = nextBlock;

    }

    private void buildIf() {
        BasicBlock trueBlock = builder.buildBasicBlock(curFunc);
        BasicBlock falseBlock, nextBlock;
        falseBlock = builder.buildBasicBlock(curFunc);
        nextBlock = stmt2==null ? falseBlock: builder.buildBasicBlock(curFunc);

        cond.setTrueBlock(trueBlock);
        cond.setFalseBlock(falseBlock);
        cond.buildIr();

        // 构建正确条件基本块
        curBlock = trueBlock;
        stmt1.setRtnType(rtnType);
        stmt1.buildIr();
        builder.buildBr(curBlock, nextBlock);

        if(stmt2!=null){
            curBlock = falseBlock;
            stmt2.buildIr();
            builder.buildBr(curBlock, nextBlock);
        }
        curBlock = nextBlock;
    }

    private void buildBreakOrContinue() {
        if( breakOrContinue.getType() == Token.TokenType.BREAKTK ){
            builder.buildBr(curBlock,nextLoop.peek());
        } else {
            builder.buildBr(curBlock,endLoop.peek());
        }
        curBlock = new BasicBlock("dead_block", new Function(false,"dead_function", new VoidType(), new ArrayList<>()));
    }

    private void buildPrintf() {
        // 构造字符串常量StringConst
        List<String> stringList = StrTool.getStrings(stringConst.getValue());
        List<Value> outputList = new ArrayList<>();
        for(int i = 0;i < expList.size();i++){
            expList.get(i).buildIr();
            outputList.add(valueUp);
        }
        int cnt = 0;
        for(int i = 0;i<stringList.size();i++){
            String string = stringList.get(i);
            Value value, finalValue;
            if(string.equals("%c")){
                value = outputList.get(cnt);
                cnt++;
                if(value.getValueType() instanceof IntType){
                    finalValue = builder.buildTrunc(curBlock, value);
                }else {
                    finalValue = value;
                }
                builder.buildCall(curBlock, Function.putchar, new ArrayList<>(){{
                    add(finalValue);
                }});
            } else if (string.equals("%d")) {
                value = outputList.get(cnt);
                cnt++;
                if(value.getValueType() instanceof CharType){
                    finalValue = builder.buildZext(curBlock, value);
                } else {
                    finalValue = value;
                }
                builder.buildCall(curBlock, Function.putint, new ArrayList<>(){{
                    add(finalValue);
                }});
            } else {
                GlobalVariable constString = builder.buildConstStr(string);
                Getelementptr addr = builder.buildGetElementPtr(curBlock, constString, new ConstInt(0), new ConstInt(0));
                builder.buildCall(curBlock, Function.putstr, new ArrayList<>(){{
                    add(addr);
                }});
            }
        }
    }

    private void buildBlock(){
        stack.push(new IrSymbolTable());
        block.buildIr();
        stack.pop();
    }

    private void buildReturn(){
        if(exp == null){ // 无返回值
            builder.buildRet(curBlock);
        }else{ // 有返回值
            exp.buildIr();
            if(rtnType instanceof IntType && valueUp.getValueType() instanceof CharType){
                valueUp = builder.buildZext(curBlock, valueUp);
            } else if (rtnType instanceof CharType && valueUp.getValueType() instanceof IntType) {
                valueUp = builder.buildTrunc(curBlock, valueUp);
            }
            builder.buildRet(curBlock, valueUp);
        }
    }

    private void buildLValAssign() {
        lValAtLeft = true;
        lVal.buildIr();
        lValAtLeft = false;
        Value lValue = valueUp;
        exp.buildIr();
        Value value = valueUp;
        if(((PointerType)lValue.getValueType()).getPointingType() instanceof CharType && value.getValueType() instanceof IntType){
            value = builder.buildTrunc(curBlock, value);
        } else if (((PointerType)lValue.getValueType()).getPointingType() instanceof IntType && value.getValueType() instanceof CharType) {
            value = builder.buildZext(curBlock, value);
        }
        builder.buildStore(curBlock, value, lValue);
    }

    private void buildLValGetChar() {
        // 不需要Load语句，直接返回指针类型
        lValAtLeft = true;
        lVal.buildIr();
        lValAtLeft = false;
        Value lValue = valueUp;
        Call call = builder.buildCall(curBlock, Function.getchar, new ArrayList<>());
        if(((PointerType)(lValue.getValueType())).getPointingType() instanceof CharType){
            // 为char类型变量赋int值，需要先进行缩减
            Trunc trunc = builder.buildTrunc(curBlock, call);
            builder.buildStore(curBlock, trunc, lValue);
        } else {
            builder.buildStore(curBlock, call, lValue);
        }
    }

    // lval = exp;
    private void buildLValGetint() {
        lValAtLeft = true;
        lVal.buildIr();
        lValAtLeft = false;
        Value lValue = valueUp;
        Call call = builder.buildCall(curBlock, Function.getint, new ArrayList<>());
        if(((PointerType)(lValue.getValueType())).getPointingType() instanceof CharType){
            // 为char类型变量赋int值，需要先进行缩减
            Trunc trunc = builder.buildTrunc(curBlock, lValue);
            builder.buildStore(curBlock, trunc, lValue);
        } else {
            builder.buildStore(curBlock, call, lValue);
        }
    }



    public void traverse() {
        switch (type){
            case IF -> {
                cond.traverse();
                stmt1.traverse();
                if(stmt2!=null){
                    stmt2.traverse();
                }
            }
            case EXP -> {
                if(exp!=null){
                    exp.traverse();
                }
            }
            case LVALASSIGN -> {
                // 检查是否为常量
                lVal.traverse();
                SemanticError.checkChangeConst(lVal.getIdent().getValue(), lVal.getIdent().getLineNum());
                exp.traverse();
            }
            case Block -> {
                SemanticError.addTable(this);
                block.traverse();
                SemanticError.popTable();
            }
            case FOR -> {
                if(forStmt1!=null){
                    forStmt1.traverse();
                }
                if(cond!=null){
                    cond.traverse();
                }
                if(forStmt2!=null){
                    forStmt2.traverse();
                }
                SemanticError.inLoop();
                stmt1.traverse();
                SemanticError.leaveLoop();
            }
            case RETURN -> {
                if(exp!=null){
                    SemanticError.checkReturn(returnToken.getLineNum());
                    exp.traverse();
                }
            }
            case LVALGETINT,LVALGETCHAR -> {
                // 检查是否为常量
                SemanticError.checkChangeConst(lVal.getIdent().getValue(), lVal.getIdent().getLineNum());
                lVal.traverse();
            }
            case PRINTF -> {
                // 检查printf中格式字符与表达式个数是否匹配
                SemanticError.checkFormat(stringConst.getValue(),expList.size(),printfToken.getLineNum());
                for(Exp exp:expList){
                    exp.traverse();
                }
            }
            case BREAK, CONTINUE -> {
                SemanticError.checkLoop(breakOrContinue.getLineNum());
            }
            default -> {

            }
        }
    }
}
