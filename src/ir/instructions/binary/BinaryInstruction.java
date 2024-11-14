package ir.instructions.binary;

import ir.BasicBlock;
import ir.Value;
import ir.instructions.Instruction;
import ir.types.ValueType;

import java.util.ArrayList;

public class BinaryInstruction extends Instruction {
    private String insType; // 指令类型，如add | sub | srem | sdiv | mul
    public BinaryInstruction(String insType,ValueType valueType, String name, BasicBlock parent, Value op1, Value op2){
        super(valueType, name, parent, new ArrayList<>(){{
            add(op1);
            add(op2);
        }});
        this.insType = insType;
    }

    // %8 = add i32 %7, 5
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = ").append(insType).append(" ").append(getValueType()).append(" ").append(getOperands().get(0).getName()).append(", ").append(getOperands().get(1).getName());
        return stringBuilder.toString();
    }
}
