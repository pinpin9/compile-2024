package frontend;

import error.ErrorHandler;
import node.CompUnit;
import settings.Settings;
import tools.IO;

public class SemanticAnalyze {
    private static SemanticAnalyze instance = new SemanticAnalyze();
    public static SemanticAnalyze getInstance() {
        return instance;
    }
    private CompUnit compUnit;
    // 错误处理
    private ErrorHandler errorHandler = ErrorHandler.getErrorHandler();
    // 输出符号表
    public static IO semanticOutput = new IO(Settings.semanticFile);

    public void analyze(CompUnit compUnit){
        this.compUnit = compUnit;

    }
}
