package org.teachfx.antlr4.ep21.analysis.ssa;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
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
    
    public SSAGraph(CFG<IRNode> cfg) {
        this.originalCFG = cfg;
        this.versionMap = new HashMap<>();
        this.renamedNodes = new HashMap<>();
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
     * 插入Φ函数到汇聚节点
     */
    private void insertPhiFunctions() {
        // 对于每个基本块的每个变量，如果它有多个前驱，需要插入Φ函数
        
        for (BasicBlock<IRNode> block : originalCFG.nodes) {
            // 计算这个块中定义的变量
            Set<String> definedVars = getDefinedVariables(block);
            
            // 如果这个块有多个前驱，且定义了某些变量，需要插入Φ函数
            if (originalCFG.getFrontier(block.getId()).size() > 1) {
                for (String var : definedVars) {
                    insertPhiFunction(block, var);
                }
            }
        }
    }
    
    /**
     * 在基本块开头插入Φ函数
     */
    private void insertPhiFunction(BasicBlock<IRNode> block, String varName) {
        // 获取当前变量的版本
        String phiVar = varName + "_" + getNextVersion(varName);
        
        // 创建Φ函数的参数列表（从前驱块获取最新版本）
        List<String> phiArgs = new ArrayList<>();
        for (Integer predId : originalCFG.getFrontier(block.getId())) {
            String predVar = varName + "_" + getCurrentVersion(varName, predId);
            phiArgs.add(predVar);
        }
        
        // 创建Φ函数节点（这里简化为一个特殊的Assign）
        SSAPhiFunction phiFunction = new SSAPhiFunction(phiVar, phiArgs);
        
        // 在基本块开头插入Φ函数
        if (!block.codes.isEmpty()) {
            Loc<IRNode> phiLoc = new Loc<>(phiFunction);
            block.codes.add(0, phiLoc);
        }
    }
    
    /**
     * 变量重命名
     */
    private void renameVariables() {
        for (BasicBlock<IRNode> block : originalCFG.nodes) {
            renameInBlock(block);
        }
    }
    
    /**
     * 重命名基本块中的变量
     */
    private void renameInBlock(BasicBlock<IRNode> block) {
        for (int i = 0; i < block.codes.size(); i++) {
            Loc<IRNode> loc = block.codes.get(i);
            IRNode instr = loc.getInstruction();
            
            if (instr instanceof Assign assign) {
                // 重命名目标变量
                String originalVar = assign.getLhs().toString();
                String newVar = originalVar + "_" + getNextVersion(originalVar);
                
                // 创建新的SSARegister作为目标变量
                SSARegister newOperand = new SSARegister(newVar);
                IRNode renamedAssign = Assign.with(assign.getLhs(), assign.getRhs());
                renamedNodes.put(instr, renamedAssign);
                
                // 更新Loc中的指令
                block.codes.set(i, new Loc<>(renamedAssign));
            }
            
            // TODO: 处理其他类型的指令
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
     * 获取基本块中定义的变量
     */
    private Set<String> getDefinedVariables(BasicBlock<IRNode> block) {
        Set<String> definedVars = new HashSet<>();
        
        for (Loc<IRNode> loc : block.codes) {
            IRNode instr = loc.getInstruction();
            if (instr instanceof Assign assign) {
                String varName = assign.getLhs().toString();
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
        private final String result;
        private final List<String> arguments;
        
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