package backend;

import backend.operands.MipsOperand;
import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.HashMap;

public class Mc {
    public static Function curIrFunction = null; // 当前正在解析的中间函数块

    // 从Ir Function到Mips Function的映射
    public static HashMap<Function, MipsFunction> functionMap = new HashMap<>();
    // 从Ir BasicBlock到Mips BasicBlock的映射
    public static HashMap<BasicBlock, MipsBasicBlock> basicBlockMap = new HashMap<>();
    // 记录Ir操作数对应的Mips操作数
    public static HashMap<Value, MipsOperand> operandMap = new HashMap<>();

    public static void addFunctionMap(Function irFunction, MipsFunction mipsFunction){
        functionMap.put(irFunction, mipsFunction);
    }
    public static void addBasicBlockMap(BasicBlock irBlock, MipsBasicBlock mipsBasicBlock){
        basicBlockMap.put(irBlock, mipsBasicBlock);
    }
    public static void addOperandMap(Value value, MipsOperand op){
        operandMap.put(value, op);
    }
    public static MipsFunction getMappedFunction(Function irFunction){
        return functionMap.get(irFunction);
    }
    public static MipsBasicBlock getMappedBlock(BasicBlock irBlock){
        return basicBlockMap.get(irBlock);
    }
    public static MipsOperand getMappedValue(Value value){
        return operandMap.get(value);
    }
    public static MipsFunction getCurFunction(){
        return getMappedFunction(curIrFunction);
    }
}
