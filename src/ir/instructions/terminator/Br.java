package ir.instructions.terminator;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.MipsCondType;
import backend.operands.MipsOperand;
import backend.values.MipsBasicBlock;
import ir.instructions.binary.Icmp;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import ir.values.BasicBlock;
import ir.values.Value;
import ir.instructions.Instruction;
import ir.types.VoidType;

import java.util.ArrayList;

// br i1 <cond>, label <iftrue>, label <iffalse> 有条件跳转
// br label <dest> 无条件跳转
public class Br extends Instruction {
    private boolean isCondition; // 是否为条件跳转
    public Br(BasicBlock parent, BasicBlock dest){ // 无条件跳转
        super(new VoidType(), "", parent, new ArrayList<>(){{
            add(dest);
        }});
        isCondition = false;
        parent.addSucBlock(dest); // 添加后继关系
        dest.addPreBlock(parent); // 添加前驱关系
    }

    public Br(BasicBlock parent, Value cond, BasicBlock ifTrue, BasicBlock ifFalse){ // 有条件跳转
        super(new VoidType(), "", parent, new ArrayList<>(){{
            add(cond);
            add(ifTrue);
            add(ifFalse);
        }});
        isCondition = true;
        if(ifTrue != null){
            ifTrue.addPreBlock(parent);
            parent.addSucBlock(ifTrue);
        }
        if(ifFalse != null){
            ifFalse.addPreBlock(parent);
            parent.addSucBlock(ifFalse);
        }
    }

    public boolean isCondition() {
        return isCondition;
    }

    // 中间代码生成
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("br ");
        if(isCondition){
            stringBuilder.append("i1 ").append(getOperands().get(0).getName()).append(", ");
            stringBuilder.append("label ").append(getOperands().get(1).getName()).append(", ");
            stringBuilder.append("label ").append(getOperands().get(2).getName());
        }else{
            stringBuilder.append("label ").append(getOperands().get(0).getName());
        }
        return stringBuilder.toString();
    }

    @Override
    public void buildMips() {
        // 当前基本块
        MipsBasicBlock curBlock = Mc.getMappedBlock(getParent());
        MipsBuilder builder = MipsBuilder.getInstance();
        if(!isCondition){ // 无条件跳转
            MipsBasicBlock targetBlock = Mc.getMappedBlock((BasicBlock) getOperands().get(0));
            builder.buildBranch(targetBlock, getParent());
            curBlock.setTrueBlock(targetBlock);
        } else {
            // 有条件跳转
            MipsBasicBlock trueBlock  = Mc.getMappedBlock((BasicBlock) getOperands().get(1));
            MipsBasicBlock falseBlock = Mc.getMappedBlock((BasicBlock) getOperands().get(2));

            Icmp icmp = (Icmp) getOperands().get(0); // 比较条件是icmp语句
            MipsCondType condType = MipsCondType.getType(icmp.getCond());
            Value op1 = icmp.getOperands().get(0);
            Value op2 = icmp.getOperands().get(1);
            MipsOperand src1, src2;
            if(op1 instanceof Constant && !(op2 instanceof Constant)){
                condType = MipsCondType.getOppCondType(condType);
                src1 = builder.buildOperand(op2, false, Mc.curIrFunction, getParent());
                src2 = builder.buildOperand(op1, true, Mc.curIrFunction, getParent());
            } else {
                src1 = builder.buildOperand(op1, false, Mc.curIrFunction, getParent());
                src2 = builder.buildOperand(op2, true, Mc.curIrFunction, getParent());
            }
            builder.buildBranch(condType, src1, src2, trueBlock, getParent());
            curBlock.setTrueBlock(trueBlock);
            curBlock.setFalseBlock(falseBlock);
        }

    }
}
