package ir.instructions.binary;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsImme;
import backend.operands.MipsOperand;
import backend.operands.MipsPhyReg;
import backend.values.MipsBasicBlock;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.types.ValueType;

public class Srem extends BinaryInstruction {
    public Srem(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("srem", valueType, name, parent, op1, op2);
    }

    @Override
    public void buildMips() {
        MipsBuilder builder = MipsBuilder.getInstance();
        MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
        builder.buildMove(dst, MipsPhyReg.ZERO, getParent());
    }
}
