package ir.instructions.terminator;

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
    }
    public Br(BasicBlock parent, Value cond, BasicBlock ifTrue, BasicBlock ifFalse){ // 有条件跳转
        super(new VoidType(), "", parent, new ArrayList<>(){{
            add(cond);
            add(ifTrue);
            add(ifFalse);
        }});
        isCondition = true;
    }

    public boolean isCondition() {
        return isCondition;
    }

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
}
