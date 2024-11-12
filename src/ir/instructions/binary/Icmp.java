package ir.instructions.binary;


import ir.BasicBlock;
import ir.Value;
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

    private String getCond(Cond cond){
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
                append(getCond(cond)).append(" i32").append(" ").append(getOperands().get(0).
                        getName()).append(", ").append(getOperands().get(1).getName());
        return stringBuilder.toString();
    }
}
