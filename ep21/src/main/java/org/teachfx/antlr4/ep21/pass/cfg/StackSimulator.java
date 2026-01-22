package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;

import java.util.*;

/**
 * 栈帧模拟器 (Stack Frame Simulator)
 * 
 * <p>用于尾递归优化中的栈帧分析和模拟。</p>
 * 
 * <h2>核心功能</h2>
 * <ul>
 *   <li>活跃变量分析：确定变量的活跃区间</li>
 *   <li>栈帧重用：确定哪些栈帧位置可以安全重用</li>
 *   <li>逃逸分析：检测变量是否"逃逸"到外部</li>
 * </ul>
 * 
 * <h2>使用示例</h2>
 * <pre>{@code
 * StackSimulator<IRNode> simulator = new StackSimulator<>(cfg, domTree);
 * simulator.analyze();
 * 
 * // 检查变量是否可重用
 * boolean reusable = simulator.isSlotReusable(slotIndex);
 * 
 * // 获取变量的活跃区间
 * Interval liveRange = simulator.getLiveRange(variable);
 * }</pre>
 * 
 * @author EP21 Team
 * @version 1.0
 */
public class StackSimulator<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(StackSimulator.class);

    private final CFG<I> cfg;
    private final DominatorTree<I> domTree;

    /** 活跃变量分析结果 */
    private Map<String, VariableInfo> variableInfo = new HashMap<>();

    /** 栈帧槽位信息 */
    private List<SlotInfo> slots = new ArrayList<>();

    /** 逃逸变量集合 */
    private Set<String> escapedVariables = new HashSet<>();

    /** 分析是否完成 */
    private boolean analyzed = false;

    public StackSimulator(CFG<I> cfg, DominatorTree<I> domTree) {
        this.cfg = cfg;
        this.domTree = domTree;
    }

    /**
     * 执行栈帧分析
     */
    public void analyze() {
        logger.info("开始栈帧分析...");

        // 1. 收集变量信息
        collectVariables();

        // 2. 执行活跃变量分析
        livenessAnalysis();

        // 3. 检测逃逸变量
        escapeAnalysis();

        // 4. 分析栈帧槽位
        analyzeSlots();

        analyzed = true;

        logger.info("栈帧分析完成: 发现 {} 个变量, {} 个逃逸变量, {} 个槽位",
                    variableInfo.size(), escapedVariables.size(), slots.size());
    }

    /**
     * 收集所有变量信息
     */
    private void collectVariables() {
        variableInfo.clear();

        for (BasicBlock<I> block : cfg) {
            for (Loc<I> loc : block) {
                if (loc.instr instanceof Assign assign) {
                    // 收集赋值目标变量
                    if (assign.getLhs() instanceof FrameSlot frameSlot) {
                        String varName = frameSlot.getVariableName();
                        if (varName != null) {
                            variableInfo.computeIfAbsent(varName, VariableInfo::new);
                        }
                    }

                    // 收集使用的变量
                    collectUsedVariables(assign.getRhs());
                }
            }
        }

        logger.debug("收集到 {} 个变量", variableInfo.size());
    }

    /**
     * 收集表达式中使用的变量
     */
    private void collectUsedVariables(Object expr) {
        if (expr == null) return;

        String exprStr = expr.toString();
        // 解析表达式中的变量名
        for (String varName : variableInfo.keySet()) {
            if (exprStr.contains(varName)) {
                variableInfo.get(varName).addUse();
            }
        }
    }

    /**
     * 活跃变量分析 (Dataflow Analysis)
     * 
     * <p>使用反向数据流分析计算每个点的活跃变量集合。</p>
     */
    private void livenessAnalysis() {
        // 初始化：所有变量初始为不活跃
        Map<Integer, Set<String>> liveIn = new HashMap<>();
        Map<Integer, Set<String>> liveOut = new HashMap<>();

        // 反向遍历块
        boolean changed = true;
        int iterations = 0;
        int maxIterations = cfg.nodes.size() * cfg.nodes.size();

        while (changed && iterations < maxIterations) {
            changed = false;
            iterations++;

            for (int i = cfg.nodes.size() - 1; i >= 0; i--) {
                BasicBlock<I> block = cfg.getBlock(i);
                if (block == null) continue;

                Set<String> live = new HashSet<>();

                // liveout[B] = ∪_{S ∈ succ(B)} livein[S]
                Set<Integer> succs = cfg.getSucceed(i);
                for (int succId : succs) {
                    Set<String> succLiveIn = liveIn.getOrDefault(succId, new HashSet<>());
                    live.addAll(succLiveIn);
                }

                // 计算块内的活跃变量
                Set<String> blockLive = new HashSet<>(live);

                // 逆向遍历块内指令
                List<Loc<I>> instructions = new ArrayList<>(block.codes);
                for (int j = instructions.size() - 1; j >= 0; j--) {
                    IRNode instr = instructions.get(j).instr;

                    // 移除定义
                    if (instr instanceof Assign assign) {
                        if (assign.getLhs() instanceof FrameSlot frameSlot) {
                            String varName = frameSlot.getVariableName();
                            if (varName != null) {
                                blockLive.remove(varName);
                                variableInfo.get(varName).addDef(i);
                            }
                        }
                        // 添加使用
                        collectUsedVariablesLive(blockLive, assign.getRhs());
                    }
                }

                // livein[B] = blockLive ∪ (liveout[B] - ∪_{D ∈ def(B)} {D})
                // 这里简化处理，使用blockLive作为livein

                if (!Objects.equals(liveIn.get(i), blockLive)) {
                    liveIn.put(i, blockLive);
                    changed = true;
                }
            }
        }

        logger.debug("活跃变量分析完成: {} 次迭代", iterations);

        // 更新变量的活跃区间
        updateLiveRanges(liveIn);
    }

    /**
     * 收集活跃变量
     */
    private void collectUsedVariablesLive(Set<String> live, Object expr) {
        if (expr == null) return;

        String exprStr = expr.toString();
        for (String varName : variableInfo.keySet()) {
            if (exprStr.contains(varName)) {
                live.add(varName);
                variableInfo.get(varName).addUse();
            }
        }
    }

    /**
     * 更新变量的活跃区间
     */
    private void updateLiveRanges(Map<Integer, Set<String>> liveIn) {
        for (Map.Entry<Integer, Set<String>> entry : liveIn.entrySet()) {
            int blockId = entry.getKey();
            Set<String> live = entry.getValue();

            for (String varName : live) {
                VariableInfo info = variableInfo.get(varName);
                info.addLiveBlock(blockId);
            }
        }
    }

    /**
     * 逃逸分析
     * 
     * <p>检测变量是否"逃逸"到外部作用域。</p>
     */
    private void escapeAnalysis() {
        escapedVariables.clear();

        for (Map.Entry<String, VariableInfo> entry : variableInfo.entrySet()) {
            String varName = entry.getKey();
            VariableInfo info = entry.getValue();

            // 如果变量在多个基本块中使用，可能逃逸
            if (info.getLiveBlocks().size() > 1) {
                escapedVariables.add(varName);
                logger.debug("变量 {} 逃逸 (活跃在 {} 个块)", varName, info.getLiveBlocks().size());
            }

            // 如果变量被赋值且值被用于返回值，可能逃逸
            if (info.getDefCount() > 1) {
                escapedVariables.add(varName);
                logger.debug("变量 {} 逃逸 (多次定义)", varName);
            }
        }

        logger.debug("逃逸分析完成: {} 个逃逸变量", escapedVariables.size());
    }

    /**
     * 分析栈帧槽位
     */
    private void analyzeSlots() {
        slots.clear();

        // 为每个变量创建槽位信息
        int slotIndex = 0;
        for (Map.Entry<String, VariableInfo> entry : variableInfo.entrySet()) {
            String varName = entry.getKey();
            VariableInfo varInfo = entry.getValue();

            SlotInfo slotInfo = new SlotInfo(slotIndex++, varName);
            slotInfo.setReusable(!escapedVariables.contains(varName));
            slotInfo.setLiveRange(varInfo.getLiveBlocks());

            slots.add(slotInfo);

            logger.debug("槽位 {}: {} (可重用: {})", slotInfo.index, varName, slotInfo.isReusable());
        }

        logger.debug("槽位分析完成: {} 个槽位", slots.size());
    }

    /**
     * 检查槽位是否可重用
     */
    public boolean isSlotReusable(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= slots.size()) {
            return false;
        }
        return slots.get(slotIndex).isReusable();
    }

    /**
     * 检查变量是否逃逸
     */
    public boolean isVariableEscaped(String varName) {
        return escapedVariables.contains(varName);
    }

    /**
     * 获取变量的活跃区间
     */
    public Interval getLiveRange(String varName) {
        VariableInfo info = variableInfo.get(varName);
        if (info == null || info.getLiveBlocks().isEmpty()) {
            return null;
        }

        Set<Integer> liveBlocks = info.getLiveBlocks();
        int start = Collections.min(liveBlocks);
        int end = Collections.max(liveBlocks);

        return new Interval(start, end);
    }

    /**
     * 检查两个槽位是否干扰
     */
    public boolean interfere(int slot1, int slot2) {
        if (slot1 < 0 || slot1 >= slots.size() ||
            slot2 < 0 || slot2 >= slots.size()) {
            return false;
        }

        Interval range1 = slots.get(slot1).getLiveRange();
        Interval range2 = slots.get(slot2).getLiveRange();

        if (range1 == null || range2 == null) {
            return false;
        }

        return range1.overlaps(range2);
    }

    /**
     * 获取变量信息
     */
    public VariableInfo getVariableInfo(String varName) {
        return variableInfo.get(varName);
    }

    /**
     * 获取所有变量名
     */
    public Set<String> getVariables() {
        return Collections.unmodifiableSet(variableInfo.keySet());
    }

    /**
     * 获取槽位数量
     */
    public int getSlotCount() {
        return slots.size();
    }

    /**
     * 获取槽位信息
     */
    public SlotInfo getSlot(int index) {
        if (index < 0 || index >= slots.size()) {
            return null;
        }
        return slots.get(index);
    }

    /**
     * 检查分析是否完成
     */
    public boolean isAnalyzed() {
        return analyzed;
    }

    /**
     * 获取分析摘要
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("StackSimulator Summary:\n");
        sb.append("  Variables: ").append(variableInfo.size()).append("\n");
        sb.append("  Escaped: ").append(escapedVariables.size()).append("\n");
        sb.append("  Slots: ").append(slots.size()).append("\n");
        sb.append("  Reusable slots: ").append(
            slots.stream().filter(SlotInfo::isReusable).count()).append("\n");
        return sb.toString();
    }

    /**
     * 变量信息
     */
    public static class VariableInfo {
        private final String name;
        private final Set<Integer> liveBlocks = new HashSet<>();
        private int defCount = 0;
        private int useCount = 0;

        public VariableInfo(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public void addDef(int blockId) {
            defCount++;
            liveBlocks.add(blockId);
        }

        public void addUse() {
            useCount++;
        }

        public void addLiveBlock(int blockId) {
            liveBlocks.add(blockId);
        }

        public int getDefCount() {
            return defCount;
        }

        public int getUseCount() {
            return useCount;
        }

        public Set<Integer> getLiveBlocks() {
            return Collections.unmodifiableSet(liveBlocks);
        }
    }

    /**
     * 栈帧槽位信息
     */
    public static class SlotInfo {
        private final int index;
        private final String variableName;
        private boolean reusable = false;
        private Interval liveRange;

        public SlotInfo(int index, String variableName) {
            this.index = index;
            this.variableName = variableName;
        }

        public int getIndex() {
            return index;
        }

        public String getVariableName() {
            return variableName;
        }

        public void setReusable(boolean reusable) {
            this.reusable = reusable;
        }

        public boolean isReusable() {
            return reusable;
        }

        public void setLiveRange(Set<Integer> liveBlocks) {
            if (liveBlocks.isEmpty()) {
                this.liveRange = null;
            } else {
                int start = Collections.min(liveBlocks);
                int end = Collections.max(liveBlocks);
                this.liveRange = new Interval(start, end);
            }
        }

        public Interval getLiveRange() {
            return liveRange;
        }
    }

    /**
     * 区间（用于表示活跃范围）
     */
    public static class Interval {
        private final int start;
        private final int end;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public boolean contains(int point) {
            return start <= point && point <= end;
        }

        public boolean overlaps(Interval other) {
            return !(end < other.start || start > other.end);
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }
    }
}
