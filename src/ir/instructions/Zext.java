package ir.instructions;

import ir.BasicBlock;
import ir.Value;
import ir.types.IntType;

import java.util.ArrayList;

/**
 * @author zlp
 * @Discription 将 ty的value的type扩充为ty2
 * @date 2024/11/02
 */
// zext i1 to i32
// zext i8 to i32
// <result> = zext <ty> <value> to <ty2>
public class Zext extends Instruction{
    public Zext(String name, BasicBlock basicBlock, Value value){
        super(new IntType(32), name, basicBlock, new ArrayList<>(){{
            add(value);
        }});
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = zext ");
        stringBuilder.append(getOperands().get(0).getValueType()).append(" ").append(getOperands().get(0).getName());
        stringBuilder.append(" to i32");
        return stringBuilder.toString();
    }
}
