import java.io.IOException;

import frontend.LexicalAnalyze;
import frontend.SyntaxAnalyze;
import frontend.error.ErrorNode;
import tools.IO;

public class Compiler {
    public static void main(String[] args) throws IOException {
        // 词法分析
        LexicalAnalyze.getInstance().analyze(IO.getInput());
        if(ErrorNode.getErrorNode().getIsError()){
            ErrorNode.getErrorNode().printErrorList();
        }else{
            LexicalAnalyze.getInstance().printTokenList();
        }

        // 语法分析
//        SyntaxAnalyze.getInstance().analyze();
    }
}
