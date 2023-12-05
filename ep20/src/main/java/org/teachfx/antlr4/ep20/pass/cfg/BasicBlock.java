package org.teachfx.antlr4.ep20.pass.cfg;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.JMPInstr;
import org.teachfx.antlr4.ep20.ir.expr.Operand;
import org.teachfx.antlr4.ep20.ir.stmt.CJMP;
import org.teachfx.antlr4.ep20.ir.stmt.FuncEntryLabel;
import org.teachfx.antlr4.ep20.ir.stmt.JMP;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.utils.Kind;
import org.teachfx.antlr4.ep20.utils.StreamUtils;

import java.util.*;
import java.util.stream.Stream;

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

    public BasicBlock(Kind kind, List<Loc<I>> codes,Label label,int ord) {
        this.codes = new ArrayList<>(codes);
        this.label = label;
        this.id = ord;
        this.kind = kind;
    }

    @NotNull
    @Contract("_ -> new")
    public static BasicBlock<IRNode> buildFromLinearBlock(@NotNull LinearIRBlock block,List<BasicBlock<IRNode>> cachedNodes) {
        return new BasicBlock<IRNode>(block.getKind(), block.getStmts().stream().map(Loc::new).toList(),block.getLabel(),block.getOrd());
    }

    @Override
    public int compareTo(@NotNull BasicBlock<I> o) {
        return this.id - o.id;
    }

    @NotNull
    @Override
    public Iterator<Loc<I>> iterator() {
        return codes.iterator();
    }

    public Iterator<Loc<I>> backwardIterator() {
        return new Iterator<Loc<I>>() {
            private int index = codes.size() - 1;

            @Override
            public boolean hasNext() {
                return index != -1;
            }

            @Override
            public Loc<I> next() {
                var loc = codes.get(index);
                index--;
                return loc;
            }
        };
    }

    public int getId() {
        return id;
    }

    public Label getLabel() {
        return label;
    }

    public String getOrdLabel() {
        return "L%d".formatted(id);
    }

    // Generate isEmpty
    public boolean isEmpty() {
        return codes.isEmpty();
    }

    public List<Loc<I>> allSeq() {
        if (kind.equals(Kind.CONTINUOUS)) {
            return codes;
        }
        return codes.subList(0, codes.size() - 1);
    }

    public List<Loc<I>> dropLabelSeq() {
        if (codes.size() <= 1) return codes;

        if (codes.get(0).instr instanceof FuncEntryLabel) {
            return codes.subList(0, codes.size());
        }

        return codes.subList(1, codes.size());
    }

    public I getLastInstr() {
        return codes.get(codes.size() - 1).instr;
    }

    public void mergeNearBlock(BasicBlock<I> nextBlock) {
        /// remove last jump instr
        if (getLastInstr() instanceof JMPInstr) {
            codes.remove(codes.size() - 1);
        }

        /// merge instr and update kind to use merge nextblock's kind
        codes.addAll(nextBlock.dropLabelSeq());
        kind = nextBlock.kind;
    }

    public void removeLastInstr() {
        codes.remove(codes.size() - 1);
        kind = Kind.CONTINUOUS;
    }


    public Stream<I> getIRNodes() {
        return StreamUtils.flatMap(codes.stream(), Loc::getInstr);
    }
}