package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.utils.Kind;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 块操作器 - 提供CFG块的创建、分裂、删除、边操作
 */
public class BlockManipulator<I extends IRNode> {

    private static final Logger logger = LogManager.getLogger(BlockManipulator.class);
    
    private final CFG<I> cfg;
    private int nextBlockId;
    
    public BlockManipulator(@NotNull CFG<I> cfg) {
        this.cfg = cfg;
        this.nextBlockId = cfg.nodes.stream()
            .mapToInt(BasicBlock::getId)
            .max()
            .orElse(0) + 1;
    }

    /**
     * 分裂基本块
     * @param block 要分裂的块
     * @param instructionIndex 分裂点索引
     * @return 新创建的块
     */
    public BasicBlock<I> splitBlock(@NotNull BasicBlock<I> block, int instructionIndex) {
        if (instructionIndex < 0 || instructionIndex >= block.codes.size()) {
            throw new IllegalArgumentException(
                "Invalid split index: " + instructionIndex + ", block size: " + block.codes.size());
        }
        
        logger.debug("Splitting block L{} at index {}", block.getId(), instructionIndex);
        
        BasicBlock<I> newBlock = createBlock();
        
        List<Loc<I>> tailInstructions = block.codes.subList(instructionIndex, block.codes.size());
        newBlock.codes.addAll(tailInstructions);
        tailInstructions.clear();
        
        Set<Integer> successors = cfg.getSucceed(block.getId());
        for (int succId : successors) {
            redirectEdge(block.getId(), newBlock.getId(), succId);
        }
        
        addEdge(block, newBlock);
        
        logger.info("Successfully split block L{} -> L{} (new L{})", 
            block.getId(), newBlock.getId(), newBlock.getId());
        
        return newBlock;
    }

    /**
     * 创建预头部块
     * @param loopHeader 循环头块
     * @return 预头部块
     */
    public BasicBlock<I> createPreheader(@NotNull BasicBlock<I> loopHeader) {
        logger.debug("Creating preheader for loop header L{}", loopHeader.getId());
        
        BasicBlock<I> preheader = createBlock();
        preheader.setLabel(new Label("preheader_" + loopHeader.getId(), null));
        
        Set<Integer> predecessors = cfg.getFrontier(loopHeader.getId());
        
        if (predecessors.isEmpty()) {
            logger.warn("Loop header L{} has no predecessors, cannot create preheader", loopHeader.getId());
            return null;
        }
        
        logger.debug("Loop header L{} has {} predecessors", loopHeader.getId(), predecessors.size());
        
        for (int predId : predecessors) {
            BasicBlock<I> pred = cfg.getBlock(predId);
            if (pred != null) {
                removeEdge(pred, loopHeader);
                addEdge(pred, preheader);
            }
        }
        
        addEdge(preheader, loopHeader);
        
        logger.info("Successfully created preheader L{} for L{}", preheader.getId(), loopHeader.getId());
        
        return preheader;
    }

    /**
     * 确保循环头有预头部
     */
    public BasicBlock<I> ensurePreheader(@NotNull BasicBlock<I> loopHeader) {
        Set<Integer> predecessors = cfg.getFrontier(loopHeader.getId());
        
        if (predecessors.size() == 1) {
            int predId = predecessors.iterator().next();
            BasicBlock<I> pred = cfg.getBlock(predId);
            if (pred != null && pred.getLabel().getRawLabel().startsWith("preheader_")) {
                logger.debug("Loop header L{} already has preheader L{}", loopHeader.getId(), predId);
                return pred;
            }
        }
        
        return createPreheader(loopHeader);
    }

    /**
     * 添加边
     */
    public boolean addEdge(@NotNull BasicBlock<I> from, @NotNull BasicBlock<I> to) {
        for (var edge : cfg.edges) {
            if (edge.getLeft() == from.getId() && edge.getMiddle() == to.getId()) {
                logger.debug("Edge L{} -> L{} already exists", from.getId(), to.getId());
                return false;
            }
        }
        
        Triple<Integer, Integer, Integer> newEdge = Triple.of(from.getId(), to.getId(), 0);
        cfg.edges.add(newEdge);
        
        cfg.ensureLinkExists(from.getId());
        cfg.ensureLinkExists(to.getId());
        cfg.getLinks().get(from.getId()).getRight().add(to.getId());
        cfg.getLinks().get(to.getId()).getLeft().add(from.getId());
        
        logger.debug("Added edge L{} -> L{}", from.getId(), to.getId());
        return true;
    }

    /**
     * 移除边
     */
    public boolean removeEdge(@NotNull BasicBlock<I> from, @NotNull BasicBlock<I> to) {
        Triple<Integer, Integer, Integer> edgeToRemove = null;
        
        for (var edge : cfg.edges) {
            if (edge.getLeft() == from.getId() && edge.getMiddle() == to.getId()) {
                edgeToRemove = edge;
                break;
            }
        }
        
        if (edgeToRemove == null) {
            logger.warn("Edge L{} -> L{} does not exist", from.getId(), to.getId());
            return false;
        }
        
        cfg.edges.remove(edgeToRemove);
        
        if (from.getId() < cfg.getLinks().size() && to.getId() < cfg.getLinks().size()) {
            cfg.getLinks().get(from.getId()).getRight().remove(to.getId());
            cfg.getLinks().get(to.getId()).getLeft().remove(from.getId());
        }
        
        logger.debug("Removed edge L{} -> L{}", from.getId(), to.getId());
        return true;
    }

    /**
     * 重定向边的目标
     */
    public int redirectEdge(int from, int oldTarget, int newTarget) {
        int count = 0;
        
        for (int i = 0; i < cfg.edges.size(); i++) {
            var edge = cfg.edges.get(i);
            if (edge.getLeft() == from && edge.getMiddle() == oldTarget) {
                Triple<Integer, Integer, Integer> newEdge = Triple.of(from, newTarget, edge.getRight());
                cfg.edges.set(i, newEdge);
                
                if (oldTarget < cfg.getLinks().size()) {
                    cfg.getLinks().get(oldTarget).getLeft().remove(from);
                }
                cfg.ensureLinkExists(newTarget);
                cfg.getLinks().get(newTarget).getLeft().add(from);
                
                count++;
                logger.debug("Redirected edge L{} -> L{} to L{} -> L{}", from, oldTarget, from, newTarget);
            }
        }
        
        return count;
    }

    /**
     * 移除块的所有出边
     */
    public int removeAllOutgoingEdges(@NotNull BasicBlock<I> block) {
        Set<Integer> successors = new HashSet<>(cfg.getSucceed(block.getId()));
        int count = 0;
        
        for (int succId : successors) {
            BasicBlock<I> succ = cfg.getBlock(succId);
            if (succ != null && removeEdge(block, succ)) {
                count++;
            }
        }
        
        return count;
    }

    /**
     * 移动块内容到另一块末尾
     */
    public void moveBlockContentsToEnd(@NotNull BasicBlock<I> source, @NotNull BasicBlock<I> target) {
        List<Loc<I>> instructions = new ArrayList<>(source.codes);
        source.codes.clear();
        target.codes.addAll(instructions);
        
        logger.debug("Moved {} instructions from L{} to end of L{}", 
            instructions.size(), source.getId(), target.getId());
    }

    /**
     * 创建新的基本块
     */
    public BasicBlock<I> createBlock() {
        int id = nextBlockId++;
        BasicBlock<I> block = new BasicBlock<>(
            Kind.CONTINUOUS,
            new ArrayList<>(),
            new Label("L" + id, null),
            id
        );
        cfg.nodes.add(block);
        cfg.ensureLinkExists(id);
        
        return block;
    }

    /**
     * 创建新的基本块，带有指定标签
     */
    public BasicBlock<I> createBlock(@NotNull String labelStr) {
        int id = nextBlockId++;
        BasicBlock<I> block = new BasicBlock<>(
            Kind.CONTINUOUS,
            new ArrayList<>(),
            new Label(labelStr, null),
            id
        );
        cfg.nodes.add(block);
        cfg.ensureLinkExists(id);
        
        return block;
    }

    /**
     * 删除块
     */
    public boolean removeBlock(@NotNull BasicBlock<I> block) {
        removeAllOutgoingEdges(block);
        
        Set<Integer> predecessors = cfg.getFrontier(block.getId());
        for (int predId : predecessors) {
            BasicBlock<I> pred = cfg.getBlock(predId);
            if (pred != null) {
                removeEdge(pred, block);
            }
        }
        
        boolean removed = cfg.nodes.remove(block);
        
        if (removed) {
            logger.debug("Removed block L{}", block.getId());
        }
        
        return removed;
    }

    /**
     * 复制块
     */
    public BasicBlock<I> duplicateBlock(@NotNull BasicBlock<I> block, @NotNull String newLabel) {
        int id = nextBlockId++;
        
        List<Loc<I>> copiedCodes = new ArrayList<>();
        for (Loc<I> loc : block.codes) {
            copiedCodes.add(new Loc<>(loc.instr));
        }
        
        BasicBlock<I> newBlock = new BasicBlock<>(
            block.kind,
            copiedCodes,
            new Label(newLabel, null),
            id
        );
        
        cfg.nodes.add(newBlock);
        cfg.ensureLinkExists(id);
        
        logger.debug("Duplicated block L{} as L{}", block.getId(), id);
        
        return newBlock;
    }

    public int getNextBlockId() {
        return nextBlockId;
    }

    public void setNextBlockId(int id) {
        this.nextBlockId = id;
    }

    /**
     * 验证CFG完整性
     */
    public ValidationResult validateCFG() {
        ValidationResult result = new ValidationResult();
        List<Pair<Set<Integer>, Set<Integer>>> links = cfg.getLinks();
        
        for (var edge : cfg.edges) {
            int from = edge.getLeft();
            int to = edge.getMiddle();
            
            if (cfg.getBlock(from) == null) {
                result.addError("Edge references non-existent node: " + from);
            }
            if (cfg.getBlock(to) == null) {
                result.addError("Edge references non-existent node: " + to);
            }
            
            if (from < links.size() && !links.get(from).getRight().contains(to)) {
                result.addError("Edge " + from + " -> " + to + " in edges but not in links");
            }
            if (to < links.size() && !links.get(to).getLeft().contains(from)) {
                result.addError("Edge " + from + " -> " + to + " in edges but not in reverse links");
            }
        }
        
        for (int i = 0; i < links.size(); i++) {
            for (int succ : links.get(i).getRight()) {
                if (cfg.getBlock(succ) == null) {
                    result.addError("Links reference non-existent successor: " + succ);
                }
            }
            for (int pred : links.get(i).getLeft()) {
                if (cfg.getBlock(pred) == null) {
                    result.addError("Links reference non-existent predecessor: " + pred);
                }
            }
        }
        
        logger.info("CFG validation complete: {} errors, {} warnings", 
            result.getErrors().size(), result.getWarnings().size());
        
        return result;
    }

    public static class ValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public List<String> getErrors() {
            return Collections.unmodifiableList(errors);
        }
        
        public List<String> getWarnings() {
            return Collections.unmodifiableList(warnings);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public void logResults() {
            if (isValid()) {
                logger.info("CFG validation passed");
            } else {
                logger.error("CFG validation failed: {} errors", errors.size());
                for (String error : errors) {
                    logger.error("  - {}", error);
                }
            }
            if (!warnings.isEmpty()) {
                logger.warn("CFG validation warnings: {}", warnings.size());
                for (String warning : warnings) {
                    logger.warn("  - {}", warning);
                }
            }
        }
    }
}
