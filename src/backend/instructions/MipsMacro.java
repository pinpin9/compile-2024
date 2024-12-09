package backend.instructions;


public class MipsMacro extends MipsInstruction{
    private String content;

    public MipsMacro(String content){
        this.content = content;
    }
    @Override
    public String toString() {
        return content + "\n";
    }
}
