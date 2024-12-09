package ir.instructions.memory;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import ir.types.CharType;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.instructions.Instruction;
import ir.types.VoidType;

import java.util.ArrayList;

// store <ty> <value>, <ty>* <pointer>
// store i32 %0, i32* %3
public class Store extends Instruction {
    public Store(BasicBlock parent, Value value, Value pointer){
        super(new VoidType(), " ", parent, new ArrayList<>(){{
            add(value);
            add(pointer);
        }});
    }

    @Override
    public String toString() {
        return "store "+getOperands().get(0).getValueType() + " " + getOperands().get(0).getName() + ", "
                + getOperands().get(1).getValueType() + " " + getOperands().get(1).getName();
    }


    @Override
    public void buildMips() {
        MipsBuilder builder = MipsBuilder.getInstance();
        Value op1 = getOperands().get(0);
        MipsOperand src,addr,offset;
        src = builder.buildOperand(getOperands().get(0), false, Mc.curIrFunction, getParent());
        addr = builder.buildOperand(getOperands().get(1), false, Mc.curIrFunction, getParent());
        offset = builder.buildImmeOperand(0, true, Mc.curIrFunction, getParent());
        if(op1.getValueType() instanceof CharType){
            builder.buildBinary(MipsBinary.BinaryType.AND, src, src, builder.buildImmeOperand(0xff, true, Mc.curIrFunction, getParent()), getParent());
        }

        builder.buildStore(src, offset, addr, getParent());
    }
}
