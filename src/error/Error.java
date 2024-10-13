package error;

public class Error implements Comparable<Error>{
    @Override
    public int compareTo(Error o) {
        return this.lineNum-o.lineNum;
    }

    public enum ErrorType {
        a, //&&或||
        b,
        c,
        d,
        e,f,g,h,
        i,//;
        j,//]
        k,//)
        l,
        m
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
