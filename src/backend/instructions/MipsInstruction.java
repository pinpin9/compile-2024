package backend.instructions;

import backend.operands.MipsOperand;
import backend.operands.MipsPhyReg;
import backend.operands.MipsVirReg;

import java.util.ArrayList;


/**
 * @author zlp
 * @Discription Mips指令
 * @date 2024/11/19
 */
public class MipsInstruction {
    private ArrayList<MipsOperand> useRegs = new ArrayList<>(); // 当前指令使用的寄存器
    private ArrayList<MipsOperand> defRegs = new ArrayList<>(); // 当前指令定义的寄存器

    public void addUseReg(MipsOperand op){
        if(op instanceof MipsPhyReg || op instanceof MipsVirReg){
            useRegs.add(op);
        }
    }
    public void addDefReg(MipsOperand op){
        if(op instanceof MipsPhyReg || op instanceof MipsVirReg){
            defRegs.add(op);
        }
    }
    public void remDefReg(MipsOperand op){
        if(op != null && op instanceof MipsOperand){
            defRegs.remove(op);
        }
    }
    public void remUseReg(MipsOperand op){
        if(op != null && op instanceof MipsOperand){
            useRegs.remove(op);
        }
    }

    /**
     * 动态寄存器分配，可能需要替换寄存器
     * 更新指令的定义寄存器
     * @param oldOp 旧寄存器的记录
     * @param newOp 新的使用寄存器
     */
    public void addDefReg(MipsOperand oldOp, MipsOperand newOp){
        remDefReg(oldOp);
        addDefReg(newOp);
    }
    /**
     * 动态寄存器分配，可能需要替换寄存器
     * 更新指令的使用寄存器
     * @param oldOp 旧寄存器的记录
     * @param newOp 新的使用寄存器
     */
    public void addUseReg(MipsOperand oldOp, MipsOperand newOp){
        remUseReg(oldOp);
        addUseReg(newOp);
    }

    /**
     * 替换指定的寄存器
     * @param oldReg
     * @param newReg
     */
    public void replaceReg(MipsOperand oldReg, MipsOperand newReg){
    }


    public ArrayList<MipsOperand> getDefRegs() {
        return defRegs;
    }
    public ArrayList<MipsOperand> getUseRegs() {
        return useRegs;
    }
}
