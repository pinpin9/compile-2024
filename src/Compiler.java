import java.io.IOException;

import frontend.LexicalAnalyze;
import frontend.error.ErrorNode;
import tools.IO;

public class Compiler {
    public static void main(String[] args) throws IOException {
        LexicalAnalyze.getInstance().analyze(IO.getInput());
        if(ErrorNode.getErrorNode().getIsError()){
            ErrorNode.getErrorNode().printErrorList();
        }else{
            LexicalAnalyze.getInstance().printTokenList();
        }
    }
}
