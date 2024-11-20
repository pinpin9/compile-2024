package backend;

import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;
import ir.values.BasicBlock;
import ir.values.Function;

import java.util.HashMap;

public class Mc {
    public static Function curIrFunction = null; // 当前正在解析的中间函数块
    public static MipsFunction curFunction = null; // 当前正在进行解析的函数块

    // 从Ir Function到Mips Function的映射
    public static HashMap<Function, MipsFunction> functionMap = new HashMap<>();
    // 从Ir BasicBlock到Mips BasicBlock的映射
    public static HashMap<BasicBlock, MipsBasicBlock> basicBlockMap = new HashMap<>();
    public static void addFunctionMap(Function irFunction, MipsFunction mipsFunction){
        functionMap.put(irFunction, mipsFunction);
    }
    public static void addBasicBlockMap(BasicBlock irBlock, MipsBasicBlock mipsBasicBlock){
        basicBlockMap.put(irBlock, mipsBasicBlock);
    }
    public static MipsFunction getMappedFunction(Function irFunction){
        return functionMap.get(irFunction);
    }
    public static MipsBasicBlock getMappedBlock(BasicBlock irBlock){
        return basicBlockMap.get(irBlock);
    }
}
