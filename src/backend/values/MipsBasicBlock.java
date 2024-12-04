package backend.values;

import backend.instructions.MipsInstruction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MipsBasicBlock {
    private String name;
    private int loopDepth;
    private static int nameCnt = 0;
    public MipsBasicBlock(String name, int loopDepth){
        this.name = name.substring(1)+"_"+nameCnt++; // 去掉开头的@
        this.loopDepth = loopDepth;
    }

    public String getName() {
        return name;
    }

    public int getLoopDepth() {
        return loopDepth;
    }

    // 因为需要实现头尾插入删除，所以用LinkedList
    private LinkedList<MipsInstruction> instructions = new LinkedList<>(); // 指令集合

    //=============指令操作==============
    public void setInstructions(LinkedList<MipsInstruction> instructions) {
        this.instructions = instructions;
    }
    public LinkedList<MipsInstruction> getInstructions(){
        return instructions;
    }
    public void addTailInstruction(MipsInstruction mipsInstruction){
        instructions.add(mipsInstruction);
    }
    public void addHeadInstruction(MipsInstruction mipsInstruction){
        instructions.addFirst(mipsInstruction);
    }
    public void removeLastInstruction(){ // 去掉尾部的跳转指令，方便与下一个基本块合并
        instructions.removeLast();
    }
    public MipsInstruction getLastInstruction(){
        return instructions.getLast();
    }
    public void insertAfter(MipsInstruction dst, MipsInstruction src){
        for(MipsInstruction instruction : instructions){
            if(instruction.equals(dst)){
                int index = instructions.indexOf(dst);
                instructions.add(index+1, src);
                return;
            }
        }
    }
    public void insertBefore(MipsInstruction dst, MipsInstruction src){
        for(MipsInstruction instruction : instructions){
            if(instruction.equals(dst)){
                int index = instructions.indexOf(dst);
                instructions.add(index, src);
                return;
            }
        }
    }

    //============前驱和后继块============
    private MipsBasicBlock trueBlock = null;
    private MipsBasicBlock falseBlock = null;
    private List<MipsBasicBlock> preBlocks = new ArrayList<>();
    public void addPreBlock(MipsBasicBlock mipsBasicBlock){
        preBlocks.add(mipsBasicBlock);
    }
    public void removePreBlock(MipsBasicBlock mipsBasicBlock){
        preBlocks.remove(mipsBasicBlock);
    }
    public List<MipsBasicBlock> getPreBlocks(){
        return preBlocks;
    }
    public void setTrueBlock(MipsBasicBlock trueBlock){
        this.trueBlock = trueBlock;
    }
    public void setFalseBlock(MipsBasicBlock falseBlock) {
        this.falseBlock = falseBlock;
    }
    public MipsBasicBlock getTrueBlock() {
        return trueBlock;
    }
    public MipsBasicBlock getFalseBlock() {
        return falseBlock;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name).append(":\n");
        for(MipsInstruction instruction:instructions){
//            System.out.println("def:"+instruction.getDefRegs());
            stringBuilder.append("\t").append(instruction);
        }
        return stringBuilder.toString();
    }
}
