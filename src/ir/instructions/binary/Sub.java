package ir.instructions.binary;

import ir.BasicBlock;
import ir.Value;
import ir.types.ValueType;

public class Sub extends BinaryInstruction {
    public Sub(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("sub", valueType, name, parent, op1, op2);
    }
}
