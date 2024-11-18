package backend;

import ir.GlobalVariable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zlp
 * @Discription 目标代码树的根节点Module
 * @date 2024/11/18
 */
public class MipsModule {
    // 全局唯一，单例模型
    private static MipsModule module = new MipsModule();
    public static MipsModule getModule(){
        return module;
    }

    // 一个module模块中有多个Function和GlobalVariable
    private List<MipsFunction> mipsFunctions = new ArrayList<>();
    private List<MipsGlobalVariable> mipsGlobalVariables = new ArrayList<>();

    // 只有唯一的mainFunction
    public static MipsFunction mainFunction = null;

    public void addMipsFunction(MipsFunction mipsFunction){
        if(mipsFunction.getName().equals("main")){
            mainFunction = mipsFunction;
        }
        mipsFunctions.add(mipsFunction);
    }
    public void addGlobalVariable(MipsGlobalVariable mipsGlobalVariable){
        mipsGlobalVariables.add(mipsGlobalVariable);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("# pinpin9 2024/11/18\n");
        // getint
        stringBuilder.append(".macro getint\n");
        stringBuilder.append("\tli $v0, 5\n");
        stringBuilder.append("\tsyscall\n");
        stringBuilder.append(".end_macro\n\n");
        // getchar
        stringBuilder.append(".macro getchar\n");
        stringBuilder.append("\tli $v0, 5\n");
        stringBuilder.append("\tsyscall\n");
        stringBuilder.append(".end_macro\n\n");
        // putint
        stringBuilder.append(".macro putint\n");
        stringBuilder.append("\tli $v0, 1\n");
        stringBuilder.append("\tsyscall\n");
        stringBuilder.append(".end_macro\n\n");
        // putch
        stringBuilder.append(".macro putch\n");
        stringBuilder.append("\tli $v0, 11\n");
        stringBuilder.append("\tsyscall\n");
        stringBuilder.append(".end_macro\n\n");
        // putstr
        stringBuilder.append(".macro putstr\n");
        stringBuilder.append("\tli $v0, 4\n");
        stringBuilder.append("\tsyscall\n");
        stringBuilder.append(".end_macro\n\n");

        // .data段
        stringBuilder.append(".data\n");
        // .word | .byte
        for(MipsGlobalVariable mipsGlobalVariable:mipsGlobalVariables){
            if(mipsGlobalVariable.isHasInit()){
                stringBuilder.append(mipsGlobalVariable);
            }
        }
        // .space
        for (MipsGlobalVariable mipsGlobalVariable:mipsGlobalVariables){
            if(!mipsGlobalVariable.isHasInit()&&!mipsGlobalVariable.isStr()){
                stringBuilder.append(mipsGlobalVariable);
            }
        }
        // .asciiz
        for (MipsGlobalVariable mipsGlobalVariable:mipsGlobalVariables){
            if(mipsGlobalVariable.isStr()){
                stringBuilder.append(mipsGlobalVariable);
            }
        }
        stringBuilder.append("\n");

        // .text段
        stringBuilder.append(".text\n");

        stringBuilder.append(mainFunction);
        for(MipsFunction mipsFunction:mipsFunctions){
            if(mipsFunction != mainFunction){
                stringBuilder.append(mipsFunction).append("\n");
            }
        }

        return stringBuilder.toString();
    }
}
