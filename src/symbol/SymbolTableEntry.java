package symbol;

public class SymbolTableEntry {
    public enum Type{

    }
    private String name;
    private Type type;
    private int lineNum;
    private int scopeLevel = 0; //作用范围
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

}
