    @Override
    public Object visitStat(StatContext ctx) {
        stashScope(ctx);
        return super.visitStat(ctx);
    }

    @Override
    public Object visitExprStructFieldAccess(ExprStructFieldAccessContext ctx) {
        stashScope(ctx);
        return super.visitExprStructFieldAccess(ctx);
    }

    @Override
    public Object visitExprPrimary(ExprPrimaryContext ctx) {
        stashScope(ctx);
        return super.visitExprPrimary(ctx);
    }
