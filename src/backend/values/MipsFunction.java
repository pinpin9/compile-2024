package backend.values;

import backend.MipsBuilder;
import backend.instructions.MipsInstruction;
import backend.operands.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public class MipsFunction {
    private String name;
    private boolean isLibFuc;
    private ArrayList<MipsBasicBlock> mipsBasicBlocks = new ArrayList<>(); // 函数下的基本块
    private TreeSet<RegType> regsNeedSave = new TreeSet<>(); // 函数中用到的寄存器，除了a0-a4等，需要保存现场
    private HashSet<MipsImme> argsOffset = new HashSet<>(); // 相对于栈顶的偏移
    private HashSet<MipsVirReg> usedVirRegs = new HashSet<>(); // 当前函数模块使用的虚拟寄存器

    public String getName(){
        return name;
    }
    public boolean isLibFuc() {
        return isLibFuc;
    }
    public TreeSet<RegType> getRegsNeedSave() { // 返回需要保存现场的寄存器
        return regsNeedSave;
    }
    public HashSet<MipsVirReg> getUsedVirRegs() {
        return usedVirRegs;
    }

    public MipsFunction(String name, boolean isLibFuc){
        this.name = name.substring(1); // 去掉开头的@符号
        this.isLibFuc = isLibFuc;
    }

    public ArrayList<MipsBasicBlock> getMipsBasicBlocks() {
        return mipsBasicBlocks;
    }

    //=============栈空间大小===============
    private int stackTotalSize = 0;
    private int allocatedSize = 0; // 局部变量所占空间
    public int getStackTotalSize(){
        return stackTotalSize;
    }
    public void allocSpace(int size){
        allocatedSize += size;
    }
    public int getAllocatedSize() {
        return allocatedSize;
    }

    //=============栈上的偏移=============
    public void addArgsOffset(MipsImme argOffset){
        argsOffset.add(argOffset);
    }

    /**
     * 函数栈的空间从上到下依次为：
     * 1.调用者保存的寄存器
     * 2.局部变量
     * 3.其余参数的 alloca
     * 4.前四个参数 alloca
     */
    public void buildStack(){
        for(MipsBasicBlock block:mipsBasicBlocks){ // 遍历所有的基本块
            for(MipsInstruction instruction:block.getInstructions()){ // 遍历基本块中所有的指令
                // 保存写过的寄存器
                for(MipsOperand op : instruction.getDefRegs()){
                    if (op instanceof MipsPhyReg) {
                        RegType regType = ((MipsPhyReg) op).getRegType();
                        if (RegType.regsNeedSave.contains(regType)) {
                            regsNeedSave.add(regType);
                        }
                    }
                }
            }
        }
        int stackRegSize = 4 * regsNeedSave.size(); // 保留现场所占的空间
        stackTotalSize = allocatedSize + stackRegSize; // 局部变量所占空间 + 分配的空间大小
        for(MipsImme argOffset : argsOffset){
            // 更新位移
            int newOffset = argOffset.getValue() + stackTotalSize;
            argOffset.setValue(newOffset);
        }
    }


    //===========基本块序列化=============
    private HashSet<MipsBasicBlock> serializedBlock = new HashSet<>(); // 已经遍历过的模块
    MipsBuilder builder = MipsBuilder.getInstance();
    // 根据块的后继关系，调整跳转指令和合并块。
    public void blockSerialize(MipsBasicBlock curBlock){
        serializedBlock.add(curBlock);
        mipsBasicBlocks.add(curBlock); // 构建好的序列列表
        if(curBlock.getTrueBlock() == null && curBlock.getFalseBlock() == null){
            return;
        }
        if(curBlock.getTrueBlock() != null && curBlock.getFalseBlock() == null){ // 只有一个后继基本块
            MipsBasicBlock trueBlock = curBlock.getTrueBlock();
            if(!serializedBlock.contains(trueBlock)){
                curBlock.removeLastInstruction(); // 合并两个基本块，删除上一基本块中的跳转指令
                blockSerialize(trueBlock);
            }
        } else if (curBlock.getTrueBlock() != null && curBlock.getFalseBlock() != null) { // 有两个后继基本块
            MipsBasicBlock trueBlock = curBlock.getTrueBlock();
            MipsBasicBlock falseBlock = curBlock.getFalseBlock();
            if(serializedBlock.contains(falseBlock)){
                builder.buildBranch(falseBlock, curBlock);
            }
            if(!serializedBlock.contains(falseBlock)){
                blockSerialize(falseBlock);
            }
            if(!serializedBlock.contains(trueBlock)){
                blockSerialize(trueBlock);
            }
        }
    }

    public void addVirReg(MipsVirReg mipsVirReg){
        usedVirRegs.add(mipsVirReg);
    }


    @Override
    public String toString() {
        if(isLibFuc){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append(":\n");
        /**
         * 非主函数需要保存现场
         * 主函数不会被调用，所以不需要保存现场
         */
        if(!name.equals("main")){
            int regOffset = 0;
            for(RegType regType : regsNeedSave){
                regOffset -= 4;
                stringBuilder.append("\tsw ").append(regType).append(", ").append(regOffset).append("($sp)\n");
            }
        }
        /**
         * 栈顶指针移动
         * addiu $sp, $sp, -stackTotalSize
         */
        if(stackTotalSize != 0){
            stringBuilder.append("\taddiu $sp, $sp, ").append(-stackTotalSize).append("\n");
        }
        for(MipsBasicBlock block : mipsBasicBlocks){
            stringBuilder.append(block);
        }
        return stringBuilder.toString();
    }
}
