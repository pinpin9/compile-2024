package ir.instructions.memory;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import backend.operands.MipsPhyReg;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.instructions.Instruction;
import ir.types.ArrayType;
import ir.types.PointerType;
import ir.types.ValueType;

import java.util.ArrayList;

// <result> = getelementptr <ty>, * {, [inrange] <ty> <idx>}*
// <result> = getelementptr inbounds <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*
public class Getelementptr extends Instruction {
    ValueType baseType;
    // base是一个指针类型，指向int | char类型的数据，获取以base为基准，偏移为idx的指针地址
    public Getelementptr(String name, BasicBlock block, Value base, Value index) {
        super(base.getValueType(), name, block, new ArrayList<>(){{
            add(base);
            add(index);
        }});
        this.baseType = ((PointerType)base.getValueType()).getPointingType(); // IntType | CharType
    }

    // base是一个指针类型，指向数组类型的数据，一般为一个alloca指令
    // alloca为数组分配空间时，指令返回一个指针，指向数组地址
    // valueType为数组存储的元素类型
    public Getelementptr(String name, BasicBlock block, Value base, Value firstIndex, Value secondIndex){
        super(new PointerType(((ArrayType)((PointerType)base.getValueType()).getPointingType()).getValueType()), name, block, new ArrayList<>(){{
            add(base);
            add(firstIndex);
            add(secondIndex);
        }});
        this.baseType = ((PointerType)base.getValueType()).getPointingType(); // ArrayType
    }

    // %2 = getelementptr inbounds [3 x i32], [3 x i32]* %1, i32 0, i32 0
    // 三个参数，第一个参数为为数组分配的空间，获取指针变量，%2为指针类型，获取数组的首地址
    // %3 = getelementptr inbounds i32, i32* %2, i32 1
    // 两个参数，第一个参数为数组首地址，第二个参数为获取的下标值，获取数组中某个元素的地址
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()+" = getelementptr inbounds ");
        stringBuilder.append(baseType).append(", ");
        for(int i = 0;i<getOperands().size();i++){
            stringBuilder.append(getOperands().get(i).getValueType()+" "+getOperands().get(i).getName());
            if(i<getOperands().size()-1){
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.toString();
    }

    MipsBuilder builder = MipsBuilder.getInstance();
    @Override
    public void buildMips() {
        Value irBase = getOperands().get(0);
        Value irOffset1 = getOperands().get(1);

        MipsOperand base = builder.buildOperand(irBase, false, Mc.curIrFunction, getParent());
        MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
        if(getOperands().size() == 2){ // 只有一个偏移
            handleCal(dst, MipsPhyReg.AT, base, irOffset1, baseType);
        } else { // 有两个偏移
            // 因为是一维数组，所以第一个偏移量一定是0
            MipsOperand offset1 = builder.buildImmeOperand(0, true, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, offset1, getParent());
            ValueType valueType = ((ArrayType)baseType).getValueType();
            handleCal(dst, MipsPhyReg.AT, dst, getOperands().get(2), valueType);
        }
    }

    private void handleCal(MipsOperand dst, MipsOperand mid, MipsOperand base, Value irOffset, ValueType valueType){
        int valueSize = valueType.getSize();
        if(valueSize == 1){
            valueSize = 4;
        }

        if(irOffset instanceof Constant){
            int dim;
            if(irOffset instanceof ConstInt){
                dim = ((ConstInt) irOffset).getValue();
            } else{
                dim = ((ConstChar) irOffset).getValue();
            }
            int totalOffset = valueSize*dim;
            MipsOperand totalOffsetOp = builder.buildImmeOperand(totalOffset, true, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, totalOffsetOp, getParent());
        } else {
            MipsOperand mul1 = builder.buildOperand(irOffset, false, Mc.curIrFunction, getParent());
            MipsOperand mul2 = builder.buildImmeOperand(valueSize, true, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.MUL, mid, mul1, mul2, getParent());
            builder.buildBinary(MipsBinary.BinaryType.ADDU, dst, base, mid, getParent());
        }
    }
}