import java.io.IOException;

import frontend.LexicalAnalyze;
import frontend.ParserAnalyze;
import error.ErrorHandler;
import frontend.SemanticAnalyze;
import tools.IO;

public class Compiler {

    public static void error() throws IOException {
        if (ErrorHandler.getErrorHandler().getIsError()){
            ErrorHandler.getErrorHandler().print();
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

        //============语义分析=============
        SemanticAnalyze.getInstance().analyze(ParserAnalyze.getInstance().getResult());
    }
}
