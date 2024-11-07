package ir.instructions;

import ir.BasicBlock;
import ir.User;
import ir.Value;
import ir.types.ValueType;

import java.util.ArrayList;

public class Instruction extends User {
    public Instruction(ValueType valueType, String name, BasicBlock basicBlock, ArrayList<Value> operands) {
        super(valueType, name, basicBlock, operands);
    }
}
