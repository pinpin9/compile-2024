package frontend;

import error.Error;
import error.ErrorHandler;
import node.*;
import node.Character;
import node.Number;
import settings.Settings;
import token.Token;
import token.Token.TokenType;
import tools.IO;

import java.io.IOException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

public class ParserAnalyze {
    // 单例模式
    private static ParserAnalyze instance = new ParserAnalyze();
    public static ParserAnalyze getInstance() {
        return instance;
    }
    private List<Token> tokens;
    private int len; // Tokens的长度

    private int currentIndex = 0;
    private Token currentToken;

    private CompUnit resultCompUnit = null;
    private ErrorHandler errorHandler = ErrorHandler.getErrorHandler();

    public static IO parserOutput = new IO(Settings.parserFile);
    // 语法分析
    public void analyze(List<Token> tokens){
        this.tokens = tokens;
        this.len = tokens.size();
        this.currentToken = tokens.get(currentIndex);

        // 语法分析
        resultCompUnit = getCompUint();
    }

    // 匹配当前终结符的类型，并且改变currentToken
    private Token match(TokenType tokenType){
        Token temp = currentToken;
        if(currentToken.getType()==tokenType){
            next();
            return temp;
        }else{
            if(tokenType==TokenType.SEMICN){ // 缺少;
                errorHandler.addError(new Error(Error.ErrorType.i,tokens.get(currentIndex-1).getLineNum()));
                return new Token(TokenType.SEMICN,tokens.get(currentIndex-1).getLineNum(),";");
            } else if (tokenType==TokenType.RPARENT) {
                errorHandler.addError(new Error(Error.ErrorType.j,tokens.get(currentIndex-1).getLineNum()));
                return new Token(TokenType.RPARENT,tokens.get(currentIndex-1).getLineNum(),")");
            } else if (tokenType==TokenType.RBRACK) {
                errorHandler.addError(new Error(Error.ErrorType.k,tokens.get(currentIndex-1).getLineNum()));
                return new Token(TokenType.RBRACK,tokens.get(currentIndex-1).getLineNum(),"]");
            }
        }
        return null;

    }
    private void next(){
        if(currentIndex+1<len){
            currentIndex++;
            currentToken = tokens.get(currentIndex);
        }

    }

    // 读取index个偏移以后的Token，判断非终结符的类型
    private boolean preMatch(int index, TokenType tokenType){
        if(index+currentIndex < len && tokens.get(currentIndex+index).getType() == tokenType){
            return true;
        }
        return false;
    }

    // 进行回溯
    private int savedPos = 0;
    private void savePos(){
        savedPos = currentIndex;
    }
    private void backPos(){
        currentIndex = savedPos;
        currentToken = tokens.get(currentIndex);
    }

    //
    // 递归下降子程序
    //

    // CompUnit → {Decl} {FuncDef} MainFuncDef
    // Decl           [const] int/char var
    // FuncDef        int/char/void f()
    // mainFuncDef    int main()
    private CompUnit getCompUint(){
        List<Decl> declList = new ArrayList<>();
        List<FuncDef> funcDefList = new ArrayList<>();
        MainFuncDef mainFuncDef = null;
        // 匹配Decl
        while(!preMatch(2,TokenType.LPARENT)){
            Decl decl = getDecl();
            declList.add(decl);
        }
        // 匹配FuncDef，当前字符的后一个不为main
        while(!preMatch(1,TokenType.MAINTK)){
            FuncDef funcDef = getFuncDef();
            funcDefList.add(funcDef);
        }
        mainFuncDef = getMainFuncDef();
        return new CompUnit(declList,funcDefList,mainFuncDef);
    }

    // Decl → ConstDecl | VarDecl
    // ConstDecl开头带有'const'
    private Decl getDecl() {
        VarDecl varDecl = null;
        ConstDecl constDecl = null;
        if (preMatch(0, TokenType.CONSTTK)) { // ConstDecl
            constDecl = getConstDecl();
        } else {
            varDecl = getVarDecl();
        }
        return new Decl(constDecl, varDecl);
    }
    // FuncDef → FuncType Ident '(' [FuncFParams] ')' Block
    private FuncDef getFuncDef(){
        FuncType funcType = getFuncType();
        Token ident = match(TokenType.IDENFR);
        Token lParent = match(TokenType.LPARENT);
        FuncFParams funcFParams = null;
        if(!preMatch(0,TokenType.RPARENT)){
            funcFParams = getFuncFParams();
        }
        Token rParent = match(TokenType.RPARENT);
        Block block = getBlock();
        return new FuncDef(funcType, ident, lParent, funcFParams, rParent, block);
    }

    // MainFuncDef → 'int' 'main' '(' ')' Block
    private MainFuncDef getMainFuncDef(){
        Token intToken = match(TokenType.INTTK);
        Token mainToken = match(TokenType.MAINTK);
        Token lParent = match(TokenType.LPARENT);
        Token rParent = match(TokenType.RPARENT);
        Block block = getBlock();
        return new MainFuncDef(intToken,mainToken,lParent,rParent,block);
    }

    // ConstDecl → 'const' BType ConstDef { ',' ConstDef } ';'
    private ConstDecl getConstDecl(){
        Token constToken = match(TokenType.CONSTTK);
        BType bType = getBType();
        List<ConstDef> constDefList = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token semicolonToken = null;
        constDefList.add(getConstDef());
        while(preMatch(0,TokenType.COMMA)){ // { ','
            commas.add(match(TokenType.COMMA));
            constDefList.add(getConstDef());
        }
        semicolonToken = match(TokenType.SEMICN);
        return new ConstDecl(constToken,bType,constDefList,commas,semicolonToken);
    }

    // VarDecl → BType VarDef { ',' VarDef } ';'
    private VarDecl getVarDecl(){
        BType bType = getBType();
        List<VarDef> varDefList = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token semicolon = null;
        varDefList.add(getVarDef());
        while(preMatch(0,TokenType.COMMA)){
            commas.add(match(TokenType.COMMA));
            varDefList.add(getVarDef());
        }
        semicolon = match(TokenType.SEMICN);
        return new VarDecl(bType, varDefList, commas, semicolon);
    }

    // BType → 'int' | 'char'
    private BType getBType(){
        Token token = match(TokenType.INTTK); // 先尝试匹配int
        if(token == null){
            token = match(TokenType.CHARTK);
        }
        return new BType(token);
    }

    // ConstDef → Ident [ '[' ConstExp ']' ] '=' ConstInitVal
    private ConstDef getConstDef(){
        Token ident = match(TokenType.IDENFR);
        Token lBrack = null;
        ConstExp constExp = null;
        Token rBrack = null;
        Token assign ;
        ConstInitVal constInitVal;
        if(preMatch(0,TokenType.LBRACK)){
            lBrack = match(TokenType.LBRACK);
            constExp = getConstExp();
            rBrack = match(TokenType.RBRACK);
        }
        assign = match(TokenType.ASSIGN);
        constInitVal = getConstInitVal();
        return new ConstDef(ident,lBrack,constExp,rBrack,assign,constInitVal);
    }

    // ConstInitVal → ConstExp | '{' [ ConstExp { ',' ConstExp } ] '}' | StringConst
    // '{' [ ConstExp { ',' ConstExp } ] '}'的first字符为{
    // StringConst为终结符
    private ConstInitVal getConstInitVal(){
        ConstExp constExp = null;

        Token lBrace = null;
        List<ConstExp> constExpList = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token rBrace = null;

        Token stringConst = null;
        if(preMatch(0,TokenType.LBRACE)){ // '{'
            lBrace = match(TokenType.LBRACE);
            if(!preMatch(0,TokenType.RBRACE)){
                constExpList.add(getConstExp());
                while(preMatch(0,TokenType.COMMA)){ // ','
                    commas.add(match(TokenType.COMMA));
                    constExpList.add(getConstExp());
                }
            }
            rBrace = match(TokenType.RBRACE);
            return new ConstInitVal(lBrace,constExpList,commas,rBrace);
        }else if (preMatch(0,TokenType.STRCON)){
            stringConst = match(TokenType.STRCON);
            return new ConstInitVal(stringConst);
        }else {
            constExp = getConstExp();
            return new ConstInitVal(constExp);
        }
    }

    // VarDef → Ident [ '[' ConstExp ']' ] | Ident [ '[' ConstExp ']' ] '=' InitVal
    // VarDef → Ident [ '[' ConstExp ']' ] [ '=' InitVal ]
    private VarDef getVarDef(){
        Token ident = match(TokenType.IDENFR);
        Token lBrack = null;
        ConstExp constExp = null;
        Token rBrack = null;
        Token assign = null;
        InitVal initVal = null;
        if(preMatch(0,TokenType.LBRACK)){
            lBrack = match(TokenType.LBRACK);
            constExp = getConstExp();
            rBrack = match(TokenType.RBRACK);
        }
        if(preMatch(0,TokenType.ASSIGN)){
            assign = match(TokenType.ASSIGN);
            initVal = getInitVal();
        }
        return new VarDef(ident,lBrack,constExp,rBrack,assign,initVal);
    }

    // InitVal → Exp | '{' [ Exp { ',' Exp } ] '}' | StringConst
    private InitVal getInitVal(){
        Exp exp;
        Token lBrace;
        List<Exp> expList = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        Token rBrace;
        Token stringConst;
        if(preMatch(0,TokenType.LBRACE)){ //'{'
            lBrace = match(TokenType.LBRACE);
            if(!preMatch(0,TokenType.RBRACE)){
                expList.add(getExp());
                while(preMatch(0,TokenType.COMMA)){
                    commas.add(match(TokenType.COMMA));
                    expList.add(getExp());
                }
            }
            rBrace = match(TokenType.RBRACE);
            return new InitVal(lBrace,expList,commas,rBrace);
        } else if (preMatch(0,TokenType.STRCON)) {
            stringConst = match(TokenType.STRCON);
            return new InitVal(stringConst);
        } else{
            exp = getExp();
            return new InitVal(exp);
        }
    }

    // FuncType → 'void' | 'int' | 'char'
    private FuncType getFuncType(){
        Token token = match(TokenType.VOIDTK); // 匹配void
        if(token==null){ // 匹配int
            token = match(TokenType.INTTK);
        }
        if(token == null){ // 匹配char
            token = match(TokenType.CHARTK);
        }
        return new FuncType(token);
    }

    // FuncFParams → FuncFParam { ',' FuncFParam }
    private FuncFParams getFuncFParams(){
        List<FuncFParam> funcFParams = new ArrayList<>();
        List<Token> commas = new ArrayList<>();

        funcFParams.add(getFuncFParam());
        while(preMatch(0,TokenType.COMMA)){
            commas.add(match(TokenType.COMMA));
            funcFParams.add(getFuncFParam());
        }
        return new FuncFParams(funcFParams,commas);
    }

    // FuncFParam → BType Ident ['[' ']']
    private FuncFParam getFuncFParam(){
        BType bType = getBType();
        Token ident = match(TokenType.IDENFR);
        Token lBrack = null;
        Token rBrack = null;
        if(preMatch(0,TokenType.LBRACK)){
            lBrack = match(TokenType.LBRACK);
            rBrack = match(TokenType.RBRACK);
        }
        return new FuncFParam(bType,ident,lBrack,rBrack);
    }

    // Block → '{' { BlockItem } '}'
    private Block getBlock(){
        Token lBrace = match(TokenType.LBRACE);
        List<BlockItem> blockItemList = new ArrayList<>();
        while(!preMatch(0,TokenType.RBRACE)){
            blockItemList.add(getBlockItem());
        }
        Token rBrace = match(TokenType.RBRACE);
        return new Block(lBrace,blockItemList,rBrace);
    }

    // BlockItem → Decl | Stmt
    // Decl的first字符为'const' | 'int' | 'char'
    private BlockItem getBlockItem(){
        Decl decl = null;
        Stmt stmt = null;
        if(preMatch(0,TokenType.CONSTTK) || preMatch(0,TokenType.INTTK) || preMatch(0,TokenType.CHARTK)){
            decl = getDecl();
        }else{
            stmt = getStmt();
        }
        return new BlockItem(decl,stmt);
    }

    /*
    语句 Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
    | [Exp] ';' //有无Exp两种情况
    | Block                                              // '{'
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
    private Stmt getStmt(){
        Exp exp = null;
        Token lParent = null;
        Token rParent = null;
        Cond cond = null;
        Stmt stmt1 = null;
        Stmt stmt2 = null;
        Token semiColonToken = null;
        if(preMatch(0,TokenType.LBRACE)){ // Block
            Block block = getBlock();
            return new Stmt(Stmt.StmtType.Block,block);
        } else if (preMatch(0,TokenType.IFTK)) { // if
            Token ifToken = match(TokenType.IFTK);
            lParent = match(TokenType.LPARENT);
            cond = getCond();
            rParent = match(TokenType.RPARENT);
            stmt1 = getStmt();
            Token elseToken = null;
            if(preMatch(0,TokenType.ELSETK)){
                elseToken = match(TokenType.ELSETK);
                stmt2 = getStmt();
            }
            return new Stmt(Stmt.StmtType.IF,ifToken,lParent,cond,rParent,stmt1,elseToken,stmt2);
        } else if (preMatch(0,TokenType.FORTK)) { // for
            Token forToken = match(TokenType.FORTK);
            lParent = match(TokenType.LPARENT);
            ForStmt forStmt1 = null;
            List<Token> semicolonList = new ArrayList<>();
            ForStmt forStmt2 = null;
            if(!preMatch(0,TokenType.SEMICN)){
                forStmt1 = getForStmt();
            }
            semicolonList.add(match(TokenType.SEMICN));
            if(!preMatch(0,TokenType.SEMICN)){
                cond = getCond();
            }
            semicolonList.add(match(TokenType.SEMICN));
            if(!preMatch(0,TokenType.RPARENT)){
                forStmt2 = getForStmt();
            }
            rParent = match(TokenType.RPARENT);
            stmt1 = getStmt();
            return new Stmt(Stmt.StmtType.FOR,forToken,lParent,forStmt1,semicolonList,cond,forStmt2,rParent,stmt1);
        } else if (preMatch(0,TokenType.BREAKTK)||preMatch(0,TokenType.CONTINUETK)) { // break;continue;
            Token token = match(TokenType.BREAKTK);
            if (token!=null){
                semiColonToken = match(TokenType.SEMICN);
                return new Stmt(Stmt.StmtType.BREAK,token,semiColonToken);
            }else{
                token = match(TokenType.CONTINUETK);
                semiColonToken = match(TokenType.SEMICN);
                return new Stmt(Stmt.StmtType.CONTINUE,token,semiColonToken);
            }
        } else if (preMatch(0,TokenType.RETURNTK)) { // return
            Token returnToken = match(TokenType.RETURNTK);
            if(!preMatch(0,TokenType.SEMICN)){
                exp = getExp();
            }
            semiColonToken = match((TokenType.SEMICN));
            return new Stmt(Stmt.StmtType.RETURN,returnToken, exp, semiColonToken);
        } else if (preMatch(0,TokenType.PRINTFTK)) { // printf
            Token printfToken = match(TokenType.PRINTFTK);
            lParent = match(TokenType.LPARENT);
            Token stringConst = match(TokenType.STRCON);
            List<Token> commas = new ArrayList<>();
            List<Exp> expList = new ArrayList<>();
            while(preMatch(0,TokenType.COMMA)){
                commas.add(match(TokenType.COMMA));
                expList.add(getExp());
            }
            rParent = match(TokenType.RPARENT);
            semiColonToken = match(TokenType.SEMICN);
            return new Stmt(Stmt.StmtType.PRINTF,printfToken,lParent,stringConst,commas,expList,rParent,semiColonToken);
        } else if (preMatch(0,TokenType.SEMICN)) { //只有分号
            semiColonToken = match(TokenType.SEMICN);
            return new Stmt(Stmt.StmtType.EXP,exp,semiColonToken);
        } else {
            /*
            Stmt → LVal '=' Exp ';' // 每种类型的语句都要覆盖
            | Exp ';'
            | LVal '=' 'getint''('')'';'
            | LVal '=' 'getchar''('')'';'
             */
            savePos(); // 保存位置
            exp = getExp();
            LVal lVal = null;
            Token assign = null;
            Token token = null; // 读取getint或者getchar
            if(preMatch(0,TokenType.ASSIGN)){ // LVal = 语句
                backPos(); //回溯
                lVal = getLVal();
                assign = match(TokenType.ASSIGN);
                if(preMatch(0,TokenType.GETINTTK)){ // LVal = getint();
                    token = match(TokenType.GETINTTK);
                    lParent = match(TokenType.LPARENT);
                    rParent = match(TokenType.RPARENT);
                    semiColonToken = match(TokenType.SEMICN);
                    return new Stmt(Stmt.StmtType.LVALGETINT,lVal,assign,token,lParent,rParent,semiColonToken);
                } else if (preMatch(0,TokenType.GETCHARTK)) {
                    token = match(TokenType.GETCHARTK);
                    lParent = match(TokenType.LPARENT);
                    rParent = match(TokenType.RPARENT);
                    semiColonToken = match(TokenType.SEMICN);
                    return new Stmt(Stmt.StmtType.LVALGETCHAR,lVal,assign,token,lParent,rParent,semiColonToken);
                } else {
                    exp = getExp();
                    semiColonToken = match(TokenType.SEMICN);
                    return new Stmt(Stmt.StmtType.LVALASSIGN,lVal,assign,exp,semiColonToken);
                }
            }else{ //读取exp以后，如果剩下';',说明为Exp;
                semiColonToken = match(TokenType.SEMICN);
                return new Stmt(Stmt.StmtType.EXP,exp,semiColonToken);
            }
        }
    }

    // ForStmt → LVal '=' Exp
    private ForStmt getForStmt(){
        LVal lVal = getLVal();
        Token assign = match(TokenType.ASSIGN);
        Exp exp = getExp();
        return new ForStmt(lVal, assign, exp);
    }

    // Exp → AddExp
    private Exp getExp(){
        AddExp addExp = getAddExp();
        return new Exp(addExp);
    }

    // Cond → LOrExp
    private Cond getCond(){
        LOrExp lOrExp = getLOrExp();
        return new Cond(lOrExp);
    }

    // LVal → Ident ['[' Exp ']']
    private LVal getLVal(){
        Token ident = match(TokenType.IDENFR);
        Token lBrack = null;
        Exp exp = null;
        Token rBrack = null;
        if(preMatch(0,TokenType.LBRACK)){
            lBrack = match(TokenType.LBRACK);
            exp = getExp();
            rBrack = match(TokenType.RBRACK);
        }
        return new LVal(ident,lBrack,exp,rBrack);
    }

    // PrimaryExp → '(' Exp ')' | LVal | Number | Character
    private PrimaryExp getPrimaryExp(){
        if(preMatch(0,TokenType.LPARENT)){ // '(' Exp ')'
            Token lParent = match(TokenType.LPARENT);
            Exp exp = getExp();
            Token rParent = match(TokenType.RPARENT);
            return new PrimaryExp(lParent,exp,rParent);
        } else if (preMatch(0,TokenType.INTCON)) {
            Number number = getNumber();
            return new PrimaryExp(number);
        } else if (preMatch(0,TokenType.CHRCON)) {
            Character character = getCharacter();
            return new PrimaryExp(character);
        }else{
            LVal lVal = getLVal();
            return new PrimaryExp(lVal);
        }
    }

    // Number → IntConst
    private Number getNumber(){
        Token intConst = match(TokenType.INTCON);
        return new Number(intConst);
    }

    // Character → CharConst
    private Character getCharacter(){
        Token charConst = match(TokenType.CHRCON);
        return new Character(charConst);
    }

    // UnaryExp → PrimaryExp | Ident '(' [FuncRParams] ')' | UnaryOp UnaryExp
    private UnaryExp getUnaryExp(){
        if(isUnaryOp(currentToken)){ // UnaryOp UnaryExp
            UnaryOp unaryOp = getUnaryOp();
            UnaryExp unaryExp = getUnaryExp();
            return new UnaryExp(unaryOp,unaryExp);
        } else if (preMatch(1,TokenType.LPARENT)) {
            Token ident = match(TokenType.IDENFR);
            Token lParent = match(TokenType.LPARENT);
            FuncRParams funcRParams = null;
            if(!preMatch(0,TokenType.RPARENT)){
                funcRParams = getFuncRParams();
            }
            Token rParent = match(TokenType.RPARENT);
            return new UnaryExp(ident,lParent,funcRParams,rParent);
        } else {
            PrimaryExp primaryExp = getPrimaryExp();
            return new UnaryExp(primaryExp);
        }
    }
    private boolean isUnaryOp(Token token){
        return token.getType()==TokenType.PLUS || token.getType() == TokenType.MINU || token.getType() == TokenType.NOT;
    }

    // UnaryOp → '+' | '−' | '!'
    private UnaryOp getUnaryOp(){
        Token token = match(TokenType.PLUS);
        if(token == null){
            token = match(TokenType.MINU);
        }
        if(token == null){
            token = match(TokenType.NOT);
        }
        return new UnaryOp(token);
    }

    // FuncRParams → Exp { ',' Exp }
    private FuncRParams getFuncRParams(){
        List<Exp> expList = new ArrayList<>();
        List<Token> commas = new ArrayList<>();
        expList.add(getExp());
        while(preMatch(0,TokenType.COMMA)){
            commas.add(match(TokenType.COMMA));
            expList.add(getExp());
        }
        return new FuncRParams(expList,commas);
    }

    // MulExp → UnaryExp | MulExp ('*' | '/' | '%') UnaryExp
    // 改成扩充的BNF范式 MulExp → UnaryExp { ('*' | '/' | '%') UnaryExp }
    private MulExp getMulExp(){
        List<UnaryExp> unaryExps = new ArrayList<>();
        List<Token> ops = new ArrayList<>();
        unaryExps.add(getUnaryExp());

        while(preMatch(0,TokenType.MULT)||preMatch(0,TokenType.DIV)||preMatch(0,TokenType.MOD)){
            Token token = match(TokenType.MULT);
            if (token == null){
                token = match(TokenType.DIV);
            }
            if(token == null){
                token = match(TokenType.MOD);
            }
            ops.add(token);
            unaryExps.add(getUnaryExp());
        }
        return new MulExp(unaryExps,ops);
    }

    // AddExp → MulExp | AddExp ('+' | '−') MulExp
    // 改成扩充的BNF范式 AddExp → MulExp { ('+' | '−') MulExp }
    private AddExp getAddExp(){
        List<MulExp> mulExps = new ArrayList<>();
        List<Token> ops = new ArrayList<>();
        mulExps.add(getMulExp());
        while(preMatch(0,TokenType.PLUS)||preMatch(0,TokenType.MINU)){
            Token token = match(TokenType.PLUS);
            if(token == null){
                token = match(TokenType.MINU);
            }
            ops.add(token);
            mulExps.add(getMulExp());
        }
        return new AddExp(mulExps,ops);
    }

    // RelExp → AddExp | RelExp ('<' | '>' | '<=' | '>=') AddExp
    // 改成扩充的BNF范式 RelExp → AddExp { ('<' | '>' | '<=' | '>=') AddExp }
    private RelExp getRelExp(){
        List<AddExp> addExps = new ArrayList<>();
        List<Token> ops = new ArrayList<>();
        addExps.add(getAddExp());
        while(preMatch(0,TokenType.LSS)||preMatch(0,TokenType.LEQ)||preMatch(0,TokenType.GRE)||preMatch(0,TokenType.GEQ)){
            Token token = match(TokenType.LSS);
            if(token == null){
                token = match(TokenType.LEQ);
            }
            if(token == null){
                token = match(TokenType.GRE);
            }
            if(token == null){
                token = match(TokenType.GEQ);
            }
            ops.add(token);
            addExps.add(getAddExp());
        }
        return new RelExp(addExps,ops);
    }

    // EqExp → RelExp | EqExp ('==' | '!=') RelExp
    // EqExp → RelExp { ('==' | '!=') RelExp }
    private EqExp getEqExp(){
        List<RelExp> relExps = new ArrayList<>();
        List<Token> ops = new ArrayList<>();
        relExps.add(getRelExp());
        while(preMatch(0,TokenType.EQL)||preMatch(0,TokenType.NEQ)){
            Token token = match(TokenType.EQL);
            if(token == null){
                token = match(TokenType.NEQ);
            }
            ops.add(token);
            relExps.add(getRelExp());
        }
        return new EqExp(relExps,ops);
    }

    // LAndExp → EqExp | LAndExp '&&' EqExp
    // LAndExp → EqExp { '&&' EqExp }
    private LAndExp getLAndExp(){
        List<EqExp> eqExps = new ArrayList<>();
        List<Token> ops =new ArrayList<>();
        eqExps.add(getEqExp());
        while(preMatch(0,TokenType.AND)){
            ops.add(match(TokenType.AND));
            eqExps.add(getEqExp());
        }
        return new LAndExp(eqExps,ops);
    }

    // LOrExp → LAndExp | LOrExp '||' LAndExp
    private LOrExp getLOrExp(){
        List<LAndExp> lAndExps = new ArrayList<>();
        List<Token> ops =new ArrayList<>();
        lAndExps.add(getLAndExp());
        while(preMatch(0,TokenType.OR)){
            ops.add(match(TokenType.OR));
            lAndExps.add(getLAndExp());
        }
        return new LOrExp(lAndExps,ops);
    }

    // ConstExp → AddExp
    private ConstExp getConstExp(){
        AddExp addExp = getAddExp();
        return new ConstExp(addExp);
    }

    public void print(){
        if(!ErrorHandler.getErrorHandler().getIsError()){
            resultCompUnit.print();
        }
    }

    public CompUnit getResult(){
        return resultCompUnit;
    }
}

