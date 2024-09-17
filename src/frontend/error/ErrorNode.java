package frontend.error;

import settings.Settings;
import tools.IO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

//错误处理的过程：向ErrorList中添加一个error对象，改变isError的值，表示出错

public class ErrorNode {
    private static ErrorNode instance = new ErrorNode();

    public static ErrorNode getErrorNode() {
        return instance;
    }
    private boolean isError=false;
    public void changeIsError(){
        isError=true;
    }
    public boolean getIsError(){
        return isError;
    }
    private List<Error> errorList=new ArrayList<>();

    public void addError(Error error){
        errorList.add(error);
    }
    public List<Error> getErrorList(){
        return errorList;
    }
    public void printErrorList() throws IOException {
        IO.output(errorList, Settings.errorFile);
    }
}
