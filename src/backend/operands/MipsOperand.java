package backend.operands;

/**
 * @author zlp
 * @Discription Mips操作数
 * @date 2024/11/19
 */
public abstract class MipsOperand {
    /**
     * 预着色, 对于非物理寄存器，均为 false
     * 对于物理寄存器, 返回 !isAllocated
     * 非物理寄存器的两种状态: isAllocated 和 isPreColored
     * @return
     */
    public boolean isPreColored(){
        return false;
    }
    // 是否需要着色，物理寄存器未被分配的时候是未被着色的
    public boolean needColoring(){
        return false;
    }
    // 是否被分配
    public boolean isAllocated(){
        return false;
    }
}
