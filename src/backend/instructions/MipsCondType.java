package backend.instructions;

import ir.instructions.binary.Icmp;

import java.util.HashMap;

public enum MipsCondType{
    EQ("==", "eq"), // ==
    NE("!=", "ne"), // !=
    LE("<=","le"), // <=
    LT("<", "lt"), // <
    GE(">=", "ge"), // >=
    GT(">", "gt"); // >

    String operation;
    String name;
    MipsCondType(String operation, String name){
        this.operation = operation;
        this.name = name;
    }

    public static HashMap<Icmp.Cond, MipsCondType> ir2MipsCond = new HashMap<>(){{
       put(Icmp.Cond.EQ, EQ);
       put(Icmp.Cond.NE, NE);
       put(Icmp.Cond.SGE, GE);
       put(Icmp.Cond.SGT, GT);
       put(Icmp.Cond.SLE, LE);
       put(Icmp.Cond.SLT, LT);
    }};

    public static MipsCondType getType(Icmp.Cond cond){ // 从ir到mips的映射
        return ir2MipsCond.get(cond);
    }

    public static int calCompare(MipsCondType cond, int op1, int op2){ // 获取运算结果
        boolean result;
        switch (cond){
            case NE -> result = op1 != op2;
            case EQ -> result = op1 == op2;
            case GE -> result = op1 >= op2;
            case GT -> result = op1 > op2;
            case LE -> result = op1 <= op2;
            case LT -> result = op1 < op2;
            default -> result = false;
        }
        return result ? 1:0;
    }

    public static MipsCondType getOppCondType(MipsCondType cond){
        return switch (cond){
            case EQ -> EQ;
            case NE -> NE;
            case LT -> GT;
            case LE -> GE;
            case GT -> LT;
            case GE -> LE;
        };
    }

    @Override
    public String toString() {
        return name;
    }
}
