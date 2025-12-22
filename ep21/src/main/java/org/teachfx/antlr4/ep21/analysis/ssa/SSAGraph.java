package org.teachfx.antlr4.ep21.analysis.ssa;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.JMP;
import org.teachfx.antlr4.ep21.ir.stmt.CJMP;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.*;

/**
 * SSA图生成器 - 生成静态单赋值形式的控制流图
 */
public class SSAGraph {
    private final CFG<IRNode> originalCFG;
    private final Map<String, Integer> versionMap;
    private final Map<IRNode, IRNode> renamedNodes;
    private org.teachfx.antlr4.ep21.analysis.ssa.DominatorAnalysis<IRNode> dominatorAnalysis;
    // 变量重命名相关字段
    private final Map<String, Stack<Integer>> varStacks;
    private final Map<String, Integer> currentVersion;

    public SSAGraph(CFG<IRNode> cfg) {
        this.originalCFG = cfg;
        this.versionMap = new HashMap<>();
        this.renamedNodes = new HashMap<>();
        this.dominatorAnalysis = new org.teachfx.antlr4.ep21.analysis.ssa.DominatorAnalysis<>(cfg);
        this.dominatorAnalysis.analyze();
        this.varStacks = new HashMap<>();
        this.currentVersion = new HashMap<>();
    }
    
    /**
     * 构建SSA图
     */
    public SSAGraph buildSSA() {
        // 1. 插入Φ函数
        insertPhiFunctions();
        
        // 2. 变量重命名
        renameVariables();
        
        return this;
    }
    
    /**
     * 插入Φ函数到汇聚节点（基于支配边界算法）
     *
     * 标准SSA算法（Cytron等人）：
     * 1. 收集所有变量及其定义位置（基本块）
     * 2. 对于每个变量，使用工作列表算法在支配边界插入Phi函数
     */
    private void insertPhiFunctions() {
        // 步骤1：收集变量定义位置
        Map<String, Set<Integer>> varDefs = collectVariableDefinitions();

        // 步骤2：为每个变量插入Phi函数
        for (Map.Entry<String, Set<Integer>> entry : varDefs.entrySet()) {
            String varName = entry.getKey();
            Set<Integer> defBlocks = entry.getValue();
            insertPhiFunctionsForVariable(varName, defBlocks);
        }
    }

    /**
     * 收集所有变量及其定义位置（基本块ID）
     */
    private Map<String, Set<Integer>> collectVariableDefinitions() {
        Map<String, Set<Integer>> varDefs = new HashMap<>();

        for (BasicBlock<IRNode> block : originalCFG.nodes) {
            Set<String> definedVars = getDefinedVariables(block);
            for (String var : definedVars) {
                varDefs.computeIfAbsent(var, k -> new HashSet<>()).add(block.getId());
            }
        }

        return varDefs;
    }

    /**
     * 为特定变量插入Phi函数（基于支配边界的工作列表算法）
     */
    private void insertPhiFunctionsForVariable(String varName, Set<Integer> defBlocks) {
        // 工作列表：包含所有定义块
        Queue<Integer> worklist = new ArrayDeque<>(defBlocks);
        // 已处理的块
        Set<Integer> processed = new HashSet<>();
        // 已插入Phi函数的块
        Set<Integer> hasPhi = new HashSet<>();

        while (!worklist.isEmpty()) {
            int blockId = worklist.poll();
            if (processed.contains(blockId)) {
                continue;
            }
            processed.add(blockId);

            // 获取该块的支配边界
            Set<Integer> frontier = dominatorAnalysis.getDominanceFrontier(blockId);
            if (frontier == null) {
                continue;
            }

            for (int dfBlockId : frontier) {
                if (!hasPhi.contains(dfBlockId)) {
                    // 在这个块中插入Phi函数
                    BasicBlock<IRNode> dfBlock = originalCFG.getBlock(dfBlockId);
                    if (dfBlock != null) {
                        insertPhiFunction(dfBlock, varName);
                        hasPhi.add(dfBlockId);

                        // 如果这个块之前没有Phi函数，加入工作列表
                        if (!defBlocks.contains(dfBlockId)) {
                            worklist.add(dfBlockId);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * 在基本块开头插入Φ函数（占位符）
     *
     * 插入Phi函数作为占位符，参数列表在重命名阶段填充。
     * 结果变量使用临时名称，重命名阶段会分配正确版本。
     */
    private void insertPhiFunction(BasicBlock<IRNode> block, String varName) {
        // 临时变量名，重命名阶段会分配正确版本
        String tempPhiVar = varName + "_phi";

        // 空参数列表，重命名阶段填充
        List<String> phiArgs = new ArrayList<>();

        // 创建Φ函数节点
        SSAPhiFunction phiFunction = new SSAPhiFunction(tempPhiVar, phiArgs);

        // 在基本块开头插入Φ函数
        Loc<IRNode> phiLoc = new Loc<>(phiFunction);
        block.codes.add(0, phiLoc);
    }
    
    /**
     * 变量重命名（完整SSA算法）
     *
     * 基于支配树的递归重命名算法：
     * 1. 为每个变量维护版本栈
     * 2. 深度优先遍历支配树
     * 3. 在每个基本块中处理Phi函数、重命名变量使用和定义
     * 4. 为后继基本块的Phi函数填充参数
     */
    private void renameVariables() {
        // 初始化变量栈
        varStacks.clear();
        currentVersion.clear();

        // 构建支配树的孩子关系
        Map<Integer, List<Integer>> domChildren = buildDominatorChildren();

        // 从入口块开始递归重命名（假设入口块ID为0）
        renameInBlock(0, domChildren);
    }

    /**
     * 构建支配树的孩子关系（节点ID -> 孩子节点ID列表）
     */
    private Map<Integer, List<Integer>> buildDominatorChildren() {
        Map<Integer, List<Integer>> children = new HashMap<>();
        Map<Integer, Integer> idom = dominatorAnalysis.getImmediateDominators();

        for (Map.Entry<Integer, Integer> entry : idom.entrySet()) {
            int node = entry.getKey();
            Integer parent = entry.getValue();
            if (parent != null) {
                children.computeIfAbsent(parent, k -> new ArrayList<>()).add(node);
            }
        }

        return children;
    }

    /**
     * 在基本块中递归执行变量重命名
     */
    private void renameInBlock(int blockId, Map<Integer, List<Integer>> domChildren) {
        BasicBlock<IRNode> block = originalCFG.getBlock(blockId);
        if (block == null) {
            return;
        }

        // 步骤1：处理当前基本块的Phi函数
        // 为每个Phi函数分配新版本
        List<SSAPhiFunction> blockPhis = new ArrayList<>();
        for (Loc<IRNode> loc : block.codes) {
            IRNode instr = loc.getInstruction();
            if (instr instanceof SSAPhiFunction phi) {
                blockPhis.add(phi);
            } else {
                // Phi函数都在基本块开头，遇到非Phi函数时停止
                break;
            }
        }

        // 记录Phi函数分配的新版本，用于后续填充参数
        Map<SSAPhiFunction, Integer> phiVersions = new HashMap<>();
        for (SSAPhiFunction phi : blockPhis) {
            // 提取变量名（去掉"_phi"后缀）
            String varName = phi.result.replace("_phi", "");

            // 分配新版本
            int newVersion = getNextVersion(varName);
            currentVersion.put(varName, newVersion);

            // 将新版本压栈
            varStacks.computeIfAbsent(varName, k -> new Stack<>()).push(newVersion);

            // 记录Phi函数对应的版本
            phiVersions.put(phi, newVersion);

            // 更新Phi函数的结果变量名
            phi.result = varName + "_" + newVersion;
        }

        // 步骤2：重命名基本块中的普通指令
        for (int i = blockPhis.size(); i < block.codes.size(); i++) {
            Loc<IRNode> loc = block.codes.get(i);
            IRNode instr = loc.getInstruction();

            // 处理Assign指令
            if (instr instanceof Assign assign) {
                // 重命名目标变量（定义）
                String varName = getVariableName(assign.getLhs());
                int newVersion = getNextVersion(varName);
                currentVersion.put(varName, newVersion);
                varStacks.computeIfAbsent(varName, k -> new Stack<>()).push(newVersion);

                // 重命名源操作数（使用）
                Operand renamedRhs = renameOperand(assign.getRhs());

                // 创建重命名后的指令
                IRNode renamedAssign = Assign.with(assign.getLhs(), renamedRhs);
                renamedNodes.put(instr, renamedAssign);
                block.codes.set(i, new Loc<>(renamedAssign));
            }
            // TODO: 处理其他类型的指令（如函数调用、返回等）
        }

        // 步骤3：为后继基本块的Phi函数填充参数
        Set<Integer> successors = originalCFG.getSucceed(blockId);
        if (successors != null) {
            for (int succId : successors) {
                BasicBlock<IRNode> succBlock = originalCFG.getBlock(succId);
                if (succBlock != null) {
                    fillPhiArguments(succBlock, blockId);
                }
            }
        }

        // 步骤4：递归处理支配树中的孩子节点
        List<Integer> children = domChildren.get(blockId);
        if (children != null) {
            for (int childId : children) {
                renameInBlock(childId, domChildren);
            }
        }

        // 步骤5：恢复变量栈（弹出在本块中压栈的版本）
        // 弹出Phi函数定义的变量
        for (SSAPhiFunction phi : blockPhis) {
            String varName = phi.result.split("_")[0];
            Stack<Integer> stack = varStacks.get(varName);
            if (stack != null && !stack.isEmpty()) {
                stack.pop();
            }
        }

        // 弹出普通指令定义的变量
        for (int i = blockPhis.size(); i < block.codes.size(); i++) {
            Loc<IRNode> loc = block.codes.get(i);
            IRNode instr = loc.getInstruction();
            if (instr instanceof Assign assign) {
                String varName = getVariableName(assign.getLhs());
                Stack<Integer> stack = varStacks.get(varName);
                if (stack != null && !stack.isEmpty()) {
                    stack.pop();
                }
            }
        }
    }

    /**
     * 为后继基本块的Phi函数填充参数
     */
    private void fillPhiArguments(BasicBlock<IRNode> succBlock, int predBlockId) {
        // 查找后继块开头的Phi函数
        for (Loc<IRNode> loc : succBlock.codes) {
            IRNode instr = loc.getInstruction();
            if (instr instanceof SSAPhiFunction phi) {
                // 获取变量名
                String varName = phi.result.split("_")[0];

                // 获取当前版本
                Integer currentVer = currentVersion.get(varName);
                if (currentVer != null) {
                    // 添加参数：变量名_版本
                    phi.arguments.add(varName + "_" + currentVer);
                }
            } else {
                // Phi函数都在基本块开头，遇到非Phi函数时停止
                break;
            }
        }
    }
    
    /**
     * 获取变量的下一个版本号
     */
    private int getNextVersion(String varName) {
        return versionMap.getOrDefault(varName, 0) + 1;
    }
    
    /**
     * 获取变量的当前版本号
     */
    private int getCurrentVersion(String varName, int blockId) {
        // 简化实现，返回当前版本
        return versionMap.getOrDefault(varName, 0);
    }
    
    /**
     * 从VarSlot提取变量名
     */
    private String getVariableName(VarSlot varSlot) {
        if (varSlot instanceof FrameSlot frameSlot) {
            String name = frameSlot.getVariableName();
            if (name != null) {
                return name;
            }
        }
        // 回退到toString()（适用于OperandSlot等）
        return varSlot.toString();
    }

    /**
     * 重命名操作数中的变量使用
     * 将变量引用替换为对应的版本号
     */
    private Operand renameOperand(Operand operand) {
        if (operand instanceof VarSlot varSlot) {
            String varName = getVariableName(varSlot);
            Integer currentVer = currentVersion.get(varName);
            if (currentVer != null) {
                // 创建新的SSARegister表示重命名后的变量
                return new SSARegister(varName + "_" + currentVer);
            }
        }
        // 对于非VarSlot的操作数（如常量），直接返回
        return operand;
    }

    /**
     * 获取基本块中定义的变量
     */
    private Set<String> getDefinedVariables(BasicBlock<IRNode> block) {
        Set<String> definedVars = new HashSet<>();

        for (Loc<IRNode> loc : block.codes) {
            IRNode instr = loc.getInstruction();
            if (instr instanceof Assign assign) {
                String varName = getVariableName(assign.getLhs());
                definedVars.add(varName);
            }
        }

        return definedVars;
    }
    
    /**
     * 生成SSA图的DOT表示
     */
    public String toDOT() {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph SSA {\n");
        dot.append("  ranksep=.25;\n");
        dot.append("  edge [arrowsize=.5]\n");
        dot.append("  node [shape=circle, fontname=\"ArialNarrow\",\n");
        dot.append("        fontsize=12, fixedsize=true, height=.45];\n");
        
        // 添加基本块节点
        dot.append("  ");
        for (BasicBlock<IRNode> block : originalCFG.nodes) {
            dot.append("BB").append(block.getId()).append("; ");
        }
        dot.append("\n");
        
        // 添加基本块标签和Φ函数信息
        for (BasicBlock<IRNode> block : originalCFG.nodes) {
            dot.append("  BB").append(block.getId()).append(" [label=\"");
            dot.append("BB").append(block.getId()).append("\\n");
            
            // 显示基本块中的指令，特别标注Φ函数
            for (Loc<IRNode> loc : block.codes) {
                IRNode instr = loc.getInstruction();
                if (instr instanceof SSAPhiFunction) {
                    dot.append("Φ: ").append(instr.toString()).append("\\n");
                } else {
                    dot.append(instr.toString()).append("\\n");
                }
            }
            dot.append("\"]\n");
        }
        
        // 添加边
        for (var edge : originalCFG.edges) {
            dot.append("  BB").append(edge.getLeft())
               .append(" -> BB").append(edge.getMiddle()).append(";\n");
        }
        
        dot.append("}\n");
        return dot.toString();
    }
    
    /**
     * 生成SSA图的Mermaid表示
     */
    public String toMermaid() {
        StringBuilder mermaid = new StringBuilder();
        mermaid.append("graph TD\n");
        
        for (BasicBlock<IRNode> block : originalCFG.nodes) {
            mermaid.append("  subgraph BB").append(block.getId()).append("\n");
            
            for (Loc<IRNode> loc : block.codes) {
                IRNode instr = loc.getInstruction();
                if (instr instanceof SSAPhiFunction) {
                    mermaid.append("    BB").append(block.getId()).append("Phi[\"Φ: ").append(instr).append("\"]\n");
                } else {
                    mermaid.append("    BB").append(block.getId()).append("Inst[\"").append(instr).append("\"]\n");
                }
            }
            
            mermaid.append("  end\n");
        }
        
        for (var edge : originalCFG.edges) {
            mermaid.append("  BB").append(edge.getLeft())
                   .append(" --> BB").append(edge.getMiddle()).append("\n");
        }
        
        return mermaid.toString();
    }
    
    /**
     * SSA Φ函数节点
     */
    private static class SSAPhiFunction extends IRNode {
        private String result;
        private List<String> arguments;
        
        public SSAPhiFunction(String result, List<String> arguments) {
            this.result = result;
            this.arguments = new ArrayList<>(arguments);
        }
        
        @Override
        public String toString() {
            return String.format("%s = φ(%s)", result, String.join(", ", arguments));
        }
    }
    
    /**
     * SSA寄存器（重命名后的变量）
     */
    private static class SSARegister extends Operand {
        private final String name;
        
        public SSARegister(String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
        
        @Override
        public <S,E> E accept(org.teachfx.antlr4.ep21.ir.IRVisitor<S,E> visitor) {
            // 简化实现，返回null
            return null;
        }
    }
}