// Generated from ./compiler/MathExpr.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep14.compiler;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.teachfx.antlr4.ep14.symtab.BuiltIntTypeSymbol;
import org.teachfx.antlr4.ep14.symtab.SymbolTable;
import org.teachfx.antlr4.ep14.symtab.Type;
import org.teachfx.antlr4.ep14.symtab.VariableSymbol;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MathExprParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, OP_ADD = 7, OP_SUB = 8, OP_MUL = 9,
            OP_DIV = 10, INT = 11, FLOAT = 12, ID = 13, WS = 14;
    public static final int
            RULE_compileUnit = 0, RULE_varDelaration = 1, RULE_expr = 2, RULE_type = 3;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\20\62\4\2\t\2\4\3" +
                    "\t\3\4\4\t\4\4\5\t\5\3\2\6\2\f\n\2\r\2\16\2\r\3\3\3\3\3\3\3\3\5\3\24\n" +
                    "\3\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4\"\n\4\3\4\3\4\3" +
                    "\4\7\4\'\n\4\f\4\16\4*\13\4\3\5\3\5\3\5\3\5\5\5\60\n\5\3\5\2\3\6\6\2\4" +
                    "\6\b\2\2\2\64\2\13\3\2\2\2\4\17\3\2\2\2\6!\3\2\2\2\b/\3\2\2\2\n\f\5\4" +
                    "\3\2\13\n\3\2\2\2\f\r\3\2\2\2\r\13\3\2\2\2\r\16\3\2\2\2\16\3\3\2\2\2\17" +
                    "\20\5\b\5\2\20\23\7\17\2\2\21\22\7\3\2\2\22\24\5\6\4\2\23\21\3\2\2\2\23" +
                    "\24\3\2\2\2\24\25\3\2\2\2\25\26\7\4\2\2\26\27\b\3\1\2\27\5\3\2\2\2\30" +
                    "\31\b\4\1\2\31\"\7\r\2\2\32\"\7\16\2\2\33\34\7\17\2\2\34\"\b\4\1\2\35" +
                    "\36\7\5\2\2\36\37\5\6\4\2\37 \7\6\2\2 \"\3\2\2\2!\30\3\2\2\2!\32\3\2\2" +
                    "\2!\33\3\2\2\2!\35\3\2\2\2\"(\3\2\2\2#$\f\7\2\2$%\7\t\2\2%\'\5\6\4\b&" +
                    "#\3\2\2\2\'*\3\2\2\2(&\3\2\2\2()\3\2\2\2)\7\3\2\2\2*(\3\2\2\2+,\7\7\2" +
                    "\2,\60\b\5\1\2-.\7\b\2\2.\60\b\5\1\2/+\3\2\2\2/-\3\2\2\2\60\t\3\2\2\2" +
                    "\7\r\23!(/";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

    static {
        RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION);
    }

    static {
        tokenNames = new String[_SYMBOLIC_NAMES.length];
        for (int i = 0; i < tokenNames.length; i++) {
            tokenNames[i] = VOCABULARY.getLiteralName(i);
            if (tokenNames[i] == null) {
                tokenNames[i] = VOCABULARY.getSymbolicName(i);
            }

            if (tokenNames[i] == null) {
                tokenNames[i] = "<INVALID>";
            }
        }
    }

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
        for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
            _decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
        }
    }

    SymbolTable symtab;

    public MathExprParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "compileUnit", "varDelaration", "expr", "type"
        };
    }

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'='", "';'", "'('", "')'", "'int'", "'float'", "'+'", "'-'", "'*'",
                "'/'"
        };
    }

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, "OP_ADD", "OP_SUB", "OP_MUL",
                "OP_DIV", "INT", "FLOAT", "ID", "WS"
        };
    }

    @Override
    @Deprecated
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override

    public Vocabulary getVocabulary() {
        return VOCABULARY;
    }

    @Override
    public String getGrammarFileName() {
        return "MathExpr.g4";
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String getSerializedATN() {
        return _serializedATN;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    public final CompileUnitContext compileUnit(SymbolTable symtab) throws RecognitionException {
        CompileUnitContext _localctx = new CompileUnitContext(_ctx, getState(), symtab);
        enterRule(_localctx, 0, RULE_compileUnit);
        this.symtab = symtab;
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(9);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        {
                            setState(8);
                            varDelaration();
                        }
                    }
                    setState(11);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while (_la == T__4 || _la == T__5);
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final VarDelarationContext varDelaration() throws RecognitionException {
        VarDelarationContext _localctx = new VarDelarationContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_varDelaration);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(13);
                ((VarDelarationContext) _localctx).vtype = type();
                setState(14);
                ((VarDelarationContext) _localctx).name = match(ID);
                setState(17);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__0) {
                    {
                        setState(15);
                        match(T__0);
                        setState(16);
                        ((VarDelarationContext) _localctx).value = expr(0);
                    }
                }

                setState(19);
                match(T__1);

                BuiltIntTypeSymbol sym = (BuiltIntTypeSymbol) symtab.resolve((((VarDelarationContext) _localctx).vtype != null ? _input.getText(((VarDelarationContext) _localctx).vtype.start, ((VarDelarationContext) _localctx).vtype.stop) : null));

                VariableSymbol vs = new VariableSymbol((((VarDelarationContext) _localctx).name != null ? ((VarDelarationContext) _localctx).name.getText() : null), sym);
                symtab.define(vs);
                System.out.println((((VarDelarationContext) _localctx).name != null ? ((VarDelarationContext) _localctx).name.getText() : null) + " ref to " + symtab.resolve((((VarDelarationContext) _localctx).name != null ? ((VarDelarationContext) _localctx).name.getText() : null)));

            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public final ExprContext expr() throws RecognitionException {
        return expr(0);
    }

    private ExprContext expr(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        ExprContext _localctx = new ExprContext(_ctx, _parentState);
        ExprContext _prevctx = _localctx;
        int _startState = 4;
        enterRecursionRule(_localctx, 4, RULE_expr, _p);
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(31);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case INT: {
                        setState(23);
                        match(INT);
                    }
                    break;
                    case FLOAT: {
                        setState(24);
                        match(FLOAT);
                    }
                    break;
                    case ID: {
                        setState(25);
                        ((ExprContext) _localctx).name = match(ID);
                        System.out.println("a2 line " + ((ExprContext) _localctx).name.getLine() + " " + (((ExprContext) _localctx).name != null ? ((ExprContext) _localctx).name.getText() : null) + " : ref to " +
                                symtab.resolve((((ExprContext) _localctx).name != null ? ((ExprContext) _localctx).name.getText() : null)));
                    }
                    break;
                    case T__2: {
                        setState(27);
                        match(T__2);
                        setState(28);
                        expr(0);
                        setState(29);
                        match(T__3);
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(38);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            {
                                _localctx = new ExprContext(_parentctx, _parentState);
                                _localctx.lhs = _prevctx;
                                _localctx.lhs = _prevctx;
                                pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                setState(33);
                                if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                setState(34);
                                ((ExprContext) _localctx).op = match(OP_ADD);
                                setState(35);
                                ((ExprContext) _localctx).rhs = expr(6);
                            }
                        }
                    }
                    setState(40);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
                }
            }
        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            unrollRecursionContexts(_parentctx);
        }
        return _localctx;
    }

    public final TypeContext type() throws RecognitionException {
        TypeContext _localctx = new TypeContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_type);
        try {
            setState(45);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case T__4:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(41);
                    match(T__4);

                    ((TypeContext) _localctx).tsym = (Type) symtab.resolve("int");

                }
                break;
                case T__5:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(43);
                    match(T__5);

                    ((TypeContext) _localctx).tsym = (Type) symtab.resolve("float");

                }
                break;
                default:
                    throw new NoViableAltException(this);
            }
            _ctx.stop = _input.LT(-1);
            // _localctx.start is the first tree node matched by this rule
            System.out.println("a3 line " + _localctx.start.getLine() + ": ref " + _localctx.tsym.getName());

        } catch (RecognitionException re) {
            _localctx.exception = re;
            _errHandler.reportError(this, re);
            _errHandler.recover(this, re);
        } finally {
            exitRule();
        }
        return _localctx;
    }

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 2:
                return expr_sempred((ExprContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean expr_sempred(ExprContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return precpred(_ctx, 5);
        }
        return true;
    }

    public static class CompileUnitContext extends ParserRuleContext {
        public SymbolTable symtab;

        public CompileUnitContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public CompileUnitContext(ParserRuleContext parent, int invokingState, SymbolTable symtab) {
            super(parent, invokingState);
            this.symtab = symtab;
        }

        public List<VarDelarationContext> varDelaration() {
            return getRuleContexts(VarDelarationContext.class);
        }

        public VarDelarationContext varDelaration(int i) {
            return getRuleContext(VarDelarationContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_compileUnit;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof MathExprVisitor)
                return ((MathExprVisitor<? extends T>) visitor).visitCompileUnit(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class VarDelarationContext extends ParserRuleContext {
        public TypeContext vtype;
        public Token name;
        public ExprContext value;

        public VarDelarationContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TypeContext type() {
            return getRuleContext(TypeContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(MathExprParser.ID, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_varDelaration;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof MathExprVisitor)
                return ((MathExprVisitor<? extends T>) visitor).visitVarDelaration(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExprContext extends ParserRuleContext {
        public ExprContext lhs;
        public Token name;
        public Token op;
        public ExprContext rhs;

        public ExprContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode INT() {
            return getToken(MathExprParser.INT, 0);
        }

        public TerminalNode FLOAT() {
            return getToken(MathExprParser.FLOAT, 0);
        }

        public TerminalNode ID() {
            return getToken(MathExprParser.ID, 0);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        public TerminalNode OP_ADD() {
            return getToken(MathExprParser.OP_ADD, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_expr;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof MathExprVisitor) return ((MathExprVisitor<? extends T>) visitor).visitExpr(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class TypeContext extends ParserRuleContext {
        public Type tsym;

        public TypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_type;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof MathExprVisitor) return ((MathExprVisitor<? extends T>) visitor).visitType(this);
            else return visitor.visitChildren(this);
        }
    }
}