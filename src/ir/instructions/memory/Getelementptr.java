package ir.instructions.memory;

import ir.BasicBlock;
import ir.Value;
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
}