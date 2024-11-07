package ir.instructions.binary;

import ir.BasicBlock;
import ir.Value;
import ir.types.ValueType;

public class Sdiv extends BinaryInstruction {

    public Sdiv(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("sdiv", valueType, "%v" + name, parent, op1, op2);
    }
}
