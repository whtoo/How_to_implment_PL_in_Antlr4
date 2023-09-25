package org.teachfx.antlr4.ep20.ast.stmt;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.ast.expr.ExprNode;
import org.teachfx.antlr4.ep20.debugger.Dumper;
import org.teachfx.antlr4.ep20.symtab.Type;

import java.util.List;

public class AssignStmtNode extends StmtNode{
    protected Type type;
    protected ExprNode lhs;
    protected ExprNode rhs;


    public AssignStmtNode(ExprNode lhs, ExprNode rhs, ParserRuleContext ctx) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.ctx = ctx;
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("lhs",lhs);
        d.printMember("rhs",rhs);
    }
}
