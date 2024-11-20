package ir.types;


/**
 * @author zlp
 * @Discription 指针类型
 * @date 2024/10/30
 */
public class PointerType extends ValueType{
    // 指向类型
    private ValueType pointingType;

    public PointerType(ValueType pointingType){
        this.pointingType = pointingType;
    }

    public ValueType getPointingType(){
        return pointingType;
    }
    public int getSize(){
        return 4;
    }

    @Override
    public String toString() {
        return pointingType + "*";
    }
}
