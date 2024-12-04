package ir.instructions.memory;

import backend.Mc;
import backend.MipsBuilder;
import backend.operands.MipsOperand;
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
        MipsOperand src = builder.buildOperand(getOperands().get(0), false, Mc.curIrFunction, getParent());
        MipsOperand addr = builder.buildOperand(getOperands().get(1), false, Mc.curIrFunction, getParent());
        MipsOperand offset = builder.buildImmeOperand(0, true, Mc.curIrFunction, getParent());
        builder.buildStore(src, offset, addr, getParent());
    }
}
