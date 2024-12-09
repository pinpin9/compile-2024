package backend.instructions;

import backend.values.MipsFunction;

/**
 * @author zlp
 * @Discription 函数调用
 * jal f
 * @date 2024/11/21
 */
public class MipsCall extends MipsInstruction{
    private MipsFunction function;
    public MipsCall(MipsFunction function){
        this.function = function;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("jal ").append(function.getName()).append("\n");
        return stringBuilder.toString();
    }
}
