package token;

import frontend.ParserAnalyze;

public class Token {
    /**
     * 词法单元类型
     */
    public enum TokenType {
        IDENFR,INTCON,STRCON,CHRCON,MAINTK,CONSTTK,INTTK,CHARTK,BREAKTK,CONTINUETK,
        IFTK,ELSETK,NOT,AND,OR,FORTK,GETINTTK,GETCHARTK,PRINTFTK,RETURNTK,
        PLUS,MINU,VOIDTK,MULT,DIV,MOD,LSS,LEQ,GRE,GEQ,EQL,NEQ,ASSIGN,SEMICN,
        COMMA,LPARENT,RPARENT,LBRACK,RBRACK,LBRACE,RBRACE
    }

    private TokenType type;
    private String value;
    private int lineNum;
    public Token(TokenType type, int lineNum, String value) {
        this.type = type;
        this.lineNum = lineNum;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public int getLineNum() {
        return lineNum;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type.toString() + " " + value;
    }

    public void print(){
        ParserAnalyze.parserOutput.output(toString());
    }
}
