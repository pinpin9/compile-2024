import java.io.IOException;
import java.util.Collections;

import frontend.LexicalAnalyze;
import frontend.ParserAnalyze;
import error.ErrorHandler;
import tools.IO;

public class Compiler {

    public static void error() throws IOException {
        Collections.sort(ErrorHandler.getErrorHandler().getErrorList());
        if (ErrorHandler.getErrorHandler().getIsError()){
            ErrorHandler.getErrorHandler().printErrorList();
        }
    }
    public static void main(String[] args) throws IOException {
        // 读输入
        String input = IO.getInput();
        //============词法分析=============
        LexicalAnalyze.getInstance().analyze(input);
        LexicalAnalyze.getInstance().print();

        //============语法分析=============
        ParserAnalyze.getInstance().analyze(LexicalAnalyze.getInstance().getTokens());
        ParserAnalyze.getInstance().print();
        error();
    }
}
