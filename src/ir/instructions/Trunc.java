package ir.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.operands.MipsImme;
import backend.operands.MipsOperand;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.types.IntType;

import java.util.ArrayList;

/**
 * @author zlp
 * @Discription char和int类型的值相加的时候，会先将char转为int，保存的时候将int转为char
 * @date 2024/11/02
 */
// %5 = trunc i32 %4 to i8
// <result> = trunc <ty> <value> to <ty2>
public class Trunc extends Instruction{
    public Trunc(String name, BasicBlock parent, Value value){
        super(new IntType(8),name, parent, new ArrayList<>(){{
            add(value);
        }});
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = trunc i32 ");
        stringBuilder.append(getOperands().get(0).getName()).append(" to i8");
        return stringBuilder.toString();
    }

    @Override
    public void buildMips() {
        Value value = getOperands().get(0);
        // 好像为char的赋值不会超过127
//        MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
//        MipsOperand src = builder.buildOperand(value, false, Mc.curIrFunction, getParent());
//        MipsOperand num = new MipsImme(0xff);
//        // i32 -> i8
//        builder.buildBinary(MipsBinary.BinaryType.AND, dst, src, num, getParent());
        Mc.addOperandMap(this, Mc.getMappedValue(value));
    }
}
