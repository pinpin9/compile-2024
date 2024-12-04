package ir.instructions.memory;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsOperand;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.instructions.Instruction;
import ir.types.PointerType;
import ir.types.ValueType;

import java.util.ArrayList;

// <result> = load <ty>, <ty>* <pointer>
public class Load extends Instruction {
    private ValueType loadType; // load出来的值类型
    public Load(String name, BasicBlock basicBlock, Value pointer){
        super(((PointerType)pointer.getValueType()).getPointingType(), name, basicBlock,new ArrayList<>(){{
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

    @Override
    public void buildMips() {
        MipsBuilder builder = MipsBuilder.getInstance();
        MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
        MipsOperand base = builder.buildOperand(getOperands().get(0), false, Mc.curIrFunction, getParent());
        MipsOperand offset = builder.buildImmeOperand(0, true, Mc.curIrFunction, getParent());
        builder.buildLoad(dst, base, offset, getParent());
    }
}
