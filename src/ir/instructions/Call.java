package ir.instructions;

import backend.Mc;
import backend.MipsBuilder;
import backend.instructions.*;
import backend.operands.MipsImme;
import backend.operands.MipsOperand;
import backend.operands.MipsPhyReg;
import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;
import ir.types.ValueType;
import ir.types.VoidType;
import ir.values.BasicBlock;
import ir.values.Function;
import ir.values.Value;

import java.util.ArrayList;
import java.util.List;

// <result> = call [ret attrs] <ty> <name>(<...args>)
public class Call extends Instruction{
    private Function function;
    private List<Value> args;
    /**
     * 方法描述：call指令的构造函数
     * call的ValueType和function函数的rtnType一致
     * @param basicBlock
     * @param function
     * @param args
     */
    public Call(String name, BasicBlock basicBlock, Function function, List<Value> args){
        super(function.getRetType(), name, basicBlock, new ArrayList<>() {{
            add(function);
            addAll(args);
        }});
        this.function = function;
        this.args = args;
    }

    // %6 = call i32 @foo(i32 %4, i32 %5)
    // call void @putint(i32 %6)
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        Function function = (Function) getOperands().get(0);
        if(!getName().isEmpty()){
            stringBuilder.append(getName()).append(" = ");
        }
        stringBuilder.append("call ").append(getValueType()).append(" ").append(function.getName()).append("(");
        for(int i=1;i<getOperands().size();i++){
            Value value = getOperands().get(i);
            stringBuilder.append(value.getValueType()).append(" ").append(value.getName());
            if(i<getOperands().size()-1){
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public void buildMips() {
        MipsBuilder builder = MipsBuilder.getInstance();
        MipsBasicBlock mipsBlock = Mc.getMappedBlock(getParent());
        MipsFunction callFunc = Mc.getMappedFunction(function);
        MipsInstruction callIns; // 调用的指令
        // 链接函数
        if(function.isLibFunc()){ // 不需要jal
            callIns = new MipsMacro(callFunc.getName());
            callIns.addDefReg(MipsPhyReg.V0); // 系统调用会改变v0
        } else {
            callIns = new MipsCall(callFunc);
        }

        int argc = args.size(); //参数个数
        for(int i = 0; i < argc; i++){
            Value irArg = args.get(i);
            MipsOperand src;
            if(i<4){ // 前四个参数，存入寄存器内
                src = builder.buildOperand(irArg, true, Mc.curIrFunction, getParent());
                MipsMove move = builder.buildMove(new MipsPhyReg("a" + i), src, getParent());
                callIns.addUseReg(move.getDst());
            } else { // 存入寄存器中
                src = builder.buildOperand(irArg, false, Mc.curIrFunction, getParent());
                MipsImme offset = new MipsImme(-(argc - i)*4);
                builder.buildStore(src, offset, MipsPhyReg.SP, getParent());
            }
        }

        if(argc > 4){
            // 栈向下生长 SP = SP - 4 * num
            MipsOperand offset = builder.buildImmeOperand(4 * (argc - 4), true, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.SUBU, MipsPhyReg.SP, MipsPhyReg.SP, offset, getParent());
        }

        // jal语句
        mipsBlock.addTailInstruction(callIns);

        if(argc > 4){
            // 栈的恢复
            MipsOperand offset = builder.buildImmeOperand(4 * (argc - 4), true, Mc.curIrFunction, getParent());
            builder.buildBinary(MipsBinary.BinaryType.ADDU, MipsPhyReg.SP, MipsPhyReg.SP, offset, getParent());
        }

        for(int i = 0; i < 4; i++){
            callIns.addDefReg(new MipsPhyReg("a"+i));
        }

        if(!function.isLibFunc()){ // 自定义函数需要保存返回地址 ra
            callIns.addDefReg(MipsPhyReg.RA);
        }

        // 处理返回值
        ValueType rtnType = function.getRetType();
        // 无论有无返回值，都需保存$V0
        callIns.addDefReg(MipsPhyReg.V0);
        if(!(rtnType instanceof VoidType)){
            MipsOperand dst = builder.buildOperand(this, false, Mc.curIrFunction, getParent());
            builder.buildMove(dst, MipsPhyReg.V0, getParent());
        }
    }
}
