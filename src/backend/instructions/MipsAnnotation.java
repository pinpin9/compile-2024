package backend.instructions;

/**
 * 注释
 */
public class MipsAnnotation extends MipsInstruction{
    private String content;
    public MipsAnnotation(String content){
        this.content = content;
    }

    @Override
    public String toString() {
        return "# " + content + "\n";
    }
}