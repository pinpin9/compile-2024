package ir.instructions.binary;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsImme;
import backend.operands.MipsOperand;
import backend.values.MipsBasicBlock;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.types.ValueType;

public class Sub extends BinaryInstruction {
    public Sub(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("sub", valueType, name, parent, op1, op2);
    }

    @Override
    public void buildMips() {
        MipsBuilder builder = MipsBuilder.getInstance();
        Value op1 = getOp1();
        Value op2 = getOp2();
        MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
        MipsOperand src1, src2;
        if(op1 instanceof Constant && op2 instanceof Constant){ // 全是常数
            int val1 = op1 instanceof ConstInt ? ((ConstInt)op1).getValue() : ((ConstChar)op1).getValue();
            int val2 = op2 instanceof ConstInt ? ((ConstInt)op2).getValue() : ((ConstChar)op2).getValue();
            int result = val1 - val2;
            builder.buildMove(dst, new MipsImme(result), getParent());
        } else if (op2 instanceof Constant) {
            src1 = builder.buildOperand(op1, false, Mc.curIrFunction, getParent());
            src2 = builder.buildOperand(op2, true, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.SUBU, dst, src1, src2, getParent());
        } else {
            src1 = builder.buildOperand(op1, false, Mc.curIrFunction, getParent());
            src2 = builder.buildOperand(op2, true, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.SUBU, dst, src1, src2, getParent());
        }
    }
}
