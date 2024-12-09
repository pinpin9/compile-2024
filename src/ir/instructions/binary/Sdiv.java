package ir.instructions.binary;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsBinary;
import backend.instructions.MipsMoveHI;
import backend.instructions.MipsShift;
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
import tools.MipsMath;

public class Sdiv extends BinaryInstruction {
    public Sdiv(ValueType valueType, String name, BasicBlock parent, Value op1, Value op2) {
        super("sdiv", valueType, name, parent, op1, op2);
    }

    MipsBuilder builder = MipsBuilder.getInstance();
    @Override
    public void buildMips() {

        MipsOperand src1 = builder.buildOperand(getOp1(), false, Mc.curIrFunction, getParent());
        MipsBasicBlock mipsBlock = Mc.getMappedBlock(getParent());

        MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
        Value op2 = getOp2();

        if (op2 instanceof ConstInt) { // 除数是常数，可以进行常数优化
            // 获得除数常量
            int imm = ((ConstInt) op2).getValue();
            if (imm == 1) { // 除数为 1 ，无需生成中间代码，,将 ir 映射成被除数，直接记录即可
                Mc.addOperandMap(this, src1);
            } else { // 除数不为1
                MipsOperand result = Mc.getMappedDiv(mipsBlock, src1, new MipsImme(imm));
                // 如果先前已有计算结果，无需生成中间代码，直接记录映射即可
                if (result != null) {
                    Mc.addOperandMap(this, result);
                } else { // 先前没有计算结果，需要手动进行计算
                    doDivConstOpt(dst, src1, imm);
                }
            }
        } else { // 除数不是常数，无法进行常数优化
            MipsOperand src2 = builder.buildOperand(op2, false, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.DIV, dst, src1, src2, getParent());
        }
    }

    /**
     * 常数优化乘法： dst = src / imm
     */
    private void doDivConstOpt(MipsOperand dst, MipsOperand src, int imm) {
        // 这里之所以取 abs，是在之后如果是负数，那么会有一个取相反数的操作
        int abs = Math.abs(imm);
        // 除数为-1，取相反数dst = 0 - src, 生成结束
        if (imm == -1) {
            builder.buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsPhyReg.ZERO, src,getParent());
            return;
        } else if (imm == 1) { // 除数为1，直接进行move
            builder.buildMove(dst, src, getParent());
        } else if (MipsMath.isPow2(abs)) { // 如果是 2 的幂次
            // 末尾0的个数
            int l = MipsMath.countTailZeroNumber(abs);
            // 产生新的被除数
            MipsOperand newSrc = buildNegativeSrcCeil(src, abs);
            // 将新的被除数右移
            builder.buildShift(MipsShift.ShiftType.SRA, dst, newSrc, l, getParent());
        } else {
            // 转换公式dst = src / abs
            // dst = (src * n) >> shift
            long nc = ((long) 1 << 31) - (((long) 1 << 31) % abs) - 1;
            long p = 32;
            while (((long) 1 << p) <= nc * (abs - ((long) 1 << p) % abs)) {
                p++;
            }
            long m = ((((long) 1 << p) + (long) abs - ((long) 1 << p) % abs) / (long) abs);
            int n = (int) ((m << 32) >>> 32);
            int shift = (int) (p - 32);

            // tmp0 = n
            MipsOperand tmp0 = builder.buildVirReg(Mc.curIrFunction);
            builder.buildMove(tmp0, new MipsImme(n), getParent());

            MipsOperand tmp1 = builder.buildVirReg(Mc.curIrFunction);
            // tmp1 = src + (src * n)[63:32]
            if (m >= 0x80000000L) {
                // HI = src
                builder.buildMoveHI(MipsMoveHI.MoveHIType.MTHI, src, getParent());
                // tmp1 += src * tmp0 + HI （有符号乘法）
                builder.buildBinary(MipsBinary.BinaryType.MADD, tmp1, src, tmp0, getParent());
            } else { // tmp1 = (src * n)[63:32] 有符号的
                builder.buildBinary(MipsBinary.BinaryType.MULT, tmp1, src, tmp0, getParent());
            }

            MipsOperand tmp2 = builder.buildVirReg(Mc.curIrFunction);
            // tmp2 = tmp1 >> shift
            builder.buildShift(MipsShift.ShiftType.SRA, tmp2, tmp1, shift, getParent());
            // AT = src >> 31
            builder.buildShift(MipsShift.ShiftType.SRL, MipsPhyReg.AT, src, 31, getParent());
            // dst = tmp2 + AT
            builder.buildBinary(MipsBinary.BinaryType.ADDU, dst, MipsPhyReg.AT, tmp2, getParent());
        }

        // 先前都是使用的除数绝对值abs
        // 如果除数为负值，需要变为相反数
        if (imm < 0) {
            builder.buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsPhyReg.ZERO, dst, getParent());
        }
        // 记录，以便后续使用
        Mc.addDivMap(Mc.getMappedBlock(getParent()), src, new MipsImme(imm), dst);
    }

    /**
     * 针对负被除数的除法向上取整，产生新的被除数
     * 若只采用移位操作，除法向下取整 -3 / 4 = -1，与除法的含义不符
     * 新的被除数：newDividend = oldDividend + divisor - 1
     * @param oldSrc        旧的被除数
     * @param absImm         除数的绝对值，为2的幂次
     * @return 新的被除数
     */
    private MipsOperand buildNegativeSrcCeil(MipsOperand oldSrc, int absImm) {
        MipsOperand newSrc = builder.buildVirReg(Mc.curIrFunction);
        int l = MipsMath.countTailZeroNumber(absImm);

        // tmp1 = (oldDividend >> 31)
        // 这是为了保留负数的最高位1，正数在下面的过程中不受影响
        MipsOperand tmp1 = builder.buildVirReg(Mc.curIrFunction);
        builder.buildShift(MipsShift.ShiftType.SRA, tmp1, oldSrc, 31, getParent());
        // tmp1 = tmp1 << 32-l
        // 如果被除数是负数，那么[l-1 : 0] 位全为1，这就是abs - 1
        builder.buildShift(MipsShift.ShiftType.SRL, tmp1, tmp1, 32 - l, getParent());
        // newSrc = oldSrc + divisor - 1
        builder.buildBinary(MipsBinary.BinaryType.ADDU, newSrc, oldSrc, tmp1, getParent());
        return newSrc;
    }
}
