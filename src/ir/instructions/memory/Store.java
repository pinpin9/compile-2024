package ir.instructions.memory;

import ir.BasicBlock;
import ir.Value;
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
}
