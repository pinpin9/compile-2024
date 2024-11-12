package ir.instructions.binary;

import ir.BasicBlock;
import ir.Value;
import ir.types.ValueType;

public class Srem extends BinaryInstruction {
    public Srem(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("srem", valueType, name, parent, op1, op2);
    }
}
