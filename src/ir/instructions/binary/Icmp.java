package ir.instructions.binary;


import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsCondType;
import backend.operands.MipsImme;
import backend.operands.MipsOperand;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.instructions.Instruction;
import ir.types.IntType;

import java.util.ArrayList;

/**
 * @author zlp
 * @Discription 比较，返回的结果是bool类型
 * @date 2024/11/02
 */
// %6 = icmp ne i32 %5, 0
// <result> = icmp <cond> <ty> <op1>, <op2>
public class Icmp extends Instruction {
    private Cond cond;
    public enum Cond{
        EQ,NE, // 相等和不相等
        SGT,SGE,SLT,SLE; // 有符号比较
    }
    public Icmp(Cond cond, String name, BasicBlock parent, Value op1, Value op2){
        super(new IntType(1), name, parent, new ArrayList<>(){{
            add(op1);
            add(op2);
        }});
        this.cond = cond;
    }

    public Cond getCond(){
        return cond;
    }

    private String getCondString(Cond cond){
        switch (cond){
            case EQ -> {
                return "eq";
            }
            case NE -> {
                return "ne";
            }
            case SGE -> {
                return "sge";
            }
            case SGT -> {
                return "sgt";
            }
            case SLE -> {
                return "sle";
            }
            case SLT -> {
                return "slt";
            }
        }
        return null;
    }
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = ").append("icmp ").
                append(getCondString(cond)).append(" i32").append(" ").append(getOperands().get(0).
                        getName()).append(", ").append(getOperands().get(1).getName());
        return stringBuilder.toString();
    }


    @Override
    public void buildMips() {

    }

    // zext指令的icmp
    public void build(){
        MipsBuilder builder = MipsBuilder.getInstance();

        Value value1 = getOperands().get(0);
        Value value2 = getOperands().get(1);

        MipsCondType cond = MipsCondType.getType(getCond());

        // 如果两个操作数都是常量，则直接计算出值
        if(value1 instanceof Constant && value2 instanceof Constant){
            int op1 = ((ConstInt)value1).getValue();
            int op2 = ((ConstInt)value2).getValue();
            int res = MipsCondType.calCompare(cond, op1, op2);
            MipsOperand imme = builder.buildImmeOperand(res, true, Mc.curIrFunction, getParent());
            Mc.addOperandMap(this, imme);
        } else {
            MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
            MipsOperand src1 = builder.buildOperand(value1, false, Mc.curIrFunction, getParent());
            MipsOperand src2 = builder.buildOperand(value2, false, Mc.curIrFunction, getParent());
            builder.buildCompare(cond, dst, src1, src2, getParent());
        }
    }
}
