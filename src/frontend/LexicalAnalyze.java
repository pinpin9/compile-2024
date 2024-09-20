package frontend;

import frontend.error.Error;
import frontend.error.ErrorNode;
import frontend.token.Token;
import settings.Settings;
import tools.IO;

import java.io.IOException;
import java.util.*;

// 词法分析
public class LexicalAnalyze {
    public LexicalAnalyze() {}
    // 单例模式
    private static LexicalAnalyze instance = new LexicalAnalyze();
    public static LexicalAnalyze getInstance() {
        return instance;
    }
    // 转义字符集合
    private final Set<String> escapes = Set.of("\\a", "\\b", "\\n", "\\t", "\\v"
            , "\\f", "\\\"", "\\\'", "\\\\", "\\0");
    private List<Token> tokens=new ArrayList<>();
    // 错误处理
    private final ErrorNode errorNode = ErrorNode.getErrorNode();
    // 返回结果
    public List<Token> getTokens() {
        return tokens;
    }
    private int lineNum=1;
    private Token.TokenType wordMatch(String letter){
        Token.TokenType type;
        switch (letter){
            case "main":
                type= Token.TokenType.MAINTK;
                break;
            case "const":
                type= Token.TokenType.CONSTTK;
                break;
            case "int":
                type= Token.TokenType.INTTK;
                break;
            case "char":
                type= Token.TokenType.CHARTK;
                break;
            case "break":
                type= Token.TokenType.BREAKTK;
                break;
            case "continue":
                type= Token.TokenType.CONTINUETK;
                break;
            case "if":
                type= Token.TokenType.IFTK;
                break;
            case "else":
                type= Token.TokenType.ELSETK;
                break;
            case "for":
                type= Token.TokenType.FORTK;
                break;
            case "getint":
                type= Token.TokenType.GETINTTK;
                break;
            case "getchar":
                type= Token.TokenType.GETCHARTK;
                break;
            case "printf":
                type= Token.TokenType.PRINTFTK;
                break;
            case "return":
                type= Token.TokenType.RETURNTK;
                break;
            case "void":
                type = Token.TokenType.VOIDTK;
                break;
            default:
                type = Token.TokenType.IDENFR;
                break;
        }
        return type;
    }
    private final HashMap<String,Token.TokenType> symbols = new HashMap<String,Token.TokenType>() {
        {
            put("+", Token.TokenType.PLUS);
            put("-", Token.TokenType.MINU);
            put("*", Token.TokenType.MULT);
            put("/", Token.TokenType.DIV);
            put("%", Token.TokenType.MOD);
            put("=", Token.TokenType.ASSIGN);
            put(";", Token.TokenType.SEMICN);
            put(",", Token.TokenType.COMMA);
            put("(", Token.TokenType.LPARENT);
            put(")", Token.TokenType.RPARENT);
            put("[", Token.TokenType.LBRACK);
            put("]", Token.TokenType.RBRACK);
            put("{", Token.TokenType.LBRACE);
            put("}", Token.TokenType.RBRACE);
        }
    };

    private boolean isLetter(char c){
        return Character.isLetter(c);
    }
    private boolean isDigit(char c){
        return Character.isDigit(c);
    }
    private boolean isDigitOrLetter(char c){
        return Character.isLetterOrDigit(c);
    }

    public void analyze(String sourceCode) {
        int len = sourceCode.length();  // 源码长度

        for (int i = 0; i < len; i++) {
            char c = sourceCode.charAt(i);
            // 换行符，更新行号
            if (c == '\n') {
                lineNum++;
            } else if (c == '_' || isLetter(c)) {
                // 标识符或保留字
                i = handleWord(sourceCode, len, i);
            } else if (isDigit(c)) {
                // 数字
                i = handleNumber(sourceCode, len, i);
            } else if (c == '\'') {
                // 字符常量
                i = handleCharacter(sourceCode, len, i);
            } else if (c == '\"') {
                // 字符串常量
                i = handleString(sourceCode, len, i);
            } else if (c == '/') {
                // 注释
                i = handleComment(sourceCode, len, i);
            } else if (c == '&') {
                // 逻辑与 &&
                i = handleAndOperator(sourceCode, len, i);
            } else if (c == '|') {
                // 逻辑或 ||
                i = handleOrOperator(sourceCode, len, i);
            } else if (c == '<') {
                //  < 或 <=
                i = handleLessOperator(sourceCode, len, i);
            } else if (c == '>') {
                // > 或 >=
                i = handleGreaterOperator(sourceCode, len, i);
            } else if (c == '!') {
                // ! 或 !=
                i = handleNotOperator(sourceCode, len, i);
            } else if (c == '=') {
                // = 或 ==
                i = handleEqualsOperator(sourceCode, len, i);
            } else {
                // 处理单个符号
                i = handleSymbol(sourceCode, i);
            }
        }
    }

    // 标识符或保留字处理
    private int handleWord(String sourceCode, int len, int i) {
        int j = i + 1;
        while (j < len && (sourceCode.charAt(j) == '_' || isDigitOrLetter(sourceCode.charAt(j))))
            j++;
        String letter = sourceCode.substring(i, j);
        Token.TokenType type = wordMatch(letter);
        addToken(type, lineNum, letter);
        return j - 1;
    }

    // 数字处理
    private int handleNumber(String sourceCode, int len, int i) {
        int j = i + 1;
        while (j < len && isDigit(sourceCode.charAt(j)))
            j++;
        String number = sourceCode.substring(i, j);
        addToken(Token.TokenType.INTCON, lineNum, number);
        return j - 1;
    }

    // 字符处理
    private int handleCharacter(String sourceCode, int len, int i) {
        int j = i + 1;
        while (j < len && sourceCode.charAt(j) != '\'')
            j++;
        String letter = sourceCode.substring(i, j + 1);
        String element = sourceCode.substring(i + 1, j);
        // 判断是否合法字符
        if (j == i + 2 && element.charAt(0) >= 32 && element.charAt(0) <= 126) {
            addToken(Token.TokenType.CHRCON, lineNum, letter);
        } else if (j == i + 3 && escapes.contains(element)) {
            addToken(Token.TokenType.CHRCON, lineNum, letter);
        }
        return j;
    }

    // 字符串处理
    private int handleString(String sourceCode, int len, int i) {
        int j = i + 1;
        while (j < len && sourceCode.charAt(j) != '\"')
            j++;
        String str = sourceCode.substring(i, j + 1);
        addToken(Token.TokenType.STRCON, lineNum, str);
        return j;
    }

    // 注释处理
    private int handleComment(String sourceCode, int len, int i) {
        int j = i + 1;
        if (j < len && sourceCode.charAt(j) == '/') { // 单行注释
            while (j < len && sourceCode.charAt(j) != '\n')
                j++;
            return j - 1;
        } else if (j < len && sourceCode.charAt(j) == '*') { // 多行注释
            j++;
            while (j + 1 < len && !(sourceCode.charAt(j) == '*' && sourceCode.charAt(j + 1) == '/')){
                if(sourceCode.charAt(j)=='\n')
                    lineNum++;
                j++;
            }
            return j + 1;
        } else {
            addToken(Token.TokenType.DIV, lineNum, "/");
            return i;
        }
    }

    // 处理逻辑与 &&
    private int handleAndOperator(String sourceCode, int len, int i) {
        int j = i + 1;
        if (j < len && sourceCode.charAt(j) == '&') {
            addToken(Token.TokenType.AND, lineNum, "&&");
            return j;
        } else {
            errorNode.accessError(new Error(Error.ErrorType.a, lineNum, "&"));
            return i;
        }
    }

    // 处理逻辑或 ||
    private int handleOrOperator(String sourceCode, int len, int i) {
        int j = i + 1;
        if (j < len && sourceCode.charAt(j) == '|') {
            addToken(Token.TokenType.OR, lineNum, "||");
            return j;
        } else {
            errorNode.accessError(new Error(Error.ErrorType.a, lineNum, "|"));
            return i;
        }
    }

    // 处理小于号 < 或 <=
    private int handleLessOperator(String sourceCode, int len, int i) {
        int j = i + 1;
        if (j < len && sourceCode.charAt(j) == '=') {
            addToken(Token.TokenType.LEQ, lineNum, "<=");
            return j;
        } else {
            addToken(Token.TokenType.LSS, lineNum, "<");
            return i;
        }
    }

    // 处理大于号 > 或 >=
    private int handleGreaterOperator(String sourceCode, int len, int i) {
        int j = i + 1;
        if (j < len && sourceCode.charAt(j) == '=') {
            addToken(Token.TokenType.GEQ, lineNum, ">=");
            return j;
        } else {
            addToken(Token.TokenType.GRE, lineNum, ">");
            return i;
        }
    }

    // 处理逻辑非 ! 或 !=
    private int handleNotOperator(String sourceCode, int len, int i) {
        int j = i + 1;
        if (j < len && sourceCode.charAt(j) == '=') {
            addToken(Token.TokenType.NEQ, lineNum, "!=");
            return j;
        } else {
            addToken(Token.TokenType.NOT, lineNum, "!");
            return i;
        }
    }

    // 处理等号 = 或 ==
    private int handleEqualsOperator(String sourceCode, int len, int i) {
        int j = i + 1;
        if (j < len && sourceCode.charAt(j) == '=') {
            addToken(Token.TokenType.EQL, lineNum, "==");
            return j;
        } else {
            addToken(Token.TokenType.ASSIGN, lineNum, "=");
            return i;
        }
    }

    // 处理符号
    private int handleSymbol(String sourceCode, int i) {
        String str = String.valueOf(sourceCode.charAt(i));
        if (symbols.containsKey(str)) {
            addToken(symbols.get(str), lineNum, str);
        }
        return i;
    }


    public void printTokenList() throws IOException {
        IO.output(tokens, Settings.lexerFile);
    }

    private void addToken(Token.TokenType tokenType, int lineNum, String val){
        tokens.add(new Token(tokenType,lineNum,val));
    }
}
