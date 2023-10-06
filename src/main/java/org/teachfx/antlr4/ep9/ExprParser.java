// Generated from Expr.g4 by ANTLR 4.8
package org.teachfx.antlr4.ep9;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ExprParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, MUL = 4, DIV = 5, ADD = 6, SUB = 7, ID = 8, INT = 9, NEWLINE = 10,
            WS = 11;
    public static final int
            RULE_stat = 0, RULE_e = 1;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\r/\4\2\t\2\4\3\t" +
                    "\3\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\5\2\22\n\2\3\3\3\3\3\3" +
                    "\3\3\3\3\3\3\3\3\3\3\3\3\3\3\5\3\36\n\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3\3" +
                    "\3\3\3\3\3\7\3*\n\3\f\3\16\3-\13\3\3\3\2\3\4\4\2\4\2\4\3\2\6\7\3\2\b\t" +
                    "\2\62\2\21\3\2\2\2\4\35\3\2\2\2\6\7\5\4\3\2\7\b\7\f\2\2\b\t\b\2\1\2\t" +
                    "\22\3\2\2\2\n\13\7\n\2\2\13\f\7\3\2\2\f\r\5\4\3\2\r\16\7\f\2\2\16\17\b" +
                    "\2\1\2\17\22\3\2\2\2\20\22\7\f\2\2\21\6\3\2\2\2\21\n\3\2\2\2\21\20\3\2" +
                    "\2\2\22\3\3\2\2\2\23\24\b\3\1\2\24\25\7\13\2\2\25\36\b\3\1\2\26\27\7\n" +
                    "\2\2\27\36\b\3\1\2\30\31\7\4\2\2\31\32\5\4\3\2\32\33\7\5\2\2\33\34\b\3" +
                    "\1\2\34\36\3\2\2\2\35\23\3\2\2\2\35\26\3\2\2\2\35\30\3\2\2\2\36+\3\2\2" +
                    "\2\37 \f\7\2\2 !\t\2\2\2!\"\5\4\3\b\"#\b\3\1\2#*\3\2\2\2$%\f\6\2\2%&\t" +
                    "\3\2\2&\'\5\4\3\7\'(\b\3\1\2(*\3\2\2\2)\37\3\2\2\2)$\3\2\2\2*-\3\2\2\2" +
                    "+)\3\2\2\2+,\3\2\2\2,\5\3\2\2\2-+\3\2\2\2\6\21\35)+";
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

    /**
     * Memory for our caculator; variable/value pairs go here
     */
    Map<String, Integer> memory = new HashMap<String, Integer>();

    public ExprParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "stat", "e"
        };
    }

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'='", "'('", "')'", "'*'", "'/'", "'+'", "'-'"
        };
    }

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, "MUL", "DIV", "ADD", "SUB", "ID", "INT", "NEWLINE",
                "WS"
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
        return "Expr.g4";
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

    int eval(int left, int op, int right) {
        switch (op) {
            case MUL:
                return left * right;
            case DIV:
                return left / right;
            case ADD:
                return left + right;
            case SUB:
                return left - right;
        }
        return 0;
    }

    public final StatContext stat() throws RecognitionException {
        StatContext _localctx = new StatContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_stat);
        try {
            setState(15);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                case 1:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(4);
                    ((StatContext) _localctx).e = e(0);
                    setState(5);
                    match(NEWLINE);
                    System.out.println(((StatContext) _localctx).e.v);
                }
                break;
                case 2:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(8);
                    ((StatContext) _localctx).ID = match(ID);
                    setState(9);
                    match(T__0);
                    setState(10);
                    ((StatContext) _localctx).e = e(0);
                    setState(11);
                    match(NEWLINE);
                    memory.put((((StatContext) _localctx).ID != null ? ((StatContext) _localctx).ID.getText() : null), ((StatContext) _localctx).e.v);
                }
                break;
                case 3:
                    enterOuterAlt(_localctx, 3);
                {
                    setState(14);
                    match(NEWLINE);
                }
                break;
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

    public final EContext e() throws RecognitionException {
        return e(0);
    }

    private EContext e(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        EContext _localctx = new EContext(_ctx, _parentState);
        EContext _prevctx = _localctx;
        int _startState = 2;
        enterRecursionRule(_localctx, 2, RULE_e, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(27);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case INT: {
                        setState(18);
                        ((EContext) _localctx).INT = match(INT);
                        ((EContext) _localctx).v = (((EContext) _localctx).INT != null ? Integer.valueOf(((EContext) _localctx).INT.getText()) : 0);
                    }
                    break;
                    case ID: {
                        setState(20);
                        ((EContext) _localctx).ID = match(ID);

                        String id = (((EContext) _localctx).ID != null ? ((EContext) _localctx).ID.getText() : null);
                        ((EContext) _localctx).v = memory.containsKey(id) ? memory.get(id) : 0;

                    }
                    break;
                    case T__1: {
                        setState(22);
                        match(T__1);
                        setState(23);
                        ((EContext) _localctx).e = e(0);
                        setState(24);
                        match(T__2);
                        ((EContext) _localctx).v = ((EContext) _localctx).e.v;
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(41);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 3, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(39);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 2, _ctx)) {
                                case 1: {
                                    _localctx = new EContext(_parentctx, _parentState);
                                    _localctx.a = _prevctx;
                                    _localctx.a = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_e);
                                    setState(29);
                                    if (!(precpred(_ctx, 5)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                    setState(30);
                                    ((EContext) _localctx).op = _input.LT(1);
                                    _la = _input.LA(1);
                                    if (!(_la == MUL || _la == DIV)) {
                                        ((EContext) _localctx).op = (Token) _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(31);
                                    ((EContext) _localctx).b = ((EContext) _localctx).e = e(6);
                                    ((EContext) _localctx).v = eval(((EContext) _localctx).a.v, (((EContext) _localctx).op != null ? ((EContext) _localctx).op.getType() : 0), ((EContext) _localctx).b.v);
                                }
                                break;
                                case 2: {
                                    _localctx = new EContext(_parentctx, _parentState);
                                    _localctx.a = _prevctx;
                                    _localctx.a = _prevctx;
                                    pushNewRecursionContext(_localctx, _startState, RULE_e);
                                    setState(34);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(35);
                                    ((EContext) _localctx).op = _input.LT(1);
                                    _la = _input.LA(1);
                                    if (!(_la == ADD || _la == SUB)) {
                                        ((EContext) _localctx).op = (Token) _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(36);
                                    ((EContext) _localctx).b = ((EContext) _localctx).e = e(5);
                                    ((EContext) _localctx).v = eval(((EContext) _localctx).a.v, (((EContext) _localctx).op != null ? ((EContext) _localctx).op.getType() : 0), ((EContext) _localctx).b.v);
                                }
                                break;
                            }
                        }
                    }
                    setState(43);
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

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 1:
                return e_sempred((EContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean e_sempred(EContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return precpred(_ctx, 5);
            case 1:
                return precpred(_ctx, 4);
        }
        return true;
    }

    public static class StatContext extends ParserRuleContext {
        public EContext e;
        public Token ID;

        public StatContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public EContext e() {
            return getRuleContext(EContext.class, 0);
        }

        public TerminalNode NEWLINE() {
            return getToken(ExprParser.NEWLINE, 0);
        }

        public TerminalNode ID() {
            return getToken(ExprParser.ID, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_stat;
        }
    }

    public static class EContext extends ParserRuleContext {
        public int v;
        public EContext a;
        public Token INT;
        public Token ID;
        public EContext e;
        public Token op;
        public EContext b;

        public EContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TerminalNode INT() {
            return getToken(ExprParser.INT, 0);
        }

        public TerminalNode ID() {
            return getToken(ExprParser.ID, 0);
        }

        public List<EContext> e() {
            return getRuleContexts(EContext.class);
        }

        public EContext e(int i) {
            return getRuleContext(EContext.class, i);
        }

        public TerminalNode MUL() {
            return getToken(ExprParser.MUL, 0);
        }

        public TerminalNode DIV() {
            return getToken(ExprParser.DIV, 0);
        }

        public TerminalNode ADD() {
            return getToken(ExprParser.ADD, 0);
        }

        public TerminalNode SUB() {
            return getToken(ExprParser.SUB, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_e;
        }
    }
}