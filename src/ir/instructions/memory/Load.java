package ir.instructions.memory;

import ir.BasicBlock;
import ir.Value;
import ir.instructions.Instruction;
import ir.types.PointerType;
import ir.types.ValueType;
import ir.types.VoidType;

import java.util.ArrayList;

// <result> = load <ty>, <ty>* <pointer>
public class Load extends Instruction {
    private ValueType loadType; // load出来的值类型
    public Load(String name, BasicBlock basicBlock, Value pointer){
        super(((PointerType)pointer.getValueType()).getPointingType(), "%v" + name, basicBlock,new ArrayList<>(){{
            add(pointer);
        }});
        loadType = ((PointerType)pointer.getValueType()).getPointingType();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = load ");
        stringBuilder.append(loadType).append(", ");
        stringBuilder.append(loadType).append("* ").append(getOperands().get(0).getName());
        return stringBuilder.toString();
    }
}
