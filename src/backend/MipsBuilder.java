package backend;

import backend.instructions.*;
import backend.operands.*;
import backend.opt.MulOptimizer;
import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;
import backend.values.MipsGlobalVariable;
import backend.values.MipsModule;
import ir.types.constants.ConstChar;
import ir.types.constants.ConstInt;
import ir.types.constants.Constant;
import ir.values.*;
import ir.values.Module;
import settings.Settings;
import tools.IO;
import tools.MipsMath;
import tools.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class MipsBuilder {
    private static MipsBuilder mipsBuilder = new MipsBuilder();
    public static MipsBuilder getInstance(){
        return mipsBuilder;
    }

    private MipsModule mipsModule = MipsModule.getModule();

    public void buildMips(Module irmodule){
        irmodule.buildMips();
    }

    public void buildGlobalVariable(String name, String content){ // 字符串
        MipsGlobalVariable mipsGlobalVariable = new MipsGlobalVariable(name, content);
        mipsModule.addGlobalVariable(mipsGlobalVariable);
    }

    public void buildGlobalVariable(String name, int size){ // 未初始化
        MipsGlobalVariable mipsGlobalVariable = new MipsGlobalVariable(name, size);
        mipsModule.addGlobalVariable(mipsGlobalVariable);
    }

    public void buildGlobalVariable(String name, MipsGlobalVariable.ValueType valueType, List<Integer> initValue){ // 初始化
        MipsGlobalVariable mipsGlobalVariable = new MipsGlobalVariable(name, valueType, initValue);
        mipsModule.addGlobalVariable(mipsGlobalVariable);
    }

    /**
     * 构造 mipsFunction
     * 在 mipsModule 中添加该 Function ，并添加从 irFunction 到 mipsFunction 的映射
     * @param irFunction 中间代码生成的函数模块
     */
    public void buildFunction(Function irFunction){
        MipsFunction mipsFunction = new MipsFunction(irFunction.getName(), irFunction.isLibFunc());
        mipsModule.addMipsFunction(mipsFunction);
        Mc.addFunctionMap(irFunction, mipsFunction);
    }


    /**
     * 将 LLVM IR 中的操作数构建成为 Mips 中的操作数
     * @param value ir中的操作数
     * @param isImme 是否需要构建立即数
     * @param irFunction 该操作数对应的函数块
     * @param irBlock 该操作数对应的基本块
     * @return 生成的结果
     */
    public MipsOperand buildOperand(Value value, boolean isImme, Function irFunction, BasicBlock irBlock){
        MipsOperand op = Mc.getMappedValue(value); // 试图获取该value对应的mips操作数
        if (op != null){
            // 需要寄存器，但是曾经解析出来的是一个立即数
            if (op instanceof MipsImme && !isImme){
                if(((MipsImme)op).getValue() == 0){ // 常数0
                    return MipsPhyReg.ZERO;
                } else { // 需要构建一个虚拟寄存器，并且move
                    MipsOperand virOp = buildVirReg(irFunction);
                    buildMove(virOp, op, irBlock);
                    return virOp;
                }
            }
            return op;
        } else { // 没有进行过解析
            if(value.isArg()){ // 是函数参数
                return buildArgOperand(value, irFunction);
            } else if (value instanceof GlobalVariable) { // 全局变量
                return buildGlobalOperand((GlobalVariable) value, irFunction, irBlock);
            } else if (value instanceof Constant) { // 常量
                int immeValue;
                if(value instanceof ConstInt){
                    immeValue = ((ConstInt)value).getValue();
                } else {
                    immeValue = ((ConstChar)value).getValue();
                }
                return buildImmeOperand(immeValue, isImme, irFunction, irBlock);
            } else { // 指令
                return buildVirReg(value, irFunction);
            }
        }
    }

    //============虚拟寄存器=============
    /**
     * 在对应的 mipsFunction中增加一个虚拟寄存器，
     * 并且构建从 LLVM 操作数到 Mips 操作数的配对信息
     */
    public MipsVirReg buildVirReg(Value irValue, Function irFunction){
        MipsVirReg mipsVirReg = buildVirReg(irFunction);
        Mc.addOperandMap(irValue, mipsVirReg);
        return mipsVirReg;
    }

    /**
     * 在对应的 mipsFunction中增加一个虚拟寄存器
     * @return 生成的虚拟寄存器
     */
    public MipsVirReg buildVirReg(Function irFunction){
        MipsVirReg mipsVirReg = new MipsVirReg();
        MipsFunction mipsFunction = Mc.getMappedFunction(irFunction);
        mipsFunction.addVirReg(mipsVirReg);
        return mipsVirReg;
    }

    public MipsOperand buildArgOperand(Value irValue, Function irFunction){
        MipsFunction mipsFunction = Mc.getMappedFunction(irFunction);
        MipsBasicBlock firstBlock = Mc.getMappedBlock(irFunction.getFirstBlock()); // alloca指令对应的基本块
        int argNum = irValue.getArgNum(); // 表示是第几个参数
        MipsVirReg virReg = buildVirReg(irValue, irFunction);
        if(argNum < 4){
            // 将参数寄存器上的值移动到虚拟寄存器上
            MipsMove move = new MipsMove(virReg, new MipsPhyReg("a"+argNum));
            firstBlock.addHeadInstruction(move);
        } else {
            // 从栈上加载
            int stackPos = argNum - 4;
            MipsImme mipsOffset = new MipsImme(stackPos * 4);
            mipsFunction.addArgsOffset(mipsOffset);
            MipsLoad load = new MipsLoad(virReg, MipsPhyReg.SP, mipsOffset);
            firstBlock.addHeadInstruction(load);
        }
        return virReg;
    }

    public MipsOperand buildGlobalOperand(GlobalVariable value, Function irFunction, BasicBlock irBlock){
        MipsVirReg virReg = buildVirReg(irFunction);
        buildMove(virReg, new MipsLabel(value.getName().substring(1)), irBlock);
        return virReg;
    }

    public MipsOperand buildImmeOperand(int value, boolean isImme, Function irFunction, BasicBlock irBlock){
        MipsImme mipsImme = new MipsImme(value);
        if(isImme){
            return mipsImme;
        }else {
            if(value == 0){ // 常量0
                return MipsPhyReg.ZERO;
            } else {
                MipsVirReg virReg = buildVirReg(irFunction);
                MipsMove move = buildMove(virReg, mipsImme, irBlock);
                return virReg;
            }
        }
    }



    //===============指令==================
    public MipsBranch buildBranch(MipsBasicBlock target, BasicBlock parent){
        MipsBranch branch = new MipsBranch(target);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(branch);
        return branch;
    }

    public MipsBranch buildBranch(MipsBasicBlock target, MipsBasicBlock source){
        MipsBranch branch = new MipsBranch(target);
        source.addTailInstruction(branch);
        return branch;
    }

    public MipsBranch buildBranch(MipsCondType condType, MipsOperand src1, MipsOperand src2, MipsBasicBlock target, BasicBlock irBlock){
        if(src2 instanceof MipsImme && ((MipsImme) src2).getValue() == 0){
            src2 = null;
        }
        MipsBranch branch = new MipsBranch(target, condType, src1, src2);
        MipsBasicBlock parent = Mc.getMappedBlock(irBlock);
        parent.addTailInstruction(branch);
        return branch;
    }

    public MipsMove buildMove(MipsOperand dst, MipsOperand src, BasicBlock parent){
        MipsMove move = new MipsMove(dst, src);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(move);
        return move;
    }

    public MipsBinary buildBinary(MipsBinary.BinaryType type, MipsOperand dst, MipsOperand op1, MipsOperand op2, BasicBlock parent){
        MipsBinary binary = new MipsBinary(type, dst, op1, op2);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(binary);
        return binary;
    }

    public MipsStore buildStore(MipsOperand src, MipsOperand offset, MipsOperand addr, BasicBlock parent){
        MipsStore store = new MipsStore(src, addr, offset);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(store);
        return store;
    }

    public MipsLoad buildLoad(MipsOperand dst, MipsOperand base, MipsOperand offset, BasicBlock parent){
        MipsLoad load = new MipsLoad(dst, base, offset);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(load);
        return load;
    }

    public MipsRet buildRet(Function irFunction, BasicBlock block){
        MipsRet ret = new MipsRet(Mc.getMappedFunction(irFunction));
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(block);
        mipsBasicBlock.addTailInstruction(ret);
        return ret;
    }

    public MipsCompare buildCompare(MipsCondType cond, MipsOperand dst, MipsOperand src1, MipsOperand src2, BasicBlock parent){
        MipsCompare compare = new MipsCompare(cond, dst, src1, src2);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(compare);
        return compare;
    }

    public MipsShift buildShift(MipsShift.ShiftType shiftType, MipsOperand dst, MipsOperand src1, Integer second, BasicBlock parent) {
        MipsShift shift = new MipsShift(shiftType, dst, src1, second);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(shift);
        return shift;
    }

    public MipsMoveHI buildMoveHI(MipsMoveHI.MoveHIType type, MipsOperand op, BasicBlock parent){
        MipsMoveHI moveHI = new MipsMoveHI(type, op);
        MipsBasicBlock mipsBasicBlock = Mc.getMappedBlock(parent);
        mipsBasicBlock.addTailInstruction(moveHI);
        return moveHI;
    }


    //=============基于优化的乘法===============
    public void buildMul(MipsOperand dst, Value op1, Value op2, Function irFunction, BasicBlock irBlock){
        // 计算的式子
        Boolean isOp1ConstInt = op1 instanceof ConstInt;
        Boolean isOp2ConstInt = op2 instanceof ConstInt;
        MipsOperand src1, src2;
        // 有常数，可以进行优化
        if(isOp1ConstInt || isOp2ConstInt){
            int imme;
            if(isOp1ConstInt){
                src1 = buildOperand(op2, false, irFunction, irBlock);
                imme = ((ConstInt)op1).getValue();
            } else {
                src1 = buildOperand(op1, false, irFunction, irBlock);
                imme = ((ConstInt)op2).getValue();
            }
            // 根据常数imme获取优化操作序列
            ArrayList<Pair<Boolean, Integer>> mulOptItems = MipsMath.getMulOptItems(imme);
            if(mulOptItems == null){ // 无法优化
                if (isOp1ConstInt){
                    src2 = buildOperand(op1, false, irFunction, irBlock);
                } else {
                    src2 = buildOperand(op2, false, irFunction, irBlock);
                }
            } else { // 可以优化
                if (mulOptItems.size() == 1){
                    doOptMulStep1(mulOptItems.get(0), dst, src1, irBlock);
                } else {
                    doOptMulStep1(mulOptItems.get(0), MipsPhyReg.AT, src1, irBlock);
                    for(int i = 1; i < mulOptItems.size()-1; i++){
                        doOptMulStep(mulOptItems.get(i), MipsPhyReg.AT, MipsPhyReg.AT, src1, irBlock);
                    }
                    doOptMulStep(mulOptItems.get(mulOptItems.size()-1), dst, MipsPhyReg.AT, src1, irBlock);

                }
                return;
            }
        } else {
            src1 = buildOperand(op1, false, irFunction, irBlock);
            src2 = buildOperand(op2, false, irFunction, irBlock);
        }
        buildBinary(MipsBinary.BinaryType.MUL, dst, src1, src2, irBlock);
    }

    private void doOptMulStep1(Pair<Boolean, Integer> optItem, MipsOperand dst, MipsOperand src1, BasicBlock irBlock){
        // dst = src1 << mulOptItems.get(0).getSecond()
        buildShift(MipsShift.ShiftType.SLL, dst, src1, optItem.getSecond(), irBlock);
        // dst = -dst
        if (!optItem.getFirst()) {
            buildBinary(MipsBinary.BinaryType.SUBU, dst, MipsPhyReg.ZERO, dst, irBlock);
        }
    }

    private void doOptMulStep(Pair<Boolean, Integer> optItem, MipsOperand dst, MipsOperand tmpDst, MipsOperand src1, BasicBlock irBlock){
        if (optItem.getSecond() == 0) {
            // at
            if(optItem.getFirst()){
                buildBinary(MipsBinary.BinaryType.ADDU, dst, tmpDst, src1, irBlock);
            } else{
                buildBinary(MipsBinary.BinaryType.SUBU, dst, tmpDst, src1, irBlock);
            }
        }
        // 需要位移
        else {
            // 生成周转用的虚拟寄存器 tmp = src1 << mulOptItems.get(0).getSecond()
            MipsOperand tmp = buildVirReg(Mc.curIrFunction);
            buildShift(MipsShift.ShiftType.SLL, tmp, src1, optItem.getSecond(), irBlock);
            if(optItem.getFirst()){
                buildBinary(MipsBinary.BinaryType.ADDU, dst, tmpDst, tmp, irBlock);
            } else{
                buildBinary(MipsBinary.BinaryType.SUBU, dst, tmpDst, tmp, irBlock);
            }
        }
    }



    // 输出
    private static IO mipsOutput = new IO(Settings.mipsFile);
    // 输出目标代码
    public void print(){
        mipsOutput.output(mipsModule.toString());
    }
}
