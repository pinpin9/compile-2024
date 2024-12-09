package tools;

import backend.opt.MulOptimizer;

import java.util.ArrayList;
import java.util.HashMap;

public class MipsMath {
    public static int countTailZeroNumber(int n) {
        int cnt = 0;
        n = n >>> 1;
        while (n != 0) {
            n = n >>> 1;
            cnt++;
        }
        return cnt;
    }

    /**
     * 是否能够编码为16位整数
     * @param signExtend 是否拓展至负数
     */
    public static boolean is16BitImm(int imm, boolean signExtend) {
        if (signExtend) {
            return Short.MIN_VALUE <= imm && imm <= Short.MAX_VALUE;
        }
        else {
            return 0 <= imm && imm <= (Short.MAX_VALUE - Short.MIN_VALUE);
        }
    }

    /**
     * 判断是否为2的幂次，要求x为非负整数
     */
    public static boolean isPow2(int x){
        return (x & (x - 1)) == 0;
    }

    /**
     * 根据乘常数查询对应的优化序列
     */
    private static final HashMap<Integer, MulOptimizer> mulOptimizers = new HashMap<>();



    // 生成 mulOptimizers
    static {
        ArrayList<MulOptimizer> tmpLists = new ArrayList<>();
        // 只是一个标签而已，没有实际意义，为了让一个 shift 发挥两次作用
        int NEGATIVE_TAG = 0x80000000;
        // 因为基准是 4，所以最多可以采用 3 个正向 shift，所以有 i, j, k 三个
        for (int i = 0; i < 32; i++) {
            tmpLists.add(new MulOptimizer(i));
            tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG));
            for (int j = 0; j < 32; j++) {
                tmpLists.add(new MulOptimizer(i, j));
                tmpLists.add(new MulOptimizer(i, j | NEGATIVE_TAG));
                tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j));
                tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j | NEGATIVE_TAG));
                for (int k = 0; k < 32; k++) {
                    tmpLists.add(new MulOptimizer(i, j, k));
                    tmpLists.add(new MulOptimizer(i, j, k | NEGATIVE_TAG));
                    tmpLists.add(new MulOptimizer(i, j | NEGATIVE_TAG, k));
                    tmpLists.add(new MulOptimizer(i, j | NEGATIVE_TAG, k | NEGATIVE_TAG));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j, k));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j, k | NEGATIVE_TAG));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j | NEGATIVE_TAG, k));
                    tmpLists.add(new MulOptimizer(i | NEGATIVE_TAG, j | NEGATIVE_TAG, k | NEGATIVE_TAG));
                }
            }
        }
        // 通过这个筛选，获得比基准情况和其他优化情况更优的优化
        for (MulOptimizer tmp : tmpLists) {
            if (tmp.isBetter()) {
                if (!mulOptimizers.containsKey(tmp.getMultiplier()) ||
                        tmp.getSteps() < mulOptimizers.get(tmp.getMultiplier()).getSteps()) {
                    mulOptimizers.put(tmp.getMultiplier(), tmp);
                }
            }
        }
    }

    public static ArrayList<Pair<Boolean, Integer>> getMulOptItems(int multiplier) {
        if (mulOptimizers.containsKey(multiplier)) {
            return mulOptimizers.get(multiplier).getItems();
        } else {
            return null;
        }
    }
}
