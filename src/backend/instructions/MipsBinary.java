package backend.instructions;

import backend.operands.MipsImme;
import backend.operands.MipsOperand;

public class MipsBinary extends MipsInstruction{
    public enum BinaryType{
        // 貌似无符号的计算更快, 并且无符号加减法不影响计算结果的补码形式
        /**
         * 无符号加法
         * 用法: ADDU rd, rs, rt
         */
        ADDU("addu"),

        /**
         * 无符号减法
         * 用法: SUBU rd, rs, rt
         */
        SUBU("subu"),

        /**
         * 有符号乘法
         * 用法: mul rd, rs, rt
         */
        MUL("mul"),

        /**
         * 有符号除法
         * 用法: div rd, rs, rt
         */
        DIV("div"),

        /**
         * 异或, 将两个寄存器中的内容进行按位异或, 结果存在目标寄存器中
         * 用法: xor rd, rs, rt
         */
        XOR("xor"),

        /**
         * 无符号比较, 如果第一个寄存器的值小于第二个寄存器的值, 目标寄存器为 1, 否则为 0
         * 用法: sltu rd, rs, rt
         */
        SLTU("sltu"),

        /**
         * 有符号比较
         * 用法: slt rd, rs, rt
         */
        SLT("slt"),

        /**
         * 有符号整数乘法，并将乘积累加到目标寄存器中的值。
         * 这里的作用是取rs * rt的高32位并覆盖至HI，结果存入dst
         * 语法：SMMUL rd, rs, rt
         */
        MULT("mult"),

        /**
         * 有符号整数乘法，并将乘积与第三个寄存器中的值相加。
         * 在这里我们指定ra为HI
         * 这里的作用是取rs * rt的高32位并累加至HI，结果存入dst
         * 语法：SMMADD rd, rs, rt, ra
         */
        MADD("madd"),
        /**
         * 按位与
         * 用于trunc中
         */
        AND("and");

        String name;
        BinaryType(String name){
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private BinaryType type; // 指令的类型
    private MipsOperand dst = null;
    private MipsOperand src1 = null; // 第一个操作数
    private MipsOperand src2 = null; // 第二个操作数
    public void setDst(MipsOperand dst){
        addDefReg(this.dst, dst);
        this.dst = dst;
    }
    public void setSrc1(MipsOperand src1){
        addUseReg(this.src1, src1);
        this.src1 = src1;
    }
    public void setSrc2(MipsOperand src2){
        addUseReg(this.src2, src2);
        this.src2 = src2;
    }
    public MipsOperand getDst(){
        return dst;
    }
    public MipsOperand getSrc1(){
        return src1;
    }
    public MipsOperand getSrc2(){
        return src2;
    }
    public BinaryType getType() {
        return type;
    }

    @Override
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg) {
        if(dst.equals(oldReg)){
            setDst(newReg);
        }
        if(src1.equals(oldReg)){
            setSrc1(newReg);
        }
        if(src2.equals(oldReg)){
            setSrc2(newReg);
        }
    }

    public MipsBinary(BinaryType type, MipsOperand dst, MipsOperand src1, MipsOperand src2){
        this.type = type;
        setDst(dst);
        setSrc1(src1);
        setSrc2(src2);
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        String typeStr;
        if(src2 instanceof MipsImme){ // 立即数类型
            switch (type){
                case ADDU -> typeStr = "addiu";
                case SUBU -> typeStr = "subiu";
                case SLTU -> typeStr = "sltiu";
//                default -> typeStr = type + "i";
                default -> typeStr = type.toString();
            }
            stringBuilder.append(typeStr + " " + getDst()+", " + getSrc1() + ", " + getSrc2() + "\n");
        } else {
            switch (type){
                case DIV -> {
                    //  (HI, LO) ← rs / rt
                    //   rd ← LO
                    stringBuilder.append(type + " " + getSrc1() +" "+ getSrc2() + "\n");
                    stringBuilder.append("\t" + "mflo " + dst + "\n");
                }
                case MADD, MULT -> {
                    // {HI, LO}<-{HI, LO}+ rs x rt
                    // dst ← HI
                    stringBuilder.append(type + " " + getSrc1() + " " + getSrc2() + "\n");
                    stringBuilder.append("\t" + "mfhi " + dst + "\n");
                }
                default -> stringBuilder.append(type + " " + getDst() + ", " + getSrc1() + ", " + getSrc2() + "\n");
            }
        }
        return stringBuilder.toString();
    }
}
