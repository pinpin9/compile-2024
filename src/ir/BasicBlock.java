package ir;

import ir.instructions.Instruction;
import ir.types.LabelType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


/**
 * @author zlp
 * @Discription 基本块：代码块，执行单元，属性为Label
 * 每个 BasicBlock 都有一个 label，label 使得该 BasicBlock 有一个符号表的入口点。
 * BasicBlock 以 terminator instruction（ret、br 等）结尾。
 * 每个 BasicBlock 由一系列 Instruction 组成，
 * @date 2024/11/01
 */
public class BasicBlock extends Value{
    private LinkedList<Instruction> instructions = new LinkedList<>();
    public BasicBlock(String name,Value parent){
        super(new LabelType(), name, parent);
    }
    public LinkedList<Instruction> getInstructions() {
        return instructions;
    }
    public Instruction getLastInstruction(){
        if(instructions.size()>0){
            return instructions.getLast();
        }
        return null;
    }
    public void addHeadInstruction(Instruction instruction){
        instructions.addFirst(instruction);
    }
    public void addTailInstruction(Instruction instruction){
        instructions.add(instruction);
    }
    public void delInstruction(Instruction instruction){
        instructions.remove(instruction);
    }
    public void delAllInstruction(){
        instructions.clear();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName().substring(1)).append(":\n");
        for(Instruction instruction:instructions){
            stringBuilder.append("\t").append(instruction).append("\n");
        }
        return stringBuilder.toString();
    }

    /*==============IR analyze============*/
    /**
     * 前驱和后继基本块
     * 因为不考虑顺序，所以采取set形式
     */
    private HashSet<BasicBlock> preBlocks = new HashSet<>(); // 前驱基本块
    private HashSet<BasicBlock> sucBlocks = new HashSet<>(); // 后继基本块
    public void addPreBlock(BasicBlock block){
        preBlocks.add(block);
    }
    public void addSucBlock(BasicBlock block){
        sucBlocks.add(block);
    }
    public HashSet<BasicBlock> getPreBlocks(){
        return preBlocks;
    }
    public HashSet<BasicBlock> getSucBlocks() {
        return sucBlocks;
    }

    /**
     * 支配关系的块
     */
    private List<BasicBlock> domers = new ArrayList<>(); // 支配该基本块的块
    private List<BasicBlock> domees = new ArrayList<>(); // 被该块支配的块
    private BasicBlock directDomer = null; // 直接操作该块的基本块
    private int domDepth = 0; // 在支配树中的深度
    private HashSet<BasicBlock> dominanceFrontier = new HashSet<>(); // 支配边际，即刚好不被该基本块支配的块
    public List<BasicBlock> getDomers(){
        return domers;
    }
    public List<BasicBlock> getDomees(){
        return domees;
    }
    public void setDirectDomer(BasicBlock directDomer){
        this.directDomer = directDomer;
    }
    public BasicBlock getDirectDomer(){
        return directDomer;
    }
    public boolean hasDom(BasicBlock other){ // 查看当前基本块是否支配其它块
        return domees.contains(other);
    }
    public void setDomDepth(int domDepth) {
        this.domDepth = domDepth;
    }
    public int getDomDepth() {
        return domDepth;
    }
    public HashSet<BasicBlock> getDominanceFrontier(){
        return dominanceFrontier;
    }


    // 循环优化？先不管这部分吧
    private int loopDepth = 0; // 循环深度，如果不在循环中，则为1

    public int getLoopDepth() {
        return loopDepth;
    }


    //==========目标代码生成==========
    public void buildMips(){
        for(Instruction instruction:instructions){
            instruction.buildMips();
        }
    }
}
