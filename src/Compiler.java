import java.io.IOException;

import frontend.LexicalAnalyze;
import frontend.ParserAnalyze;
import error.ErrorHandler;
import frontend.SemanticAnalyze;
import ir.IrBuilder;
import tools.IO;

public class Compiler {

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
        SemanticAnalyze.getInstance().print();

        //============错误输出=============
        if (ErrorHandler.getErrorHandler().getIsError()){
            ErrorHandler.getErrorHandler().print();
            return;
        }

        //============中间代码=============
        IrBuilder.getInstance().buildIr(ParserAnalyze.getInstance().getResult());
        IrBuilder.getInstance().print();
    }
}
