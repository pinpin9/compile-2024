package backend.opt;

import backend.instructions.MipsInstruction;
import backend.instructions.MipsLoad;
import backend.instructions.MipsMove;
import backend.instructions.MipsStore;
import backend.operands.*;
import backend.values.MipsBasicBlock;
import backend.values.MipsFunction;
import backend.values.MipsModule;
import tools.Pair;

import java.util.*;

/**
 * @author zlp
 * @Discription 图着色寄存器分配
 * @date 2024/11/28
 */
public class RegBuilder {
    private static RegBuilder regBuilder = new RegBuilder();
    public static RegBuilder getInstance() {
        return regBuilder;
    }

    private int K = RegType.regsCanAlloc.size(); // 可以分配的寄存器总数
    private HashMap<MipsBasicBlock, LiveVarInfo> liveVarInfoMap; // 基本块对应的活跃变量信息
    private HashMap<MipsOperand, HashSet<MipsOperand>> adjList = new HashMap<>(); // 一个节点对应所有相关节点信息
    private HashSet<Pair<MipsOperand, MipsOperand>> adjSet = new HashSet<>(); // 边的集合
    private HashSet<MipsOperand> coalescedNodes = new HashSet<>(); // 已经被合并的节点集合，若将v合并到u，则将v加到该集合，u加到其他集合
    private HashMap<MipsOperand, MipsOperand> alias = new HashMap<>(); // 当一条传送指令 (u,v) 被合并，且 v 已经被放入 coalescedNodes 中，alias(v) = u
    private HashMap<MipsOperand, HashSet<MipsMove>> moveList = new HashMap<>(); // 一个节点跟与这个节点相关move指令的匹配
    // 高度数节点表
    private HashSet<MipsOperand> spillList = new HashSet<>();
    private HashSet<MipsOperand> simplifyList = new HashSet<>();
    // 低度数节点表
    private HashSet<MipsOperand> freezeList = new HashSet<>();
    private HashSet<MipsOperand> spilledNodes = new HashSet<>(); // 本轮中溢出的节点
    private HashSet<MipsInstruction> coalescedMoves = new HashSet<>(); // 已经被合并的Move指令集合
    private Stack<MipsOperand> selectedStack = new Stack<>(); // 包含删除的点
    private HashSet<MipsMove> constrainsMoves = new HashSet<>(); // 源操作数和目标操作数冲突的传送指令集合
    private HashSet<MipsMove> frozenMoves = new HashSet<>(); // 不考虑合并的传送指令集合
    private HashSet<MipsMove> mergeAbleMoves = new HashSet<>(); // 可能合并的传送指令集合
    private HashSet<MipsMove> activeMoves = new HashSet<>(); // 还没有做好合并准备的指令集合

    private HashMap<MipsOperand, Integer> degree = new HashMap<>(); // 节点的度
    private MipsVirReg virReg = null; // 新的虚拟寄存器，当溢出的时候引入新的虚拟寄存器
    private final HashMap<MipsOperand, Integer> loopDepths = new HashMap<>();

    private void init(MipsFunction function){
        liveVarInfoMap = LiveVarInfo.liveAnalyze(function);

        adjList = new HashMap<>();
        adjSet = new HashSet<>();
        alias = new HashMap<>();
        moveList = new HashMap<>();
        simplifyList = new HashSet<>();
        freezeList = new HashSet<>();
        spillList = new HashSet<>();
        spilledNodes = new HashSet<>();
        coalescedNodes = new HashSet<>();
        selectedStack = new Stack<>();

        mergeAbleMoves = new HashSet<>();
        activeMoves = new HashSet<>();

        coalescedMoves = new HashSet<>();
        frozenMoves = new HashSet<>();
        constrainsMoves = new HashSet<>();

        degree = new HashMap<>();
        // 物理寄存器的度需要无限大
        for(int i = 0; i < 32; i++){
            MipsPhyReg phyReg = new MipsPhyReg(i);
            degree.put(phyReg, Integer.MAX_VALUE);
        }
    }

    /**
     * 在冲突图上添加无向边
     * @param u 第一个节点
     * @param v 第二个节点
     */
    private void addEdge(MipsOperand u, MipsOperand v){
        if(!u.equals(v) && !adjSet.contains(new Pair<>(u,v))){ // 如果不存在该边，且两个节点不相等
            adjSet.add(new Pair<>(u, v));
            adjSet.add(new Pair<>(v, u));
            if(!v.isPreColored()){ // v未预着色
                adjList.putIfAbsent(v, new HashSet<>());
                adjList.get(v).add(u);
                degree.compute(v, (key, value) -> value == null ? 0 : value + 1);
            }
            if(!u.isPreColored()){ // 不被预着色
                adjList.putIfAbsent(u, new HashSet<>());
                adjList.get(u).add(v);
                degree.compute(u, (key, value) -> value == null ? 0 : value + 1);
            }
        }
    }

    /**
     * 构建冲突图
     */
    private void buildConflictGraph(MipsFunction function){
        ArrayList<MipsBasicBlock> blockList = function.getMipsBasicBlocks();
//        System.out.println(blockList.size());
//        System.out.println(function.getName());
        // 倒序遍历
        for(int i = blockList.size()-1; i >= 0; i--){
            MipsBasicBlock block = blockList.get(i);

            // 获取当前基本块的活跃变量
            HashSet<MipsOperand> live = new HashSet<>(liveVarInfoMap.get(block).getLiveOut());
//            System.out.println(live);

            LinkedList<MipsInstruction> instructionList = block.getInstructions();
            // 倒序遍历所有指令
            for(int j = instructionList.size()-1; j >= 0; j--){
                MipsInstruction instruction = instructionList.get(j);
                ArrayList<MipsOperand> defRegs = instruction.getDefRegs();
                ArrayList<MipsOperand> useRegs = instruction.getUseRegs();

                if(instruction instanceof MipsMove){ // move指令，需要特殊处理
                    MipsMove move = (MipsMove) instruction;
                    MipsOperand src = move.getSrc();
                    MipsOperand dst = move.getDst();
                    if(src.needColoring() && dst.needColoring()){
                        live.remove(src);

                        moveList.putIfAbsent(dst, new HashSet<>());
                        moveList.get(dst).add(move);

                        moveList.putIfAbsent(src, new HashSet<>());
                        moveList.get(src).add(move);

                        // 可能会被合并
                        mergeAbleMoves.add(move);
                    }
                }

                for(MipsOperand reg : defRegs){
                    if(reg.needColoring()){
                        live.add(reg);
                    }
                }
                // 构建live和def的冲突
                for(MipsOperand d : defRegs){
                    if(d.needColoring()){
                        for(MipsOperand l : live){
                            addEdge(l, d);
                        }
                    }
                }

                // 启发式算法的依据，用于后面挑选出溢出节点
                for (MipsOperand reg : defRegs) {
                    loopDepths.put(reg, block.getLoopDepth() + 1);
                }
                for (MipsOperand reg : useRegs) {
                    loopDepths.put(reg, block.getLoopDepth() + 1);
                }

                // 说明这个指令不再存活
                defRegs.stream().filter(MipsOperand::needColoring).forEach(live::remove);
                // 活指令
                useRegs.stream().filter(MipsOperand::needColoring).forEach(live::add);
            }
        }
    }

    /**
     * 遍历所有非预着色节点，将这些节点加入不同的工作列表
     * @param function 待解析函数
     */
    private void makeWorkList(MipsFunction function){
        for(MipsVirReg virReg : function.getUsedVirRegs()){
            if(degree.getOrDefault(virReg, 0) >= K){ // 度大于K，则说明可能发生实际溢出
                spillList.add(virReg);
            } else if (relatedMove(virReg)) { // 与move相关,加入freezeList
                freezeList.add(virReg);
            } else {
                simplifyList.add(virReg);
            }
        }
    }

    /**
     * 判断当前寄存器是否是 move 指令的操作数
     * @param virReg 待检测节点
     * @return 返回 true 或者 false
     */
    private boolean relatedMove(MipsOperand virReg){
        return !getMovesByNode(virReg).isEmpty();
    }

    /**
     * 根据节点取出相关 move 指令，Active 或 MergeAble
     * @param u 待检测节点
     * @return move 指令集合
     */
    private Set<MipsMove> getMovesByNode(MipsOperand u){
        Set<MipsMove> res = new HashSet<>();
        Set<MipsMove> moves = moveList.getOrDefault(u, new HashSet<>());
        for(MipsMove move : moves){
            if(activeMoves.contains(move) || mergeAbleMoves.contains(move)){
                res.add(move);
            }
        }
        return res;
    }

    /**
     * 从adjList 中取出相应的节点
     * 去掉已经被删掉或者合并的节点
     * @param u 待检测节点
     * @return 相连的节点组
     */
    private Set<MipsOperand> getAdjacent(MipsOperand u){
        Set<MipsOperand> res = new HashSet<>();
        Set<MipsOperand> operands = adjList.getOrDefault(u, new HashSet<>());
        for(MipsOperand operand : operands){
            if(!(selectedStack.contains(operand) || coalescedNodes.contains(operand))){
                res.add(operand);
            }
        }
        return res;
    }

    /**
     * 将节点和其相连节点的 activeMoves删掉，加入mergeAble
     * 将节点和其相连 move 节点从"不能合并"状态转为"能合并"状态
     * @param u
     */
    private void enableMoves(MipsOperand u){
        makeMoves(u);
        for(MipsOperand operand : getAdjacent(u)){
            makeMoves(operand);
        }
    }

    private void makeMoves(MipsOperand operand){
        for(MipsMove move : getMovesByNode(operand)){
            if(activeMoves.contains(move)){
                activeMoves.remove(move);
                mergeAbleMoves.add(move);
            }
        }
    }

    /**
     * 降低节点的度，可能会移动list
     * @param u 待处理节点
     */
    private void decreaseDegree(MipsOperand u){
        int deg = degree.get(u);
        degree.put(u, deg - 1);

        if(deg == K){
            enableMoves(u);
            spillList.remove(u);
            if(relatedMove(u)){
                freezeList.add(u);
            } else {
                simplifyList.add(u);
            }
        }
    }

    /**
     * 选择能够进行着色的点，从simplify工作列表中取出，并入栈，降低相邻节点的度
     */
    private void doSimplify(){
        // 从可以简化的列表中取出一个节点
        MipsOperand u = simplifyList.iterator().next();
        simplifyList.remove(u);
        // 图着色时使用的栈，入栈
        selectedStack.add(u);

        for(MipsOperand operand : getAdjacent(u)){
            decreaseDegree(operand);
        }
    }

    /**
     * 对于合并的节点，可以有两个名字
     * @param u 被合并节点
     * @return 被合并的另一个节点
     */
    private MipsOperand getAlias(MipsOperand u){
        while(coalescedNodes.contains(u)){
            u = alias.get(u);
        }
        return u;
    }

    /**
     * 将节点从freezeWorkList 移动到 simplifyWorkList 中
     * @param u
     */
    private void addWorkList(MipsOperand u){
        if(!u.isPreColored() && !relatedMove(u) && degree.getOrDefault(u, 0) < K){
            freezeList.remove(u);
            simplifyList.add(u);
        }
    }

    /**
     * 根据 v 的临边关系判断， u、v节点是否可以合并
     * @return 是否可以合并
     */
    private boolean adjOk(MipsOperand v, MipsOperand u){
        return getAdjacent(v).stream().allMatch(t -> ok(t,u));
    }

    /**
     * 启发式函数，用于合并寄存器
     * @param u 待合并的虚拟寄存器的临接点
     * @param v 待合并的预着色寄存器
     * @return 是否可以被合并
     */
    private boolean ok(MipsOperand u, MipsOperand v){
        return degree.get(u) < K || u.isPreColored() || adjSet.contains(new Pair<>(u,v));
    }

    /**
     * briggs法判断是否可以合并
     * @return
     */
    private boolean conservative(MipsOperand u, MipsOperand v){
        Set<MipsOperand> uAdj = getAdjacent(u);
        Set<MipsOperand> vAdj = getAdjacent(v);
        uAdj.addAll(vAdj);

        long count = 0;
        for(MipsOperand n : uAdj){
            if(degree.get(n) >= K){
                count ++;
            }
        }
        return count < K;
    }

    /**
     * 合并操作
     * @param u 待合并节点
     * @param v 待合并节点
     */
    private void combine(MipsOperand u, MipsOperand v){
        if(freezeList.contains(v)){
            freezeList.remove(v);
        } else {
            spillList.remove(v);
        }

        coalescedNodes.add(v); // 将v放入被合并节点集合中
//        System.out.println("coalesced放入"+v);
        alias.put(v,u); //
        moveList.get(u).addAll(moveList.get(v)); // 将v对应的move指令加入u中


        for(MipsOperand operand : getAdjacent(v)){
            addEdge(operand, u);
            decreaseDegree(operand);
        }

        if(degree.getOrDefault(u, 0) >= K && freezeList.contains(u)){
            spillList.add(u);
            freezeList.remove(u);
        }
    }

    /**
     * 合并节点
     */
    private void coalesce(){
        MipsMove move = mergeAbleMoves.iterator().next();
        MipsOperand u = getAlias(move.getDst());
        MipsOperand v = getAlias(move.getSrc());

        if(v.isPreColored()){ // v是物理寄存器
            MipsOperand tmp = u;
            u = v;
            v = tmp;
        }

        mergeAbleMoves.remove(move);
        if(u.equals(v)){ // 合并
            coalescedMoves.add(move);
            addWorkList(u);
        } else if (v.isPreColored() || adjSet.contains(new Pair<>(u, v))) {
            constrainsMoves.add(move);
            addWorkList(u);
            addWorkList(v);
        } else if ((!u.isPreColored() && conservative(u, v)) || (u.isPreColored() && adjOk(v, u))) {
            coalescedMoves.add(move);
            combine(u, v);
            addWorkList(u);
        } else {
            activeMoves.add(move);
        }
    }

    /**
     * 将与 u 对应的所有 mov 指令从 active 和 mergeAble 中移出
     * @param u 待冻结的节点
     */
    private void freezeMoves(MipsOperand u){
        for(MipsMove mipsMove : getMovesByNode(u)){
            if(activeMoves.contains(mipsMove)){
                activeMoves.remove(mipsMove);
            } else {
                mergeAbleMoves.remove(mipsMove);
            }

            frozenMoves.add(mipsMove);
            MipsOperand v = getAlias(mipsMove.getDst()).equals(getAlias(u)) ? getAlias(mipsMove.getSrc()) : getAlias(mipsMove.getDst());
            if(!relatedMove(v) && degree.getOrDefault(v, 0) < K){
                freezeList.remove(v);
                simplifyList.add(v);
            }
        }
    }

    /**
     * 无法进行simplify：没有低度数，无关 mov 的点
     * 无法进行coalesce：没有符合要求可以合并的点
     * 则进行freeze
     */
    private void freeze(){
        MipsOperand u = freezeList.iterator().next();
        freezeList.remove(u);
        simplifyList.add(u);
        freezeMoves(u);
    }

    /**
     * 调出需要溢出的节点
     */
    private void chooseSpill(){
        double magicNum = 1.414;
        MipsOperand m = spillList.stream().max((l, r) -> {
            double val1 = degree.getOrDefault(l, 0).doubleValue() / Math.pow(magicNum, loopDepths.getOrDefault(l, 0));
            double val2 = degree.getOrDefault(r, 0).doubleValue() / Math.pow(magicNum, loopDepths.getOrDefault(l, 0));
            return Double.compare(val1, val2);
        }).get();

        simplifyList.add(m);
        freezeMoves(m);
        spillList.remove(m);
    }

    private void assignColor(MipsFunction function){
        HashMap<MipsOperand, MipsOperand> colored = new HashMap<>();
        while(!selectedStack.isEmpty()){
//            System.out.println(coalescedNodes);
            MipsOperand n = selectedStack.pop();
//            System.out.println("assign color："+n);
            Set<RegType> colors = new HashSet<>(RegType.regsCanAlloc);
            // 遍历弹出节点的邻接节点
            for(MipsOperand w : adjList.getOrDefault(n, new HashSet<>())){
                MipsOperand alia = getAlias(w);
                // 说明对应的物理寄存器已经被分配，需要移出
                if(alia.isAllocated() || alia.isPreColored()){
                    colors.remove(((MipsPhyReg) alia).getRegType());
                } else if (alia instanceof MipsVirReg) { // 是虚拟寄存器并且已经被着色
                    if(colored.containsKey(alia)){
                        // 该虚拟寄存器对应的物理寄存器已经被分配，需要移出
                        MipsOperand color = colored.get(alia);
                        colors.remove(((MipsPhyReg) color).getRegType());
                    }
                }
            }

            if(colors.isEmpty()){
                spilledNodes.add(n);
            } else {
                RegType color = colors.iterator().next();
                colored.put(n, new MipsPhyReg(color, true));
//                System.out.println("染色为 : " + color);
            }
        }
        if(!spilledNodes.isEmpty()){ // 有溢出
            return;
        }

        // 当处理完栈后没有任何问题，就可以处理合并节点
        for(MipsOperand coalesceNode : coalescedNodes){
//            System.out.println(coalescedNodes);
            MipsOperand alia = getAlias(coalesceNode);
            if(alia.isPreColored()){
                colored.put(coalesceNode, alia);
//                System.out.println("添加映射：" + coalesceNode + "-" + alia);
            } else { // 合并的节点是虚拟寄存器
                colored.put(coalesceNode, colored.get(alia));
            }
        }

        for(MipsBasicBlock block : function.getMipsBasicBlocks()){
            for(MipsInstruction instruction : block.getInstructions()){

                ArrayList<MipsOperand> defRegs = new ArrayList<>(instruction.getDefRegs());
                ArrayList<MipsOperand> useRegs = new ArrayList<>(instruction.getUseRegs());
                for(MipsOperand def : defRegs){
                    if(colored.containsKey(def)){
//                        System.out.println("完成替换："+def);
                        instruction.replaceReg(def, colored.get(def));
//                            System.out.println(colored.get(def));
                    }
                }
                for(MipsOperand use : useRegs){
                    if(colored.containsKey(use)){
                        instruction.replaceReg(use, colored.get(use));
//                            System.out.println(colored.get(use));
                    }
                }
            }
        }
    }

    /**
     * 溢出寄存器替换：首次使用
     */
    private MipsInstruction firstUseStore = null;
    private MipsBasicBlock firstUseBlock = null;
    /**
     * 溢出寄存器替换：最后一次定义
     */
    private MipsInstruction lastDefLoad = null;
    private MipsBasicBlock lastDefBlock = null;

    private LinkedList<MipsInstruction> repInstructions = new LinkedList<>();


    private void fixOffset(MipsFunction func, MipsInstruction instr) {
        int offset = func.getAllocatedSize();
        MipsImme mipsOffset = new MipsImme(offset);
        if (instr instanceof MipsLoad mipsLoad) { // offset 的编码规则与之前不同
            mipsLoad.setOp2(mipsOffset);
        } else if (instr instanceof MipsStore mipsStore) {
            mipsStore.setOffset(mipsOffset);
        }
    }
    /**
     * 处理溢出的临时变量存放在基本块中
     */
    private void checkPoint(MipsFunction function){
        int offset = function.getAllocatedSize();
        MipsImme offsetImm = new MipsImme(offset);
        if(lastDefLoad != null){
            MipsStore store = new MipsStore(virReg, MipsPhyReg.SP, offsetImm);
            lastDefBlock.insertAfter(lastDefLoad, store);
            fixOffset(function, store);
            lastDefLoad = null;
        }
        if(firstUseStore != null){
            MipsLoad load = new MipsLoad(virReg, MipsPhyReg.SP, offsetImm);
            firstUseBlock.insertBefore(firstUseStore, load);
            fixOffset(function, load);
            firstUseStore = null;
        }
        virReg = null;
    }

    private void rebuildProgram(MipsFunction func){
        for (MipsOperand n : spilledNodes) {
//            System.out.println("处理溢出的结点：" + n);

            // 遍历所有基本块
            ArrayList<MipsBasicBlock> blocks = func.getMipsBasicBlocks();
            for (MipsBasicBlock block : blocks) {
                virReg = null;
                firstUseStore = null;
                lastDefLoad = null;

                repInstructions = block.getInstructions();
                // 遍历所有指令
                int cntInstr = 0;
                for(int i = 0; i < repInstructions.size(); i++){
                    MipsInstruction instruction = repInstructions.get(i);
                    ArrayList<MipsOperand> defRegs = new ArrayList<>(instruction.getDefRegs());
                    ArrayList<MipsOperand> useRegs = new ArrayList<>(instruction.getUseRegs());

                    // 遍历该指令内的所有 use，如果使用过当前溢出的寄存器 n，那么取消该寄存器分配，转而换为虚拟寄存器
                    for (MipsOperand use : useRegs) {
                        if (use.equals(n)) {
                            if (virReg == null) {
                                virReg = new MipsVirReg();
                                func.addVirReg(virReg);
                            }
                            instruction.replaceReg(use, virReg);

                            if (firstUseStore == null && lastDefLoad == null) {
                                firstUseStore = instruction;
                                firstUseBlock = block;
                            }
                        }
                    }

                    // 遍历该指令内的所有 def，如果定义过当前溢出的寄存器 n，那么取消该寄存器分配，转而换为虚拟寄存器
                    for (MipsOperand def : defRegs) {
                        if (def.equals(n)) {
                            if (virReg == null) {
                                virReg = new MipsVirReg();
                                func.addVirReg(virReg);
                            }
                            instruction.replaceReg(def, virReg);
                            lastDefLoad = instruction;
                            lastDefBlock = block;
                        }
                    }
                    if (cntInstr > 30) {
                        if(firstUseStore != null){
                            i++;
                        }
                        checkPoint(func);
                    }
                    cntInstr++;
                }
                checkPoint(func);
            }
            // 为这个临时变量在栈上分配空间
            func.allocSpace(4);
        }
    }


    /**
     * 重置所有物理寄存器为未分配状态
     */
    private void clearPhyState(MipsModule mipsModule){
        for (MipsFunction function : mipsModule.getMipsFunctions()) {
            for (MipsBasicBlock block : function.getMipsBasicBlocks()) {
                for (MipsInstruction instr : block.getInstructions()) {
                    for (MipsOperand reg : instr.getDefRegs()) {
                        if (reg instanceof MipsPhyReg) {
                            ((MipsPhyReg) reg).setAllocated(false);
                        }
                    }
                    for (MipsOperand reg : instr.getUseRegs()) {
                        if (reg instanceof MipsPhyReg) {
                            ((MipsPhyReg) reg).setAllocated(false);
                        }
                    }
                }
            }
        }
    }

    public void process(MipsModule mipsModule){
        for(MipsFunction function : mipsModule.getNotLibFunctions()){
            boolean isOver = false;
            while(!isOver){
                init(function);
                buildConflictGraph(function);
                makeWorkList(function);
                while(!(simplifyList.isEmpty() && spillList.isEmpty() && freezeList.isEmpty() && mergeAbleMoves.isEmpty())){
                    if(!simplifyList.isEmpty()){
                        doSimplify();
                    }
                    if(!mergeAbleMoves.isEmpty()){
                        coalesce();
                    }
                    if(!freezeList.isEmpty()){
                        freeze();
                    }
                    if(!spillList.isEmpty()){
                        chooseSpill();
                    }
                }
                assignColor(function);
                if(spilledNodes.isEmpty()){
                    isOver = true;
                } else { // 有溢出
                    rebuildProgram(function);
                }
            }
        }
        clearPhyState(mipsModule);
        for(MipsFunction function : mipsModule.getNotLibFunctions()){
            function.buildStack();
        }
    }

}
