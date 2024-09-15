package frontend.token;

public class Token {
    /**
     * 词法单元类型
     */
    public enum TokenType {
        // 保留字
        BEGIN, END, FOR, IF, THEN, ELSE, WHILE, DO, READ, WRITE,
        // 运算符
        PLUS, MINUS, TIMES, DIVIDE, EQUAL, UNEQUAL, LESS, LESS_EQUAL, GREATER, GREATER_EQUAL,
        // 分隔符
        LPAREN, RPAREN, SEMI, COMMA, ASSIGN,
        // 标识符
        IDENTIFIER,
        // 常数
        NUMBER,
        // 文件结束符
        EOF
    }

    private TokenType type;
    private String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return type + " " + value+ "\n";
    }
}
