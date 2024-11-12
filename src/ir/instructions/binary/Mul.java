package ir.instructions.binary;

import ir.BasicBlock;
import ir.Value;
import ir.types.ValueType;

public class Mul extends BinaryInstruction {

    public Mul(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("mul", valueType, name, parent, op1, op2);
    }
}
