package ir.instructions.terminator;

import ir.BasicBlock;
import ir.Value;
import ir.instructions.Instruction;
import ir.types.VoidType;

import java.util.ArrayList;

// ret <type> <value> ,ret void
public class Ret extends Instruction {
    // 有返回值
    public Ret(BasicBlock parent, Value value){
        super(new VoidType(), "", parent, new ArrayList<>(){{
            add(value);
        }});
    }
    // 没有返回值
    public Ret(BasicBlock parent){
        super(new VoidType(), "", parent, new ArrayList<>());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("ret ");
        if(getOperands().isEmpty()){
            stringBuilder.append("void");
        }else {
            Value value = getOperands().get(0);
            stringBuilder.append(value.getValueType()).append(" ").append(value.getName());
        }
        return stringBuilder.toString();
    }
}
