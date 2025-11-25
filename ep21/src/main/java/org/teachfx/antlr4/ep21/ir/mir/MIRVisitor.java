package org.teachfx.antlr4.ep21.ir.mir;

/**
 * MIR访问者接口，用于遍历和转换MIR节点
 */
public interface MIRVisitor<R> {
    
    R visit(MIRNode node);
}