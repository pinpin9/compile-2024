package error;

public class Error implements Comparable<Error>{
    @Override
    public int compareTo(Error o) {
        return this.lineNum-o.lineNum;
    }

    public enum ErrorType {
        a, // 非法符号
        b, // 名字重定义
        c, // 未定义的名字
        d, // 函数参数个数不匹配
        e, // 函数参数类型不匹配
        f, // 无返回值的函数存在不匹配的return语句
        g, // 有返回值的函数缺少return语句
        h, // 不能改变常量的值
        i, // 缺少分号
        j, // 缺少右小括号’)’
        k, // 缺少右中括号’]’
        l, // printf中格式字符与表达式个数不匹配
        m, // 在非循环块中使用break和continue语句
    }
    private ErrorType errorType;
    private int lineNum;

    public Error(ErrorType errorType,int lineNum){
        this.errorType=errorType;
        this.lineNum=lineNum;
    }
    @Override
    public String toString(){
        return lineNum + " " + errorType;
    }
    @Override
    public boolean equals(Object o){
        Error error = (Error) o;
        return this.lineNum==error.lineNum&&this.errorType==error.errorType;
    }
}
