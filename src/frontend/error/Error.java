package frontend.error;

public class Error {
    public enum ErrorType {
        a,b,c,d,e,f,g,h,i,j,k,l,m
    }
    private ErrorType errorType;
    private int lineNum;
    private String value;

    public Error(ErrorType errorType,int lineNum,String value){
        this.errorType=errorType;
        this.lineNum=lineNum;
        this.value=value;
    }
    @Override
    public String toString(){
        return lineNum + " " + errorType + "\n";
    }
}
