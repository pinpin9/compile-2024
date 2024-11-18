package backend;

import java.util.ArrayList;
import java.util.List;

public class MipsFunction {
    private String name;
    private boolean isLibFuc;

    public String getName(){
        return name;
    }
    public boolean isLibFuc() {
        return isLibFuc;
    }
    private List<MipsBasicBlock> mipsBasicBlocks = new ArrayList<>();

    public MipsFunction(String name, boolean isLibFuc){
        this.name = name.substring(1); // 去掉开头的@符号
        this.isLibFuc = isLibFuc;
    }




    @Override
    public String toString() {
        if(isLibFuc){
            return "";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name);
        return stringBuilder.toString();
    }
}
