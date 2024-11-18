package backend;

import ir.BasicBlock;
import ir.Function;

import java.util.HashMap;

public class Mc {
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
