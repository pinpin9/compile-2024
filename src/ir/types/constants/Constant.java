package ir.types.constants;

import ir.User;
import ir.Value;
import ir.types.ValueType;

import java.util.List;

/**
 * @author zlp
 * @Discription 常量，
 * @date 2024/11/01
 */
public class Constant extends User {
    // 不带赋值
    public Constant(ValueType valueType) {
        super(valueType, "", null);
    }

    // 具有赋值
    public Constant(ValueType valueType, List<Value> operands) {
        super(valueType, "", null, operands);
    }

    @Override
    public String getName() {
        return toString(); // 只有type
    }
}
