package backend;

import ir.BasicBlock;
import ir.Function;
import ir.Module;
import settings.Settings;
import tools.IO;

import java.util.List;


public class MipsBuilder {
    private static MipsBuilder mipsBuilder = new MipsBuilder();
    public static MipsBuilder getInstance(){
        return mipsBuilder;
    }

    private MipsModule mipsModule = MipsModule.getModule();

    public void buildMips(Module irmodule){
        irmodule.buildMips();
    }

    public void buildGlobalVariable(String name, String content){ // 字符串
        MipsGlobalVariable mipsGlobalVariable = new MipsGlobalVariable(name, content);
        mipsModule.addGlobalVariable(mipsGlobalVariable);
    }

    public void buildGlobalVariable(String name, int size){ // 未初始化
        MipsGlobalVariable mipsGlobalVariable = new MipsGlobalVariable(name, size);
        mipsModule.addGlobalVariable(mipsGlobalVariable);
    }

    public void buildGlobalVariable(String name, MipsGlobalVariable.ValueType valueType, List<Integer> initValue){ // 初始化
        MipsGlobalVariable mipsGlobalVariable = new MipsGlobalVariable(name, valueType, initValue);
        mipsModule.addGlobalVariable(mipsGlobalVariable);
    }

    /**
     * 构造 mipsFunction
     * 在 mipsModule 中添加该 Function ，并添加从 irFunction 到 mipsFunction 的映射
     * @param irFunction 中间代码生成的函数模块
     */
    public void buildFunction(Function irFunction){
        MipsFunction mipsFunction = new MipsFunction(irFunction.getName(), irFunction.isLibFunc());
        mipsModule.addMipsFunction(mipsFunction);
        Mc.addFunctionMap(irFunction, mipsFunction);
    }



    // 输出
    private static IO mipsOutput = new IO(Settings.mipsFile);
    // 输出目标代码
    public void print(){
        mipsOutput.output(mipsModule.toString());
    }
}
