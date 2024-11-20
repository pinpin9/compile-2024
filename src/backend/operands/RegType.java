package backend.operands;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Mips共有32个寄存器
 */
public enum RegType {
    // 常量0, 只读
    ZERO(0, "zero"),

    // 汇编器保留寄存器, 用于伪指令
    AT(1, "at"),

    // v0-v1, 函数返回值或表达式计算结果
    V0(2, "v0"),
    V1(3, "v1"),

    // a0-a3, 函数调用时传参
    A0(4, "a0"),
    A1(5, "a1"),
    A2(6, "a2"),
    A3(7, "a3"),

    // t0-t7, 临时寄存器, 不保留函数调用间的值
    T0(8, "t0"),
    T1(9, "t1"),
    T2(10, "t2"),
    T3(11, "t3"),
    T4(12, "t4"),
    T5(13, "t5"),
    T6(14, "t6"),
    T7(15, "t7"),

    // s0-s7, 保存寄存器
    S0(16, "s0"),
    S1(17, "s1"),
    S2(18, "s2"),
    S3(19, "s3"),
    S4(20, "s4"),
    S5(21, "s5"),
    S6(22, "s6"),
    S7(23, "s7"),

    // t8-t9, 额外的临时寄存器
    T8(24, "t8"),
    T9(25, "t9"),

    // k0-k1, 内核保留寄存器, 用于异常处理
    K0(26, "k0"),
    K1(27, "k1"),

    // 全局数据段的基址
    GP(28, "gp"),

    // 栈顶指针
    SP(29, "sp"),

    // 栈帧指针, 函数调用时用于维护局部变量
    FP(30, "fp"),

    // 函数调用的返回地址
    RA(31, "ra");


    /**
     * 寄存器类有两个属性，编号和寄存器名称
     */
    private int index;
    private String name;
    RegType(int index, String name){
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return index;
    }
    public String getName() {
        return name;
    }

    public static HashMap<String, RegType> name2Type = new HashMap<>();
    /**
     * 进行函数调用时, 调用者需要保存的寄存器（现场）
     * 除 zero, at, v0, a0 ~ a3, sp
     */
    public static HashSet<RegType> regsNeedSave = new HashSet<>();
    /**
     * 进行寄存器分配时, 能够分配出去的寄存器
     * 即 zero, at, sp 以外的寄存器
     */
    public static HashSet<RegType> regsCanAlloc = new HashSet<>();

    // 获取寄存器类型
    public static RegType getRegType(String name){
        if(name2Type.containsKey(name)){
            return name2Type.get(name);
        }
        return null;
    }
    public static RegType getRegType(int index){
        if(index >= 0 && index < 32){
            return RegType.values()[index];
        }
        return null;
    }

    static { // 构建 Map 和 Set
        for(RegType regType : RegType.values()){
            name2Type.put(regType.name, regType);
            regsNeedSave.add(regType);
            regsCanAlloc.add(regType);
        }
        regsNeedSave.remove(ZERO);
        regsNeedSave.remove(AT);
        regsNeedSave.remove(V0);
        regsNeedSave.remove(A0);
        regsNeedSave.remove(A1);
        regsNeedSave.remove(A2);
        regsNeedSave.remove(A3);
        regsNeedSave.remove(SP);

        regsCanAlloc.remove(ZERO);
        regsCanAlloc.remove(AT);
        regsCanAlloc.remove(SP);
    }

    @Override
    public String toString() {
        return "$" + name;
    }
}
