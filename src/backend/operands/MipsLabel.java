package backend.operands;

import java.util.Objects;

/**
 * @author zlp
 * @Discription 标签类型
 * @date 2024/11/19
 */
public class MipsLabel extends MipsOperand{
    private String name;
    public MipsLabel(String name){
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){ // 如果指向相同的指针
            return true;
        }
        if(obj == null || getClass() != obj.getClass()){ // 如果为空或者类型不相同
            return false;
        }
        return Objects.equals(name, ((MipsLabel) obj).name);
    }

    @Override
    public String toString() {
        return name;
    }
}
