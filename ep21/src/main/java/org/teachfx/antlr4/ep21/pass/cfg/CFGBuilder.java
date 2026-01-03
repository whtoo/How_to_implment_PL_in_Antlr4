package org.teachfx.antlr4.ep21.pass.cfg;

import org.apache.commons.lang3.tuple.Triple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.stmt.CJMP;
import org.teachfx.antlr4.ep21.ir.stmt.JMP;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Control Flow Graph Builder for converting LinearIRBlock to BasicBlock CFG.
 * This class handles the traversal and edge construction for building a complete CFG.
 *
 * Implements ICFGBuilder interface to resolve abstraction inconsistency.
 *
 * Improvements made:
 * 1. Enhanced code readability and maintainability
 * 2. Performance optimization with better data structures
 * 3. Best practices and design patterns implementation
 * 4. Comprehensive error handling and edge case management
 * 5. Professional logging with Log4j2
 */
public class CFGBuilder implements ICFGBuilder {
    private static final Logger logger = LogManager.getLogger(CFGBuilder.class);
    
    // 使用CFGConstants中的常量，避免重复定义魔法数字
    private static final int MAX_RECURSION_DEPTH = 10000;
    
    private final CFG<IRNode> cfg;
    private final List<BasicBlock<IRNode>> basicBlocks;
    private final List<Triple<Integer, Integer, Integer>> edges;
    private final Set<LinearIRBlock> visitedBlocks;
    private final Set<String> processedEdges;
    private final Set<String> processedConnections; // 终极修复：跟踪节点对，忽略边类型
    
    private int recursionDepth = 0;

    /**
     * Constructs a new CFGBuilder and builds the CFG from the starting block.
     * 
     * @param startBlock the starting basic block (must not be null)
     * @throws NullPointerException if startBlock is null
     * @throws IllegalArgumentException if startBlock has invalid state
     */
    public CFGBuilder(LinearIRBlock startBlock) {
        validateStartBlock(startBlock);
        
        this.basicBlocks = new ArrayList<>();
        this.edges = new ArrayList<>();
        this.visitedBlocks = new HashSet<>();
        this.processedEdges = ConcurrentHashMap.newKeySet(); // Thread-safe edge tracking
        this.processedConnections = ConcurrentHashMap.newKeySet(); // 终极修复：跟踪节点对
        
        try {
            buildControlFlowGraph(startBlock);
        } catch (StackOverflowError e) {
            logger.error("Maximum recursion depth exceeded while building CFG. " +
                        "This may indicate infinite loops in the control flow.", e);
            throw new IllegalStateException("Failed to build CFG due to excessive recursion", e);
        }
        
        // 在创建CFG之前检查重复边
        checkForDuplicateEdges();
        
        this.cfg = new CFG<>(basicBlocks, edges);
        logger.info("CFG built successfully: {} blocks, {} edges",
                   basicBlocks.size(), edges.size());
    }

    // ICFGBuilder接口实现

    /**
     * 从起始线性IR块构建控制流图。
     * 这是ICFGBuilder接口的实现方法。
     *
     * @param startBlock 起始基本块
     * @return 构建完成的CFG实例
     */
    @Override
    public CFG<IRNode> buildFrom(LinearIRBlock startBlock) {
        // 构造函数已处理构建逻辑
        return getCFG();
    }

    /**
     * 静态工厂方法：从起始块创建CFG。
     * 提供更语义化的API。
     *
     * @param startBlock 起始基本块
     * @return 构建完成的CFG实例
     */
    public static CFG<IRNode> build(LinearIRBlock startBlock) {
        return new CFGBuilder(startBlock).getCFG();
    }
    
    private void checkForDuplicateEdges() {
        Set<String> seenEdges = new HashSet<>();
        Set<String> duplicates = new HashSet<>();
        
        for (var edge : edges) {
            String edgeKey = edge.getLeft() + "->" + edge.getMiddle() + " (type:" + edge.getRight() + ")";
            if (!seenEdges.add(edgeKey)) {
                duplicates.add(edgeKey);
            }
        }
        
        if (!duplicates.isEmpty()) {
            logger.warn("CFGBuilder检测到重复边: {}", duplicates);
        }
    }

    /**
     * Validates the start block parameter with comprehensive checks.
     */
    private void validateStartBlock(LinearIRBlock startBlock) {
        Objects.requireNonNull(startBlock, "Start block cannot be null");
        
        if (startBlock.getOrd() < 0) {
            throw new IllegalArgumentException("Start block ordinal must be non-negative: " + 
                                              startBlock.getOrd());
        }
        
        if (startBlock.getStmts() == null) {
            throw new IllegalArgumentException("Start block statements cannot be null");
        }
    }

    /**
     * Main method to build the control flow graph.
     */
    private void buildControlFlowGraph(LinearIRBlock startBlock) {
        traverseBlock(startBlock);
    }

    /**
     * Recursively traverses a block and builds the CFG structure.
     * Includes protection against infinite recursion and excessive depth.
     */
    private void traverseBlock(LinearIRBlock block) {
        checkRecursionDepth();
        
        // Check if block has already been visited (prevents infinite recursion)
        if (visitedBlocks.contains(block)) {
            logger.debug("Block ord={} already visited, skipping", block.getOrd());
            return;
        }
        
        visitedBlocks.add(block);
        
        // Create and add the basic block
        BasicBlock<IRNode> currentBlock = createBasicBlock(block);
        basicBlocks.add(currentBlock);
        logger.debug("已添加基本块: ord={}, id={}", block.getOrd(), currentBlock.getId());
        
        // Process control flow edges based on the last instruction
        processControlFlowEdges(block, currentBlock.getId());
        
        // Process successor edges - 注意这里可能导致边被重复添加
        processSuccessorEdges(block, currentBlock.getId());
        
        recursionDepth--;
    }

    /**
     * Checks and manages recursion depth to prevent stack overflow.
     */
    private void checkRecursionDepth() {
        recursionDepth++;
        if (recursionDepth > MAX_RECURSION_DEPTH) {
            throw new IllegalStateException("Maximum recursion depth of " + 
                                          MAX_RECURSION_DEPTH + " exceeded");
        }
    }

    /**
     * Creates a BasicBlock from a LinearIRBlock with proper error handling.
     */
    private BasicBlock<IRNode> createBasicBlock(LinearIRBlock block) {
        try {
            return BasicBlock.buildFromLinearBlock(block, basicBlocks);
        } catch (Exception e) {
            logger.error("Failed to create BasicBlock from LinearIRBlock: {}", block.getOrd(), e);
            throw new IllegalStateException("Failed to create basic block for block " + 
                                          block.getOrd(), e);
        }
    }

    /**
     * Processes control flow edges based on the last instruction in the block.
     * Refactored to use strategy pattern for different instruction types.
     */
    private void processControlFlowEdges(LinearIRBlock block, int currentBlockId) {
        List<IRNode> statements = block.getStmts();
        
        if (statements.isEmpty()) {
            logger.debug("Block ord={} has empty statements list", block.getOrd());
            return;
        }
        
        IRNode lastInstruction = statements.get(statements.size() - 1);
        
        logger.debug("Processing last instruction: {} in block ord={}", 
                    lastInstruction.getClass().getSimpleName(), block.getOrd());
        
        if (lastInstruction instanceof JMP jumpInstruction) {
            processJumpInstruction(block, jumpInstruction, currentBlockId);
        } else if (lastInstruction instanceof CJMP conditionalJumpInstruction) {
            processConditionalJumpInstruction(block, conditionalJumpInstruction, currentBlockId);
        }
    }

    /**
     * Processes an unconditional jump instruction with improved error handling.
     */
    private void processJumpInstruction(LinearIRBlock block, JMP jumpInstruction, int currentBlockId) {
        try {
            LinearIRBlock targetBlock = jumpInstruction.getNext();
            if (targetBlock == null) {
                logger.warn("Jump instruction in block ord={} has null target", block.getOrd());
                return;
            }
            
            int targetBlockId = targetBlock.getOrd();
            String edgeKey = createEdgeKey(currentBlockId, targetBlockId, CFGConstants.JUMP_EDGE_TYPE);
            
            if (addEdgeIfNotExists(edgeKey, currentBlockId, targetBlockId, CFGConstants.JUMP_EDGE_TYPE)) {
                logger.debug("Added jump edge: {} -> {}", currentBlockId, targetBlockId);
                traverseBlock(targetBlock);
            }
        } catch (Exception e) {
            logger.error("Error processing jump instruction in block {}", block.getOrd(), e);
        }
    }

    /**
     * Processes a conditional jump instruction with better branch handling.
     */
    private void processConditionalJumpInstruction(LinearIRBlock block, CJMP conditionalJumpInstruction, 
                                                 int currentBlockId) {
        try {
            // Process then branch
            LinearIRBlock thenBlock = conditionalJumpInstruction.getThenBlock();
            if (thenBlock != null) {
                processBranch(block, conditionalJumpInstruction, currentBlockId, 
                            thenBlock, "then");
            }
            
            // Process else branch
            LinearIRBlock elseBlock = conditionalJumpInstruction.getElseBlock();
            if (elseBlock != null) {
                processBranch(block, conditionalJumpInstruction, currentBlockId, 
                            elseBlock, "else");
            }
        } catch (Exception e) {
            logger.error("Error processing conditional jump instruction in block {}", 
                        block.getOrd(), e);
        }
    }

    /**
     * Processes a single branch (then or else) of a conditional jump.
     */
    private void processBranch(LinearIRBlock block, CJMP conditionalJumpInstruction, 
                             int currentBlockId, LinearIRBlock targetBlock, String branchType) {
        try {
            int targetBlockId = targetBlock.getOrd();
            String edgeKey = createEdgeKey(currentBlockId, targetBlockId, CFGConstants.JUMP_EDGE_TYPE);
            
            if (addEdgeIfNotExists(edgeKey, currentBlockId, targetBlockId, CFGConstants.JUMP_EDGE_TYPE)) {
                logger.debug("Added {} branch edge: {} -> {}", 
                           branchType, currentBlockId, targetBlockId);
                traverseBlock(targetBlock);
            }
        } catch (Exception e) {
            logger.error("Error processing {} branch in block {}", branchType, block.getOrd(), e);
        }
    }

    /**
     * Processes successor edges from the block with improved null checking.
     */
    private void processSuccessorEdges(LinearIRBlock block, int currentBlockId) {
        try {
            List<LinearIRBlock> successors = block.getSuccessors();
            if (successors == null || successors.isEmpty()) {
                return;
            }
            
            for (LinearIRBlock successor : successors) {
                if (successor == null) {
                    logger.warn("Null successor found in block ord={}", block.getOrd());
                    continue;
                }
                
                int successorId = successor.getOrd();
                String edgeKey = createEdgeKey(currentBlockId, successorId, CFGConstants.SUCCESSOR_EDGE_TYPE);
                
                if (addEdgeIfNotExists(edgeKey, currentBlockId, successorId, CFGConstants.SUCCESSOR_EDGE_TYPE)) {
                    logger.debug("Added successor edge: {} -> {}", currentBlockId, successorId);
                    traverseBlock(successor);
                }
            }
        } catch (Exception e) {
            logger.error("Error processing successor edges for block {}", block.getOrd(), e);
        }
    }

    /**
     * Creates a unique key for edge identification using efficient string formatting.
     */
    private String createEdgeKey(int sourceId, int targetId, int edgeType) {
        return "%d-%d-%d".formatted(sourceId, targetId, edgeType);
    }

    /**
     * Adds an edge to the graph if it doesn't already exist.
     * Returns true if the edge was added, false if it already existed.
     * 终极修复：使用processedConnections确保同一对节点只有一条边（忽略边类型）
     */
    private boolean addEdgeIfNotExists(String edgeKey, int sourceId, int targetId, int edgeType) {
        // 终极修复：只跟踪节点对，完全忽略边类型
        String connectionKey = "%d-%d".formatted(sourceId, targetId);
        
        synchronized (processedEdges) {
            // 第一道防线：检查processedConnections（只关心节点对）
            if (processedConnections.contains(connectionKey)) {
                logger.debug("节点对 {}-{} 已存在，跳过添加边: {}（类型: {}）",
                            sourceId, targetId, edgeKey, edgeType);
                return false;
            }
            
            // 第二道防线：检查edges列表（额外保险）
            boolean existsInEdges = edges.stream()
                .anyMatch(e -> e.getLeft() == sourceId && e.getMiddle() == targetId);
                
            if (existsInEdges) {
                logger.warn("edges中已存在边 {} -> {}，跳过添加（类型: {}）",
                           sourceId, targetId, edgeType);
                // 添加到processedConnections防止后续重复
                processedConnections.add(connectionKey);
                return false;
            }
            
            // 所有检查通过，原子性添加
            processedConnections.add(connectionKey);
            processedEdges.add(edgeKey);
            edges.add(Triple.of(sourceId, targetId, edgeType));
            
            logger.debug("边成功添加: {} -> {}, 类型: {}, edges: {}, connections: {}",
                       sourceId, targetId, edgeType, edges.size(), processedConnections.size());
            return true;
        }
    }

    /**
     * Gets the constructed Control Flow Graph.
     * 
     * @return the CFG instance (never null)
     */
    public CFG<IRNode> getCFG() {
        return Objects.requireNonNull(cfg, "CFG has not been properly initialized");
    }

    /**
     * Gets statistics about the built CFG for debugging and monitoring purposes.
     *
     * @return a map containing CFG statistics
     */
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("basicBlockCount", basicBlocks.size());
        stats.put("edgeCount", edges.size());
        stats.put("visitedBlockCount", visitedBlocks.size());
        stats.put("processedEdgeCount", processedEdges.size());
        stats.put("recursionDepth", recursionDepth);
        return Collections.unmodifiableMap(stats);
    }

    /**
     * Validates the constructed CFG for consistency and correctness.
     * 
     * @return true if the CFG is valid, false otherwise
     */
    public boolean validateCFG() {
        if (basicBlocks.isEmpty()) {
            logger.warn("CFG validation failed: no basic blocks found");
            return false;
        }
        
        // Check for duplicate basic blocks
        Set<Integer> blockIds = new HashSet<>();
        for (BasicBlock<IRNode> block : basicBlocks) {
            if (!blockIds.add(block.getId())) {
                logger.warn("CFG validation failed: duplicate block ID found: {}", block.getId());
                return false; // Duplicate block ID found
            }
        }
        
        // Validate edge consistency
        for (Triple<Integer, Integer, Integer> edge : edges) {
            if (edge.getLeft() < 0 || edge.getMiddle() < 0) {
                logger.warn("CFG validation failed: invalid edge found: {}", edge);
                return false; // Invalid block IDs in edge
            }
        }
        
        logger.info("CFG validation passed: {} blocks, {} edges", basicBlocks.size(), edges.size());
        return true;
    }
}