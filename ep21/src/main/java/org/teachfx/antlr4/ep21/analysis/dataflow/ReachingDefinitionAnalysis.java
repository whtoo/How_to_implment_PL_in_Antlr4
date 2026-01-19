package org.teachfx.antlr4.ep21.analysis.dataflow;

import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.stmt.Assign;
import org.teachfx.antlr4.ep21.pass.cfg.CFG;
import org.teachfx.antlr4.ep21.ir.expr.CallFunc;
import java.util.*;

import java.util.*;

/**
 * 到达定义分析（Reaching Definitions Analysis）
 *
 * <p>这是一个前向分析，数据流信息类型为 {@code Set<Definition>}，其中每个Definition
 * 包含一个变量、定义所在的基本块和指令索引。这样可以精确区分不同位置的
 * 对同一变量的定义。
 *
 * <p>算法概述：
 * <ul>
 *   <li>GEN集合：基本块中产生的新定义</li>
 *   <li>KILL集合：基本块中杀死的老定义（对同一变量的其他定义）</li>
 *   <li>传递函数：out = gen ∪ (in - kill)</li>
 * </ul>
 *
 * <p>示例：
 * <pre>
 *   x = 1;   // 定义d1: Definition(x, B0, 0)
 *   if (cond) {
 *     x = 2; // 定义d2: Definition(x, B1, 0)
 *   }
 *   y = x + 1; // 到达定义: {d1, d2}，而不是简单的{x}
 * </pre>
 *
 * <p>函数调用处理：
 * <ul>
 *   <li>简化实现：假设所有全局变量都可能被修改</li>
 *   <li>简化实现：假设所有参数都可能被修改（指针/引用）</li>
 *   <li>完整的实现需要：全局变量分析、指针别名分析、函数副作用分析</li>
 * </ul>
 *
 * @author EP21 Team
 * @version 3.0 (2026-01-18) - 移除不必要的泛型设计
 * @since 2026-01-18
 */
public class ReachingDefinitionAnalysis extends AbstractDataFlowAnalysis<Set<Definition>, IRNode> {

    /** 预先收集的所有定义，按变量分组 */
    private Map<Operand, Set<Definition>> variableDefinitions;

    /**
     * 构造函数
     *
     * @param cfg 控制流图
     */
    public ReachingDefinitionAnalysis(CFG<IRNode> cfg) {
        super(cfg);
        // 预先收集所有定义，用于计算kill集合
        collectAllDefinitions();
    }

    /**
     * 收集所有定义，按变量分组
     *
     * 遍历所有基本块和指令，收集所有赋值语句的定义
     */
    private void collectAllDefinitions() {
        variableDefinitions = new HashMap<>();

        for (var block : getCFG()) {
            int instructionIndex = 0;
            for (var loc : block.codes) {
                IRNode instr = loc.getInstruction();

                if (instr instanceof Assign assign) {
                    Operand var = assign.getLhs();
                    Definition def = new Definition(var, block, instructionIndex, instr);

                    // 按变量分组
                    variableDefinitions.computeIfAbsent(var, k -> new HashSet<>()).add(def);
                }

                instructionIndex++;
            }
        }
    }

    @Override
    public boolean isForward() {
        return true; // 到达定义分析是前向分析
    }

    @Override
    public Set<Definition> meet(Set<Definition> a, Set<Definition> b) {
        // 到达定义分析使用并集操作
        Set<Definition> result = new HashSet<>(a);
        result.addAll(b);
        return result;
    }

    @Override
    public Set<Definition> transfer(IRNode instr, Set<Definition> input) {
        // 到达定义分析的传递函数:
        // out = gen ∪ (in - kill)

        Set<Definition> gen = computeGen(instr);
        Set<Definition> kill = computeKill(instr);

        // out = (input - kill) ∪ gen
        Set<Definition> result = new HashSet<>(input);
        result.removeAll(kill);
        result.addAll(gen);

        return result;
    }

    @Override
    public Set<Definition> getInitialValue() {
        // 初始值为空集合
        return new HashSet<>();
    }

    /**
     * 计算gen集合（新产生的定义）
     *
     * @param instr 当前指令
     * @return 该指令产生的新定义集合
     */
    private Set<Definition> computeGen(IRNode instr) {
        Set<Definition> gen = new HashSet<>();

        if (instr instanceof Assign assign) {
            // 左边的目标变量是一个新定义
            Operand var = assign.getLhs();

            // 从收集的定义中找到对应的Definition
            if (variableDefinitions.containsKey(var)) {
                for (Definition def : variableDefinitions.get(var)) {
                    // 找到与当前指令匹配的Definition
                    // 注意：这里简化处理，假设每个赋值只对应一个Definition
                    if (def.getInstruction() == instr) {
                        gen.add(def);
                        break;
                    }
                }
            }
        }

        return gen;
    }

    /**
     * 计算kill集合（被杀死的老定义）
     *
     * @param instr 当前指令
     * @return 被杀死的定义集合
     */
    private Set<Definition> computeKill(IRNode instr) {
        Set<Definition> kill = new HashSet<>();

        if (instr instanceof Assign assign) {
            // 对变量x的新定义会杀死所有对x的其他老定义
            Operand var = assign.getLhs();

            if (variableDefinitions.containsKey(var)) {
                for (Definition def : variableDefinitions.get(var)) {
                    // 排除当前定义（当前定义在gen集合中）
                    if (def.getInstruction() != instr) {
                        kill.add(def);
                    }
                }
            }
        } else if (instr instanceof CallFunc call) {
            // 函数调用：杀死所有可能有副作用的变量的定义
            kill.addAll(getPotentiallyModifiedDefinitions(call));
        }

        return kill;
    }

    /**
     * 获取函数调用可能修改的变量定义
     *
     * <p>简化实现：
     * <ul>
     *   <li>假设所有全局变量都可能被修改</li>
     *   <li>假设所有参数都可能被修改（指针/引用）</li>
     * </ul>
     *
     * <p>完整的实现需要：
     * <ul>
     *   <li>完整的全局变量分析</li>
     *   <li>指针别名分析</li>
     *   <li>函数副作用分析</li>
     * </ul>
     *
     * @param call 函数调用指令
     * @return 可能被修改的变量定义集合
     */
    private Set<Definition> getPotentiallyModifiedDefinitions(CallFunc call) {
        Set<Definition> modified = new HashSet<>();

        // 1. 全局变量：假设所有全局变量都可能被修改
        modified.addAll(getGlobalVariableDefinitions());

        // 2. 通过指针/引用传递的参数
        // 注意：简化实现，假设所有参数都可能被修改
        // 完整实现需要类型系统判断（如*、&、[]等）
        // TODO: 完整实现需要遍历参数列表
        // 当前简化处理：只处理函数调用本身，不展开参数
        // for (Operand arg : call.getArgs()) {
        //     if (variableDefinitions.containsKey(arg)) {
        //         modified.addAll(variableDefinitions.get(arg));
        //     }
        // }

        return modified;
    }

    /**
     * 获取所有全局变量的定义
     *
     * <p>简化实现：假设基本块0中的所有定义都是全局变量
     *
     * <p>完整的实现需要符号表区分全局和局部作用域
     *
     * @return 所有全局变量定义的集合
     */
    private Set<Definition> getGlobalVariableDefinitions() {
        Set<Definition> globalDefs = new HashSet<>();

        // 简化实现：假设基本块0中的所有定义都是全局变量
        // 完整实现需要符号表区分全局和局部作用域
        var entryBlock = getCFG().getBlock(0);
        if (entryBlock != null) {
            int instructionIndex = 0;
            for (var loc : entryBlock.codes) {
                IRNode instr = loc.getInstruction();
                if (instr instanceof Assign assign) {
                    Operand var = assign.getLhs();
                    Definition def = new Definition(var, entryBlock, instructionIndex, instr);
                    globalDefs.add(def);
                }
                instructionIndex++;
            }
        }

        return globalDefs;
    }

    /**
     * 获取分析结果的字符串表示
     *
     * @return 格式化的分析结果
     */
    public String getResultString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== 到达定义分析结果 ===\n");

        for (var block : getCFG()) {
            int blockId = block.getId();
            sb.append("基本块 ").append(blockId).append(":\n");
            sb.append("  In: ").append(formatDefinitionSet(getIn(blockId))).append("\n");
            sb.append("  Out: ").append(formatDefinitionSet(getOut(blockId))).append("\n");

            sb.append("  指令序列:\n");
            for (var loc : block.codes) {
                IRNode instr = loc.getInstruction();
                sb.append("    ").append(instr.toString())
                  .append(" [In: ").append(formatDefinitionSet(getIn(instr)))
                  .append(", Out: ").append(formatDefinitionSet(getOut(instr))).append("]\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * 格式化定义集合
     *
     * @param definitions 定义集合
     * @return 格式化字符串
     */
    private String formatDefinitionSet(Set<Definition> definitions) {
        return DefinitionSets.format(definitions);
    }

    /**
     * 打印分析结果到标准输出
     */
    public void printResult() {
        System.out.println(getResultString());
    }

    /**
     * 获取所有收集到的定义
     *
     * @return 所有定义的集合
     */
    public Set<Definition> getAllDefinitions() {
        Set<Definition> allDefs = new HashSet<>();
        for (Set<Definition> defs : variableDefinitions.values()) {
            allDefs.addAll(defs);
        }
        return allDefs;
    }

    /**
     * 获取特定变量的所有定义
     *
     * @param var 目标变量
     * @return 该变量的所有定义
     */
    public Set<Definition> getDefinitionsOfVariable(Operand var) {
        return variableDefinitions.getOrDefault(var, Collections.emptySet());
    }
}
