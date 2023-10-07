package org.teachfx.antlr4.ep20.ast.expr;

import org.antlr.v4.runtime.ParserRuleContext;
import org.teachfx.antlr4.ep20.ast.ASTVisitor;
import org.teachfx.antlr4.ep20.debugger.ast.Dumper;
import org.teachfx.antlr4.ep20.symtab.symbol.MethodSymbol;
import org.teachfx.antlr4.ep20.symtab.type.Type;

import java.util.List;
import java.util.Objects;

public class CallFuncNode extends ExprNode {
    @Override
    protected void _dump(Dumper d) {
        super._dump(d);
        d.printMember("func",funcName);
        d.printNodeList("args",argsNode);
    }

    private List<ExprNode> argsNode;
    private MethodSymbol callFuncSymbol;

    private String funcName;

    public CallFuncNode(String funcName,List<ExprNode> args, ParserRuleContext ctx) {
        this.funcName = funcName;
        this.argsNode = args;
        this.ctx = ctx;
    }

    public List<ExprNode> getArgsNode() {
        return argsNode;
    }

    public void setArgsNode(List<ExprNode> argsNode) {
        this.argsNode = argsNode;
    }

    public MethodSymbol getCallFuncSymbol() {
        return callFuncSymbol;
    }

    public void setCallFuncSymbol(MethodSymbol callFuncSymbol) {
        this.callFuncSymbol = callFuncSymbol;
    }

    public String getFuncName() {
        return funcName;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    @Override
    public Type getExprType() {
        return callFuncSymbol.getFuncType();
    }

    @Override
    public void accept(ASTVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CallFuncNode that)) return false;
        return Objects.equals(getArgsNode(), that.getArgsNode()) && Objects.equals(getCallFuncSymbol(), that.getCallFuncSymbol()) && Objects.equals(getFuncName(), that.getFuncName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getArgsNode(), getCallFuncSymbol(), getFuncName());
    }
}
