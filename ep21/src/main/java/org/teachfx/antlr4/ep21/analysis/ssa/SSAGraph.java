package org.teachfx.antlr4.ep21.analysis.ssa;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.expr.VarSlot;
import org.teachfx.antlr4.ep21.ir.expr.addr.FrameSlot;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.ir.stmt.CJMP;
import org.teachfx.antlr4.ep21.pass.cfg.BasicBlock;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.pass.cfg.Loc;

import java.util.*;

/**
 * SSA图生成器 - 生成静态单赋值形式的控制流图
 * EP21 SSA扩展支持 (2025-12-23):
 * - TASK-3.2.5.1: CallFunc指令支持 (待分析 - 当前设计中CallFunc不直接包含变量引用)
 * - TASK-3.2.5.2: ReturnVal指令支持 ✅
 * - TASK-3.2.5.3: CJMP指令支持 ✅
 * - TASK-3.2.5.4: JMP指令支持 ✅
 * - TASK-3.2.5.5: 表达式重命名 - BinExpr/UnaryExpr在前端被转换为简单赋值，无需特殊处理 ✅
 * - TASK-3.2.5.6: SSA验证器 (待实现)
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
     * 支持的指令类型: Assign, CJMP, ReturnVal (TASK-3.2.5.2, TASK-3.2.5.3)
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
            // TASK-3.2.5.3: 处理CJMP指令 - 重命名条件变量
            else if (instr instanceof CJMP cjmp) {
                // 重命名条件变量（使用）
                Operand renamedCond = renameOperand(cjmp.cond);
                // 创建新的CJMP指令，保持跳转目标不变
                CJMP renamedCjmp = new CJMP(
                    (VarSlot)renamedCond,
                    cjmp.getThenBlock(),
                    cjmp.getElseBlock()
                );
                renamedNodes.put(instr, renamedCjmp);
                block.codes.set(i, new Loc<>(renamedCjmp));
            }
            // TASK-3.2.5.2: 处理ReturnVal指令 - 重命名返回值变量
            else if (instr instanceof org.teachfx.antlr4.ep21.ir.stmt.ReturnVal retVal) {
                // 重命名返回值变量（使用）
                VarSlot retValSlot = retVal.getRetVal();
                if (retValSlot != null) {
                    Operand renamedRetVal = renameOperand(retValSlot);
                    // 创建新的ReturnVal指令
                    // retFuncLabel是公共字段，直接访问其scope
                    org.teachfx.antlr4.ep21.ir.stmt.ReturnVal renamedRetValStmt =
                        new org.teachfx.antlr4.ep21.ir.stmt.ReturnVal(
                            (VarSlot)renamedRetVal,
                            retVal.retFuncLabel.getScope()
                        );
                    renamedRetValStmt.setMainEntry(retVal.isMainEntry());
                    renamedNodes.put(instr, renamedRetValStmt);
                    block.codes.set(i, new Loc<>(renamedRetValStmt));
                }
            }
            // TASK-3.2.5.4: JMP指令不需要特殊处理，跳过
            // 其他指令类型（Label等）不需要重命名，跳过
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
            // 只处理Assign指令，因为它定义了新变量
            if (instr instanceof Assign assign) {
                String varName = getVariableName(assign.getLhs());
                Stack<Integer> stack = varStacks.get(varName);
                if (stack != null && !stack.isEmpty()) {
                    stack.pop();
                }
            }
            // 注意：CJMP和ReturnVal只是使用变量，不定义新变量，所以不需要弹栈
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
     *
     * 只处理 Operand 类型（包括 VarSlot）
     * BinExpr 和 UnaryExpr 不会被分解，它们会在前端被转换为简单赋值
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
        // 对于非VarSlot的操作数（如常量）或其他类型，直接返回
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

    /**
     * SSA验证器 (TASK-3.2.5.6)
     * 验证SSA形式的正确性
     */
    public static class SSAValidator {

        /**
         * 验证SSA图
         */
        public ValidationResult validate(SSAGraph ssaGraph) {
            List<String> errors = new ArrayList<>();

            // 1. 验证变量版本一致性
            validateVariableConsistency(ssaGraph, errors);

            // 2. 验证Φ函数参数
            validatePhiFunctions(ssaGraph, errors);

            // 3. 验证变量使用在定义之后
            validateUseBeforeDef(ssaGraph, errors);

            return new ValidationResult(errors.isEmpty(), errors);
        }

        /**
         * 验证变量版本一致性
         * 每个变量定义应该只有一个版本号
         */
        private void validateVariableConsistency(SSAGraph ssaGraph, List<String> errors) {
            Map<String, Set<Integer>> varVersions = new HashMap<>();

            for (BasicBlock<IRNode> block : ssaGraph.originalCFG.nodes) {
                for (Loc<IRNode> loc : block.codes) {
                    IRNode instr = loc.getInstruction();

                    // 收集Phi函数定义的变量版本
                    if (instr instanceof SSAPhiFunction phi) {
                        String varName = extractVarName(phi.result);
                        Integer version = extractVersionNumber(phi.result);
                        if (varName != null && version != null) {
                            varVersions.computeIfAbsent(varName, k -> new HashSet<>()).add(version);
                        }
                    }
                    // 收集Assign定义的变量版本
                    else if (instr instanceof Assign assign) {
                        String varName = extractVarNameFromOperand(assign.getLhs());
                        // 检查rhs是否是SSARegister
                        if (assign.getRhs() instanceof SSARegister) {
                            Integer version = extractVersionNumber(assign.getRhs().toString());
                            if (version != null) {
                                varVersions.computeIfAbsent(varName, k -> new HashSet<>()).add(version);
                            }
                        }
                    }
                }
            }

            // 检查是否有缺失的版本号
            for (Map.Entry<String, Set<Integer>> entry : varVersions.entrySet()) {
                int maxVersion = getMaxVersion(entry.getValue());
                for (int v = 1; v <= maxVersion; v++) {
                    if (!entry.getValue().contains(v)) {
                        errors.add(String.format("变量 %s 缺少版本 %d", entry.getKey(), v));
                    }
                }
            }
        }

        /**
         * 验证Φ函数参数
         * Φ函数的参数数量应该与前驱块数量一致
         */
        private void validatePhiFunctions(SSAGraph ssaGraph, List<String> errors) {
            for (BasicBlock<IRNode> block : ssaGraph.originalCFG.nodes) {
                // 从 edges 列表中计算前驱块数量
                Set<Integer> predecessors = new HashSet<>();
                for (var edge : ssaGraph.originalCFG.edges) {
                    if (edge.getMiddle() == block.getId()) {
                        predecessors.add(edge.getLeft());
                    }
                }
                int predCount = predecessors.size();

                for (Loc<IRNode> loc : block.codes) {
                    IRNode instr = loc.getInstruction();
                    if (instr instanceof SSAPhiFunction phi) {
                        if (phi.arguments.size() != predCount) {
                            errors.add(String.format(
                                "基本块 %d 的Φ函数参数数量不匹配: 期望 %d (前驱数量), 实际 %d",
                                block.getId(), predCount, phi.arguments.size()
                            ));
                        }
                    } else {
                        // Phi函数都在开头，遇到非Phi函数时停止
                        break;
                    }
                }
            }
        }

        /**
         * 验证变量使用在定义之后
         */
        private void validateUseBeforeDef(SSAGraph ssaGraph, List<String> errors) {
            Map<String, Integer> definedVersions = new HashMap<>();

            for (BasicBlock<IRNode> block : ssaGraph.originalCFG.nodes) {
                for (Loc<IRNode> loc : block.codes) {
                    IRNode instr = loc.getInstruction();

                    // 处理Phi函数定义
                    if (instr instanceof SSAPhiFunction phi) {
                        String varName = extractVarName(phi.result);
                        Integer version = extractVersionNumber(phi.result);
                        if (varName != null && version != null) {
                            definedVersions.put(varName, version);
                        }

                        // 检查Phi函数参数是否已定义
                        for (String arg : phi.arguments) {
                            String argVarName = extractVarName(arg);
                            Integer argVersion = extractVersionNumber(arg);
                            if (argVarName != null && argVersion != null) {
                                Integer currentVersion = definedVersions.get(argVarName);
                                if (currentVersion == null || argVersion > currentVersion) {
                                    errors.add(String.format(
                                        "Phi函数使用了未定义的变量: %s (当前已定义版本: %s)",
                                        arg, currentVersion
                                    ));
                                }
                            }
                        }
                    }
                    // 处理Assign指令
                    else if (instr instanceof Assign assign) {
                        // 检查rhs使用的变量是否已定义
                        if (assign.getRhs() instanceof SSARegister rhsReg) {
                            String varName = extractVarName(rhsReg.toString());
                            Integer version = extractVersionNumber(rhsReg.toString());
                            if (varName != null && version != null) {
                                Integer currentVersion = definedVersions.get(varName);
                                if (currentVersion == null || version > currentVersion) {
                                    errors.add(String.format(
                                        "变量使用在定义之前: %s (当前已定义版本: %s)",
                                        rhsReg.toString(), currentVersion
                                    ));
                                }
                            }
                        }

                        // 记录新定义的变量
                        String varName = extractVarNameFromOperand(assign.getLhs());
                        if (assign.getRhs() instanceof SSARegister rhsReg) {
                            Integer version = extractVersionNumber(rhsReg.toString());
                            if (varName != null && version != null) {
                                definedVersions.put(varName, version);
                            }
                        }
                    }
                }
            }
        }

        /**
         * 从Operand提取变量名
         */
        private String extractVarNameFromOperand(Operand operand) {
            if (operand instanceof VarSlot varSlot) {
                if (varSlot instanceof FrameSlot frameSlot) {
                    String name = frameSlot.getVariableName();
                    if (name != null) {
                        return name;
                    }
                }
                return varSlot.toString();
            }
            return operand.toString();
        }

        /**
         * 从SSA变量名中提取变量名
         * 例如: "x_1" -> "x"
         */
        private String extractVarName(String ssaVarName) {
            int underscoreIndex = ssaVarName.lastIndexOf('_');
            if (underscoreIndex > 0) {
                return ssaVarName.substring(0, underscoreIndex);
            }
            return null;
        }

        /**
         * 从SSA变量名中提取版本号
         * 例如: "x_1" -> 1
         */
        private Integer extractVersionNumber(String ssaVarName) {
            int underscoreIndex = ssaVarName.lastIndexOf('_');
            if (underscoreIndex > 0 && underscoreIndex < ssaVarName.length() - 1) {
                try {
                    return Integer.parseInt(ssaVarName.substring(underscoreIndex + 1));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }

        /**
         * 获取版本号集合中的最大值
         */
        private int getMaxVersion(Set<Integer> versions) {
            return versions.stream().max(Integer::compareTo).orElse(0);
        }
    }

    /**
     * SSA验证结果 (TASK-3.2.5.6)
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = new ArrayList<>(errors);
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return new ArrayList<>(errors);
        }

        public String getSummary() {
            if (valid) {
                return "SSA验证通过";
            } else {
                return String.format("SSA验证失败: %d 个错误", errors.size());
            }
        }
    }
}