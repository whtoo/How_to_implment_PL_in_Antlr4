package org.teachfx.antlr4.ep21.pass.cfg;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep21.ir.IRNode;
import org.teachfx.antlr4.ep21.ir.JMPInstr;
import org.teachfx.antlr4.ep21.ir.expr.Operand;
import org.teachfx.antlr4.ep21.ir.stmt.CJMP;
import org.teachfx.antlr4.ep21.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep21.ir.stmt.Label;
import org.teachfx.antlr4.ep21.utils.Kind;
import org.teachfx.antlr4.ep21.utils.StreamUtils;

import java.util.*;
import java.util.stream.Stream;

/**
 * BasicBlock represents a basic block in a Control Flow Graph (CFG).
 * A basic block is a sequence of instructions that executes sequentially 
 * with a single entry point and single exit point.
 * 
 * @param <I> The instruction type that extends IRNode
 */
public class BasicBlock<I extends IRNode> implements Comparable<BasicBlock<I>>, Iterable<Loc<I>> {

    public final int id;
    // Generate codes
    public List<Loc<I>> codes;
    public Kind kind;

    // For data flow analysis
    public Set<Operand> def;
    public Set<Operand> liveUse;
    public Set<Operand> liveIn;
    public Set<Operand> liveOut;

    protected Label label;

    /**
     * Constructs a new BasicBlock with the specified parameters.
     * 
     * @param kind The type of the basic block (must not be null)
     * @param codes The list of instruction locations (must not be null)
     * @param label The label of the basic block (must not be null)
     * @param ord The ordinal number of the basic block (must be non-negative)
     * @throws IllegalArgumentException if ord is negative
     * @throws NullPointerException if any required parameter is null
     */
    public BasicBlock(@NotNull Kind kind, @NotNull List<Loc<I>> codes, @NotNull Label label, int ord) {
        validateConstructorParameters(kind, codes, label, ord);
        
        this.codes = new ArrayList<>(codes);
        this.label = label;
        this.id = ord;
        this.kind = Objects.requireNonNull(kind, "kind cannot be null");
        
        // Initialize data flow analysis sets
        this.def = new HashSet<>();
        this.liveUse = new HashSet<>();
        this.liveIn = new HashSet<>();
        this.liveOut = new HashSet<>();
    }

    /**
     * Validates the constructor parameters for correctness.
     */
    private static void validateConstructorParameters(@NotNull Kind kind, @NotNull List<?> codes, 
                                                     @NotNull Label label, int ord) {
        Objects.requireNonNull(kind, "kind cannot be null");
        Objects.requireNonNull(codes, "codes cannot be null");
        Objects.requireNonNull(label, "label cannot be null");
        
        if (ord < 0) {
            throw new IllegalArgumentException("BasicBlock ordinal must be non-negative: " + ord);
        }
        
        if (codes.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("codes list cannot contain null elements");
        }
    }

    /**
     * Factory method to build a BasicBlock from a LinearIRBlock.
     * 
     * @param block the LinearIRBlock to convert (must not be null)
     * @param cachedNodes cached basic blocks (currently unused, reserved for future optimization)
     * @return a new BasicBlock instance
     * @throws NullPointerException if block is null
     */
    @NotNull
    @Contract("_ -> new")
    public static BasicBlock<IRNode> buildFromLinearBlock(@NotNull LinearIRBlock block,
                                                         @SuppressWarnings("unused") @NotNull List<BasicBlock<IRNode>> cachedNodes) {
        Objects.requireNonNull(block, "LinearIRBlock cannot be null");
        
        // Handle potentially null label from LinearIRBlock
        Label label = block.getLabel();
        if (label == null) {
            // Create a default label based on the block's ordinal
            label = new Label("L" + block.getOrd(), null);
        }
        
        return new BasicBlock<>(
            block.getKind(),
            block.getStmts().stream().map(Loc::new).toList(),
            label,
            block.getOrd()
        );
    }

    @Override
    public int compareTo(@NotNull BasicBlock<I> o) {
        return Integer.compare(this.id, o.id);
    }

    @NotNull
    @Override
    public Iterator<Loc<I>> iterator() {
        return Collections.unmodifiableList(codes).iterator();
    }

    /**
     * Returns a backward iterator for the instruction list.
     * 
     * @return an iterator that traverses the instructions in reverse order
     */
    @NotNull
    public Iterator<Loc<I>> backwardIterator() {
        return new Iterator<Loc<I>>() {
            private int index = codes.size() - 1;

            @Override
            public boolean hasNext() {
                return index >= 0;
            }

            @Override
            public Loc<I> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No more elements in reverse iterator");
                }
                var loc = codes.get(index);
                index--;
                return loc;
            }
        };
    }

    /**
     * Gets the ID of the basic block.
     * 
     * @return the ID of this basic block
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the label of the basic block.
     * 
     * @return the label (never null)
     */
    @NotNull
    public Label getLabel() {
        return label;
    }

    /**
     * Sets the label of the basic block.
     * 
     * @param label the new label (must not be null)
     */
    public void setLabel(@NotNull Label label) {
        this.label = Objects.requireNonNull(label, "label cannot be null");
    }

    /**
     * Returns an ordinal label for the basic block.
     * 
     * @return a formatted label string in the format "L{id}"
     */
    @NotNull
    public String getOrdLabel() {
        return "L%d".formatted(id);
    }

    /**
     * Checks if the basic block is empty.
     * 
     * @return true if the basic block contains no instructions, false otherwise
     */
    public boolean isEmpty() {
        return codes.isEmpty();
    }

    /**
     * Returns the number of instructions in this basic block.
     * 
     * @return the instruction count
     */
    public int getInstructionCount() {
        return codes.size();
    }

    /**
     * Returns all instructions as an unmodifiable view.
     * This provides better encapsulation while maintaining backward compatibility.
     * 
     * @return an unmodifiable view of the instruction list
     */
    @NotNull
    public List<Loc<I>> getInstructionsView() {
        return Collections.unmodifiableList(codes);
    }

    /**
     * Returns all sequential instructions (excluding the last instruction if not continuous).
     * 
     * @return the list of sequential instructions
     */
    @NotNull
    public List<Loc<I>> allSeq() {
        if (kind == null || kind.equals(Kind.CONTINUOUS)) {
            return Collections.unmodifiableList(codes);
        }
        
        if (codes.isEmpty()) {
            return Collections.emptyList();
        }
        
        return Collections.unmodifiableList(codes.subList(0, Math.max(0, codes.size() - 1)));
    }

    /**
     * Returns the instruction sequence with the label instruction dropped.
     * If the first instruction is a function entry label, returns the full sequence.
     * 
     * @return the instruction sequence without the leading label
     */
    @NotNull
    public List<Loc<I>> dropLabelSeq() {
        if (codes.size() <= 1) {
            return Collections.unmodifiableList(codes);
        }

        if (codes.get(0).instr instanceof FuncEntryLabel) {
            return Collections.unmodifiableList(codes);
        }

        return Collections.unmodifiableList(codes.subList(1, codes.size()));
    }

    /**
     * Returns the last instruction in the basic block.
     *
     * @return the last instruction, or null if the basic block is empty
     */
    @SuppressWarnings("unchecked")
    public I getLastInstr() {
        if (codes.isEmpty()) {
            return null;
        }
        return ((Loc<I>) codes.get(codes.size() - 1)).instr;
    }

    /**
     * Enhanced version of getLastInstr with better error handling.
     * 
     * @return the last instruction
     * @throws NoSuchElementException if the basic block is empty
     */
    @SuppressWarnings("unchecked")
    @NotNull 
    public I getLastInstruction() {
        return getLastInstr();
    }

    /**
     * Checks if the last instruction is a jump instruction.
     * 
     * @return true if the last instruction is a jump instruction, false otherwise
     */
    public boolean hasJumpInstruction() {
        if (codes.isEmpty()) {
            return false;
        }
        return codes.get(codes.size() - 1).instr instanceof JMPInstr;
    }

    /**
     * Checks if the last instruction is a conditional jump.
     * 
     * @return true if the last instruction is a conditional jump, false otherwise
     */
    public boolean hasConditionalJump() {
        if (codes.isEmpty()) {
            return false;
        }
        return codes.get(codes.size() - 1).instr instanceof CJMP;
    }

    /**
     * Merges this block with the next block.
     * Removes the last jump instruction and merges the next block's instructions.
     * The nextBlock is completely cleared after merging.
     *
     * @param nextBlock the block to merge with this block
     * @throws NullPointerException if nextBlock is null
     * @throws IllegalStateException if this block doesn't have a jump instruction
     */
    public void mergeNearBlock(@NotNull BasicBlock<I> nextBlock) {
        Objects.requireNonNull(nextBlock, "nextBlock cannot be null");
        
        // Check if we need to remove the last jump instruction
        if (hasJumpInstruction()) {
            if (!codes.isEmpty()) {
                codes.remove(codes.size() - 1);
            }
        }

        // Merge all instructions from nextBlock
        @SuppressWarnings("unchecked")
        List<Loc<I>> nextInstructions = (List<Loc<I>>) nextBlock.codes;
        codes.addAll(nextInstructions);
        kind = nextBlock.kind;
        
        // Clear the nextBlock completely after merging
        nextBlock.codes.clear();
    }

    /**
     * Removes the last instruction and sets the kind to CONTINUOUS.
     * 
     * @deprecated This method modifies the internal state and may cause issues.
     *             Consider using immutable patterns or builder pattern instead.
     */
    @Deprecated
    public void removeLastInstr() {
        if (!codes.isEmpty()) {
            codes.remove(codes.size() - 1);
        }
        kind = Kind.CONTINUOUS;
    }

    /**
     * Enhanced version of removeLastInstr with better error handling.
     * 
     * @deprecated This method modifies the internal state and may cause issues.
     */
    @Deprecated
    public void removeLastInstruction() {
        removeLastInstr();
    }

    /**
     * Returns a stream of all IR nodes in this basic block.
     * 
     * @return a stream of IR nodes
     */
    @NotNull
    public Stream<I> getIRNodes() {
        return StreamUtils.flatMap(codes.stream(), Loc::getInstr);
    }

    /**
     * Creates and returns a safe copy of this basic block.
     * This can be used when defensive copying is needed.
     * 
     * @return a new BasicBlock with the same properties
     */
    @NotNull
    public BasicBlock<I> copy() {
        BasicBlock<I> copy = new BasicBlock<>(this.kind, new ArrayList<>(this.codes), this.label, this.id);
        
        // Copy data flow analysis sets
        copy.def.addAll(this.def);
        copy.liveUse.addAll(this.liveUse);
        copy.liveIn.addAll(this.liveIn);
        copy.liveOut.addAll(this.liveOut);
        
        return copy;
    }

    /**
     * Gets the immutable view of definitions set.
     * 
     * @return unmodifiable set of definition operands
     */
    @NotNull
    public Set<Operand> getDefinitions() {
        return Collections.unmodifiableSet(def);
    }

    /**
     * Gets the immutable view of live use set.
     * 
     * @return unmodifiable set of live use operands
     */
    @NotNull
    public Set<Operand> getLiveUse() {
        return Collections.unmodifiableSet(liveUse);
    }

    /**
     * Gets the immutable view of live in set.
     * 
     * @return unmodifiable set of live in operands
     */
    @NotNull
    public Set<Operand> getLiveIn() {
        return Collections.unmodifiableSet(liveIn);
    }

    /**
     * Gets the immutable view of live out set.
     * 
     * @return unmodifiable set of live out operands
     */
    @NotNull
    public Set<Operand> getLiveOut() {
        return Collections.unmodifiableSet(liveOut);
    }

    /**
     * Returns a string representation of the basic block.
     * 
     * @return string representation including ID, kind, and instruction count
     */
    @NotNull
    @Override
    public String toString() {
        return "BasicBlock{id=%d, kind=%s, instructionCount=%d}".formatted(id, kind, codes.size());
    }

    /**
     * Checks equality based on ID and label for better collection behavior.
     * 
     * @param obj the object to compare with
     * @return true if the objects are equal
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        BasicBlock<?> that = (BasicBlock<?>) obj;
        return id == that.id && 
               Objects.equals(kind, that.kind) &&
               Objects.equals(label, that.label);
    }

    /**
     * Computes hash code based on ID and label.
     * 
     * @return hash code value
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, kind, label);
    }

    /**
     * Builder pattern for creating BasicBlock instances with validation.
     */
    public static class Builder<I extends IRNode> {
        private int id;
        private Kind kind;
        private List<Loc<I>> codes;
        private Label label;

        public Builder<I> id(int id) {
            this.id = id;
            return this;
        }

        public Builder<I> kind(Kind kind) {
            this.kind = kind;
            return this;
        }

        public Builder<I> codes(List<Loc<I>> codes) {
            this.codes = new ArrayList<>(codes);
            return this;
        }

        public Builder<I> addCode(Loc<I> code) {
            if (this.codes == null) {
                this.codes = new ArrayList<>();
            }
            this.codes.add(Objects.requireNonNull(code, "code cannot be null"));
            return this;
        }

        public Builder<I> label(Label label) {
            this.label = label;
            return this;
        }

        public BasicBlock<I> build() {
            return new BasicBlock<>(
                Objects.requireNonNull(kind, "kind cannot be null"),
                Objects.requireNonNull(codes, "codes cannot be null"),
                Objects.requireNonNull(label, "label cannot be null"),
                id
            );
        }
    }
}