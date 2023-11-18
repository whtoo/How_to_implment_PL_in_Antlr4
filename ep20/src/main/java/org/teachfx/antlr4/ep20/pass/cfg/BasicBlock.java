package org.teachfx.antlr4.ep20.pass.cfg;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.teachfx.antlr4.ep20.ir.IRNode;
import org.teachfx.antlr4.ep20.ir.expr.Operand;
import org.teachfx.antlr4.ep20.ir.stmt.Label;
import org.teachfx.antlr4.ep20.utils.Kind;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BasicBlock<I extends IRNode> implements Iterable<Loc<I>> {

    // Generate codes
    public List<Loc<I>> codes;

    public final int id;

    protected Optional<Label> label;

    public Kind kind;

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

    @NotNull
    @Contract("_ -> new")
    public static BasicBlock<IRNode> buildFromLinearBlock(@NotNull LinearIRBlock block) {
        return new BasicBlock<IRNode>(block.getKind(), block.getOrd(), block.getStmts().stream().map(Loc::new).toList(), Optional.of(block.getLabel()));
    }

    public BasicBlock(Kind kind, int id, List<Loc<I>> codes, Optional<Label> label) {
        this.codes = codes;
        this.label = label;
        this.id = id;
        this.kind = kind;
    }

    public int getId() {
        return id;
    }

    public Optional<Label> getLabel() {
        return label;
    }

    // Generate isEmpty
    public boolean isEmpty() {
        return codes.isEmpty();
    }
    public List<Loc<I>> allSeq() {
        if (kind.equals(Kind.CONTINUOUS)){
            return codes;
        }
        return codes.subList(0, codes.size() - 1);
    }

    public I getLastInstr() {
        return codes.get(codes.size() - 1).instr;
    }

    // For data flow analysis
    public Set<Operand> def;

    public Set<Operand> liveUse;

    public Set<Operand> liveIn;

    public Set<Operand> liveOut;
}