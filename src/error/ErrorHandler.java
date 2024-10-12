package error;

import settings.Settings;
import tools.IO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//错误处理的过程：向ErrorList中添加一个error对象，改变isError的值，表示出错

public class ErrorHandler {
    private static ErrorHandler instance = new ErrorHandler();

    public static ErrorHandler getErrorHandler() {
        return instance;
    }
    private boolean isError=false;

    public boolean getIsError(){
        return isError;
    }
    private List<Error> errorList=new ArrayList<>();

    public List<Error> getErrorList(){
        Collections.sort(errorList);
        return errorList;
    }
    public void addError(Error e){
        errorList.add(e);
        isError=true;
    }
    public void print() throws IOException {
        IO io = new IO(Settings.errorFile);
        io.output(getErrorList());
    }
}
