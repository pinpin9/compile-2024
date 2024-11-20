package ir.instructions.binary;

import ir.values.BasicBlock;
import ir.values.Value;
import ir.types.ValueType;

public class Add extends BinaryInstruction {
    public Add(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("add", valueType, name, parent, op1, op2);
    }
}
