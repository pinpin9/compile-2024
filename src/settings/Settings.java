package settings;

public class Settings {
    public static final String inputFile="testfile.txt";
    public static final String lexerFile="lexer.txt";
    public static final String errorFile="error.txt";
    public static final String parserFile="parser.txt";
    public static final String semanticFile="symbol.txt";
    public static final String llvmFile="llvm_ir.txt";
    public static final String mipsFile="mips.txt";

    // 是否生成mips
    public static final boolean generateMips = true;

    // 是否开启图着色寄存器分配
    public static final boolean openRegAlloc = true;

    // 是否开启窥孔优化
    public static final boolean openPeephole = false;
}
