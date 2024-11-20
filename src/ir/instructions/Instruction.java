package ir.instructions;

import ir.values.BasicBlock;
import ir.values.User;
import ir.values.Value;
import ir.types.ValueType;

import java.util.ArrayList;

public class Instruction extends User {
    public Instruction(ValueType valueType, String name, BasicBlock basicBlock, ArrayList<Value> operands) {
        super(valueType, name, basicBlock, operands);
    }
    public void delFromParent(Instruction instruction){
        ((BasicBlock)getParent()).delInstruction(instruction);
    }

    @Override
    public BasicBlock getParent() {
        return (BasicBlock) super.getParent();
    }
}
