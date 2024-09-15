package frontend;

import frontend.token.Token;

import java.util.List;

// 词法分析
public class LexicalAnalyze {
    public LexicalAnalyze() {}
    // 单例模式
    private static LexicalAnalyze instance = new LexicalAnalyze();
    public static LexicalAnalyze getInstance() {
        return instance;
    }

    private List<Token> tokens;

    // 返回结果
    public List<Token> getTokens() {
        return tokens;
    }

    public void analyze(String sourceCode) {
        // 词法分析
        int len = sourceCode.length(); // 源码长度
        int lineNum=1; // 行号
        for(int i=0;i<len;i++){
            char c = sourceCode.charAt(i);
            if(c=='\n'){
                lineNum++;
            }
            if(c==' '||c=='\n'||c=='\t'||c=='\r'){
                continue;
            }

        }

    }
    private void getToken(){

    }
}
