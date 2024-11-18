package ir.types.constants;

import ir.types.ArrayType;

import java.util.ArrayList;

public class ConstArray extends Constant{
    private ArrayList<Constant> values = new ArrayList<>();
    // 带初始化的赋值
    public ConstArray(ArrayList<Constant> values){
        super(new ArrayType(values.get(0).getValueType(), values.size()), new ArrayList<>(){{
            addAll(values);
        }});
        this.values.addAll(values);
    }

    public ArrayList<Constant> getValues() {
        return values;
    }

    public Constant getElementByIndex(int i){
        return values.get(i);
    }

    private int initLen = 0; // 用来保存初始化值的个数
    public void setInitLen(int len){
        initLen = len;
    }

    public int getInitLen() {
        return initLen;
    }

    // @a = dso_local global [10 x i32] [i32 1, i32 2, i32 3, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0, i32 0]
    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        for(int i = 0; i < values.size(); i++){
            stringBuilder.append(values.get(i).getValueType()).append(" ").append(values.get(i));
            if(i<values.size()-1){
                stringBuilder.append(", ");
            }else{
                stringBuilder.append("]");
            }
        }
        return stringBuilder.toString();
    }
}
