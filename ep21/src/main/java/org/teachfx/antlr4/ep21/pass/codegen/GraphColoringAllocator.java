package org.teachfx.antlr4.ep21.pass.codegen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.symtab.symbol.VariableSymbol;

import java.util.*;

public class GraphColoringAllocator implements IRegisterAllocator {

    private static final Logger logger = LogManager.getLogger(GraphColoringAllocator.class);

    private static final int NUM_REGISTERS = 13;
    private static final int FIRST_ALLOCABLE_REG = 1;
    private static final int LAST_ALLOCABLE_REG = 12;

    private final Map<VariableSymbol, Integer> varToReg = new HashMap<>();
    private final Map<Integer, VariableSymbol> regToVar = new HashMap<>();
    private final Map<VariableSymbol, LiveInterval> liveIntervals = new HashMap<>();
    private final Map<VariableSymbol, Integer> spillSlots = new HashMap<>();
    private int nextSpillSlot = 0;
    private int spilledCount = 0;

    private final Map<VariableSymbol, Set<VariableSymbol>> interferenceGraph = new HashMap<>();

    public GraphColoringAllocator() {
        logger.debug("图着色寄存器分配器已创建");
    }

    @Override
    public int allocateRegister(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        if (varToReg.containsKey(variable)) {
            return varToReg.get(variable);
        }

        if (!liveIntervals.containsKey(variable)) {
            logger.warn("变量 {} 缺少活跃区间，直接分配寄存器1", variable.getName());
            varToReg.put(variable, FIRST_ALLOCABLE_REG);
            regToVar.put(FIRST_ALLOCABLE_REG, variable);
            return FIRST_ALLOCABLE_REG;
        }

        List<VariableSymbol> spilled = colorGraph(NUM_REGISTERS);
        Integer reg = varToReg.get(variable);

        if (reg == null) {
            Integer spillSlot = spillSlots.get(variable);
            if (spillSlot != null) {
                return -1 - spillSlot;
            }
        }

        return reg;
    }

    @Override
    public int getStackOffset(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        Integer spillSlot = spillSlots.get(variable);
        if (spillSlot == null) {
            return -1;
        }

        return -1 - spillSlot;
    }

    @Override
    public void reset() {
        varToReg.clear();
        regToVar.clear();
        spillSlots.clear();
        nextSpillSlot = 0;
        spilledCount = 0;
        liveIntervals.clear();
        interferenceGraph.clear();
        logger.debug("图着色分配器已重置");
    }

    @Override
    public int getAllocatedRegisterCount() {
        return varToReg.size();
    }

    @Override
    public int getRegister(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        return varToReg.getOrDefault(variable, -1);
    }

    @Override
    public boolean isSpilled(VariableSymbol variable) {
        if (variable == null) {
            throw new IllegalArgumentException("Variable cannot be null");
        }

        return spillSlots.containsKey(variable);
    }

    @Override
    public void freeRegister(VariableSymbol variable) {
        Integer reg = varToReg.remove(variable);
        if (reg != null) {
            regToVar.remove(reg);
            logger.debug("释放寄存器 R{} (变量 {})", reg, variable.getName());
        }
    }

    public void setLiveIntervals(Map<VariableSymbol, LiveInterval> intervals) {
        this.liveIntervals.clear();
        this.liveIntervals.putAll(intervals);
        buildInterferenceGraph();
        logger.debug("设置了 {} 个变量的活跃区间", intervals.size());
    }

    public void buildInterferenceGraph() {
        interferenceGraph.clear();

        for (VariableSymbol var1 : liveIntervals.keySet()) {
            LiveInterval interval1 = liveIntervals.get(var1);
            if (interval1 == null) continue;

            Set<VariableSymbol> conflicts = new HashSet<>();

            for (VariableSymbol var2 : liveIntervals.keySet()) {
                if (var1.equals(var2)) continue;

                LiveInterval interval2 = liveIntervals.get(var2);
                if (interval2 != null && interval1.overlaps(interval2)) {
                    conflicts.add(var2);
                }
            }

            if (!conflicts.isEmpty()) {
                interferenceGraph.put(var1, conflicts);
            }
        }

        logger.debug("构建干扰图：{} 个顶点，{} 条边",
                   interferenceGraph.size(), getTotalEdges());
    }

    private int getTotalEdges() {
        int total = 0;
        for (Set<VariableSymbol> edges : interferenceGraph.values()) {
            total += edges.size();
        }
        return total / 2;
    }

    public List<VariableSymbol> colorGraph(int k) {
        logger.debug("开始图着色，k={}", k);

        Stack<VariableSymbol> coloringStack = new Stack<>();
        Map<VariableSymbol, Integer> colors = new HashMap<>();
        Map<VariableSymbol, Integer> spilledMap = new HashMap<>();

        List<VariableSymbol> nodes = new ArrayList<>(liveIntervals.keySet());

        for (VariableSymbol node : nodes) {
            Set<VariableSymbol> neighbors = interferenceGraph.getOrDefault(node, new HashSet<>());

            if (neighbors.size() < k) {
                coloringStack.push(node);
            } else {
                spilledMap.put(node, 1);
            }
        }

        while (!coloringStack.isEmpty()) {
            VariableSymbol node = coloringStack.pop();

            Integer color = findAvailableColor(node, colors, k);
            if (color != null) {
                colors.put(node, color);
                varToReg.put(node, color);
                regToVar.put(color, node);
            }
        }

        for (VariableSymbol s : spilledMap.keySet()) {
            int slot = allocateSpillSlot(s);
        }

        logger.debug("图着色完成：分配了 {} 个寄存器，溢出了 {} 个变量",
                   colors.size(), spilledMap.size());

        return new ArrayList<>(spilledMap.keySet());
    }

    private Integer findAvailableColor(VariableSymbol node, Map<VariableSymbol, Integer> colors, int k) {
        Set<VariableSymbol> neighbors = interferenceGraph.getOrDefault(node, new HashSet<>());

        for (int c = 1; c <= k; c++) {
            boolean isAvailable = true;
            for (VariableSymbol neighbor : neighbors) {
                Integer neighborColor = colors.get(neighbor);
                if (neighborColor != null && neighborColor == c) {
                    isAvailable = false;
                    break;
                }
            }
            if (isAvailable) {
                return c;
            }
        }
        return null;
    }

    private int allocateSpillSlot(VariableSymbol variable) {
        int slot;
        if (spillSlots.containsKey(variable)) {
            slot = spillSlots.get(variable);
        } else {
            slot = nextSpillSlot++;
            spillSlots.put(variable, slot);
            spilledCount++;
        }
        return slot;
    }

    public int getSpilledCount() {
        return spilledCount;
    }

    public Map<VariableSymbol, Integer> getSpillSlots() {
        return new HashMap<>(spillSlots);
    }

    public Map<VariableSymbol, LiveInterval> getLiveIntervals() {
        return new HashMap<>(liveIntervals);
    }

    public Map<VariableSymbol, Set<VariableSymbol>> getInterferenceGraph() {
        return new HashMap<>(interferenceGraph);
    }

    @Override
    public String toString() {
        return String.format("GraphColoringAllocator{allocated=%d, spilled=%d}",
                           varToReg.size(), spilledCount);
    }
}
