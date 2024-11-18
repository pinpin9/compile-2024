package backend;

import java.util.ArrayList;
import java.util.List;

public class MipsGlobalVariable {
    private String name;
    private List<Integer> initValue = new ArrayList<>();
    private ValueType valueType; // int | char

    private boolean hasInit; // 是否被初始化
    private int size; // 空间大小

    // 字符串
    private boolean isStr;
    private String content;

    public boolean isHasInit() {
        return hasInit;
    }

    public boolean isStr() {
        return isStr;
    }

    public enum ValueType{
        intType,
        charType
    }

    public MipsGlobalVariable(String name, ValueType valueType, List<Integer> initValue){ // 有初始化
        this.name = name.substring(1); // 去掉开头的@
        this.valueType = valueType;
        this.initValue = initValue;
        hasInit = true;
        isStr = false;
        if(valueType==ValueType.charType){
            size = initValue.size();
        }else{
            size = initValue.size()*4;
        }
    }

    public MipsGlobalVariable(String name, int size){ // 没有初始化
        this.name = name.substring(1);
        this.size = size;
        hasInit = false;
        isStr = false;
    }

    public MipsGlobalVariable(String name, String content){ // 字符串
        this.name = name.substring(2); // 去掉@.
        this.content = content;
        isStr = true;
        hasInit = false;
        size = content.length();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(name + ": ");
        if(isStr){
            stringBuilder.append(".asciiz \"").append(content).append("\"");
        } else {
            if(hasInit){ // 有初始化
                if(valueType==ValueType.intType){ // int
                    stringBuilder.append(".word ");
                } else{
                    stringBuilder.append(".byte ");
                }
                for(int i = 0; i < initValue.size(); i++){
                    stringBuilder.append(initValue.get(i));
                    if(i<initValue.size()-1){
                        stringBuilder.append(", ");
                    }
                }
            } else{
                stringBuilder.append(".space "+size);
            }
        }
        stringBuilder.append("\n");

        return stringBuilder.toString();
    }
}
