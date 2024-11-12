package ir;

import ir.instructions.Instruction;
import ir.types.LabelType;

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

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName().substring(1)).append(":\n");
        for(Instruction instruction:instructions){
            stringBuilder.append("\t").append(instruction).append("\n");
        }
        return stringBuilder.toString();
    }
}
