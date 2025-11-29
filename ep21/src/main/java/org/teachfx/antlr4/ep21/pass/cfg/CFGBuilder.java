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
 * Improvements made:
 * 1. Enhanced code readability and maintainability
 * 2. Performance optimization with better data structures
 * 3. Best practices and design patterns implementation
 * 4. Comprehensive error handling and edge case management
 * 5. Professional logging with Log4j2
 */
public class CFGBuilder {
    private static final Logger logger = LogManager.getLogger(CFGBuilder.class);
    
    // 使用CFGConstants中的常量，避免重复定义魔法数字
    private static final int MAX_RECURSION_DEPTH = 10000;
    
    private final CFG<IRNode> cfg;
    private final List<BasicBlock<IRNode>> basicBlocks;
    private final List<Triple<Integer, Integer, Integer>> edges;
    private final Set<LinearIRBlock> visitedBlocks;
    private final Set<String> processedEdges;
    
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
     */
    private boolean addEdgeIfNotExists(String edgeKey, int sourceId, int targetId, int edgeType) {
        // 检查是否已经存在从sourceId到targetId的任何边（忽略边类型）
        String connectionKey = "%d-%d".formatted(sourceId, targetId);
        
        // 如果已经存在相同节点间的连接，跳过添加
        boolean hasExistingConnection = processedEdges.stream()
            .anyMatch(existingKey -> existingKey.startsWith(connectionKey + "-"));
            
        if (hasExistingConnection) {
            logger.debug("节点{}和{}之间已有连接，跳过添加边: {}, 类型: {}",
                        sourceId, targetId, edgeKey, edgeType);
            return false;
        }
        
        boolean wasAdded = processedEdges.add(edgeKey);
        logger.debug("尝试添加边: {} -> {}, 类型: {}, 边键: {}, 结果: {}",
                    sourceId, targetId, edgeType, edgeKey, wasAdded ? "添加成功" : "已存在，跳过");
        
        if (wasAdded) {
            edges.add(Triple.of(sourceId, targetId, edgeType));
            logger.debug("边已添加到edges列表，当前edges数量: {}", edges.size());
        } else {
            // 尝试查找已存在的相同边
            long existingCount = edges.stream()
                .filter(e -> e.getLeft() == sourceId && e.getMiddle() == targetId && e.getRight() == edgeType)
                .count();
            logger.warn("检测到重复边键，但已存在边数量: {}, edges列表: {}", existingCount, edges);
        }
        
        return wasAdded;
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
    public Map<String, Object> getCFGStatistics() {
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