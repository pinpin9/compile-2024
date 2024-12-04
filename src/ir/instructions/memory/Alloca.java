package ir.instructions.memory;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsPhyReg;
import backend.values.MipsFunction;
import ir.types.ArrayType;
import ir.types.CharType;
import ir.values.BasicBlock;
import ir.instructions.Instruction;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.types.constants.ConstArray;

import java.util.ArrayList;


/**
 * @author zlp
 * @Discription 申请内存空间指令，返回一个指针类型
 * @date 2024/11/02
 */
// %3 = alloca i32
public class Alloca extends Instruction {
    private ValueType allocatedType; // 指向的类型
    private ConstArray constArray = null; // 初始化值
    // 没有初始值
    public Alloca(ValueType allocatedType, String name, BasicBlock basicBlock) {
        super(new PointerType(allocatedType),name, basicBlock, new ArrayList<>());
        this.allocatedType = allocatedType;
    }
    // 有初始值
    public Alloca(ValueType allocatedType, String name, BasicBlock basicBlock, ConstArray constArray){
        super(new PointerType(allocatedType), name, basicBlock, new ArrayList<>());
        this.allocatedType = allocatedType;
        this.constArray = constArray;
    }

    public ValueType getAllocatedType(){
        return allocatedType;
    }

    @Override
    public String toString() {
        return getName()+" = alloca " + allocatedType;
    }

    @Override
    public void buildMips() {
        MipsFunction curFunction = Mc.getCurFunction();
        MipsBuilder builder = MipsBuilder.getInstance();
        // 当前在栈上分配的空间
        int allocatedSize = curFunction.getAllocatedSize();
        // 已经分配的空间
        MipsOperand allocatedSizeImme = builder.buildImmeOperand(allocatedSize, true, Mc.curIrFunction, getParent());
        int tmpSize = allocatedType.getSize();
        if(allocatedType instanceof CharType){
            tmpSize = tmpSize*4;
        } else if (allocatedType instanceof ArrayType && ((ArrayType)allocatedType).getValueType().isChar()) {
            tmpSize = tmpSize*4;
        }
        curFunction.allocSpace(tmpSize);

        // 当前alloca对应的Mips操作数对象中需要存栈中对应的地址
        // 为该alloca指令分配一个寄存器
        MipsOperand allocReg = builder.buildOperand(this, true, Mc.curIrFunction, getParent());
        builder.buildBinary(MipsBinary.BinaryType.ADDU, allocReg, MipsPhyReg.SP, allocatedSizeImme, getParent());
    }
}
