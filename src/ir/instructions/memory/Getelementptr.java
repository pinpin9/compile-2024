package ir.instructions.memory;

import ir.BasicBlock;
import ir.Value;
import ir.instructions.Instruction;
import ir.types.ValueType;

import java.util.ArrayList;

// <result> = getelementptr <ty>, * {, [inrange] <ty> <idx>}*
// <result> = getelementptr inbounds <ty>, <ty>* <ptrval>{, [inrange] <ty> <idx>}*
public class Getelementptr extends Instruction {

    public Getelementptr(ValueType valueType, String name, BasicBlock basicBlock, ArrayList<Value> operands) {
        super(valueType, name, basicBlock, operands);
    }
}
