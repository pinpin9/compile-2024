package backend;

public class MipsBasicBlock {
    private String name;
    private int loopDepth;

    public MipsBasicBlock(String name, int loopDepth){
        this.name = name;
        this.loopDepth = loopDepth;
    }
}
