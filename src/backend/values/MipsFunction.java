package backend.values;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MipsFunction {
    private String name;
    private boolean isLibFuc;

    public String getName(){
        return name;
    }
    public boolean isLibFuc() {
        return isLibFuc;
    }
    private ArrayList<MipsBasicBlock> mipsBasicBlocks = new ArrayList<>();

    public MipsFunction(String name, boolean isLibFuc){
        this.name = name.substring(1); // 去掉开头的@符号
        this.isLibFuc = isLibFuc;
    }

    private HashSet<MipsBasicBlock> serializedBlock = new HashSet<>(); // 已经遍历过的模块
    // 序列化，构建跳转关系
    public void blockSerialize(MipsBasicBlock curBlock){
        serializedBlock.add(curBlock);
        mipsBasicBlocks.add(curBlock); // 构建好的序列列表
        if(curBlock.getTrueBlock() != null && curBlock.getFalseBlock() == null){ // 只有一个后继基本块
            MipsBasicBlock trueBlock = curBlock.getTrueBlock();
            if(!serializedBlock.contains(trueBlock)){
                curBlock.removeLastInstruction(); // 合并两个基本块，删除上一基本块中的跳转指令
                blockSerialize(trueBlock);
            }
        } else if (curBlock.getTrueBlock() != null && curBlock.getFalseBlock() != null) { // 有两个后继基本块
            MipsBasicBlock trueBlock = curBlock.getTrueBlock();
            MipsBasicBlock falseBlock = curBlock.getFalseBlock();

        }
    }


    @Override
    public String toString() {
        if(isLibFuc){
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        return stringBuilder.toString();
    }
}
