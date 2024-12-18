package ir.values;

import backend.Mc;
import backend.values.MipsBasicBlock;
import backend.MipsBuilder;
import ir.types.VoidType;

import java.util.ArrayList;
import java.util.List;

public class Module extends Value {
    // 全局只有一个Module，所以为单例模式
    private static Module module = new Module();

    public Module() {
        super(new VoidType(),"module",null);
    }

    public static Module getInstance(){
        return module;
    }
    // 函数模块
    private List<Function> functionList = new ArrayList<>();
    // 全局变量模块
    private List<GlobalVariable> globalVariableList = new ArrayList<>();

    // 添加函数定义
    public void addFunction(Function function){
        functionList.add(function);
    }
    // 添加全局变量声明
    public void addGlobalVariable(GlobalVariable globalVariable){
        globalVariableList.add(globalVariable);
    }

    public List<Function> getFunctionList() {
        return functionList;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for(GlobalVariable globalVariable:globalVariableList){
            stringBuilder.append(globalVariable);
            stringBuilder.append("\n");
        }
        for (Function function:functionList){
            stringBuilder.append(function);
            stringBuilder.append("\n");
        }
        return stringBuilder.toString();
    }


    //==========目标代码生成==========
    public void buildMips(){
        for(GlobalVariable globalVariable:globalVariableList){
            globalVariable.buildMips();
        }

        /**构建函数和基本块从Ir到Mips的映射
         * 因为在构建开始之前需要知道每个基本块之间的前驱，后继关系
         * 如果依次构造，可能在遇到后继块的时候还没有进行解析
         * 所以需要先将所有的 Block 解析完成，方便之后的细致解析
         */
        buildFunctionAndBlockMaps();

        for(Function function:functionList){
            function.buildMips();
        }
    }

    MipsBuilder mipsBuilder = MipsBuilder.getInstance();
    private void buildFunctionAndBlockMaps(){
        for(Function irFunction : functionList){
            mipsBuilder.buildFunction(irFunction);

            List<BasicBlock> basicBlockList = irFunction.getBasicBlockList();
            for(BasicBlock irBlock:basicBlockList){
                MipsBasicBlock mipsBasicBlock = new MipsBasicBlock(irBlock.getName(), irBlock.getLoopDepth()); // 新建一个Mips基本块
                Mc.addBasicBlockMap(irBlock, mipsBasicBlock); // 增加ir基本块到mips基本块的映射
            }
            // 存储前继Mips基本块的信息
            for(BasicBlock irBlock:basicBlockList){
                MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(irBlock);
                for(BasicBlock preBlock : irBlock.getPreBlocks()){
                    mipsBasicBlock.addPreBlock(Mc.getMappedBlock(preBlock));
                }
            }
        }
    }
}
