package ir.instructions;

import ir.values.BasicBlock;
import ir.values.Value;
import ir.types.IntType;

import java.util.ArrayList;

/**
 * @author zlp
 * @Discription char和int类型的值相加的时候，会先将char转为int，保存的时候将int转为char
 * @date 2024/11/02
 */
// %5 = trunc i32 %4 to i8
// <result> = trunc <ty> <value> to <ty2>
public class Trunc extends Instruction{
    public Trunc(String name, BasicBlock parent, Value value){
        super(new IntType(8),name, parent, new ArrayList<>(){{
            add(value);
        }});
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getName()).append(" = trunc i32 ");
        stringBuilder.append(getOperands().get(0).getName()).append(" to i8");
        return stringBuilder.toString();
    }
}
