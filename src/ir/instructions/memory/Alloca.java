package ir.instructions.memory;

import ir.BasicBlock;
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
        super(new PointerType(allocatedType),"%v" + name, basicBlock, new ArrayList<>());
        this.allocatedType = allocatedType;
    }
    // 有初始值
    public Alloca(ValueType allocatedType, String name, BasicBlock basicBlock, ConstArray constArray){
        super(new PointerType(allocatedType), "%v" + name, basicBlock, new ArrayList<>());
        this.allocatedType = allocatedType;
        this.constArray = constArray;
    }

    @Override
    public String toString() {
        return getName()+" = alloca " + allocatedType;
    }
}
