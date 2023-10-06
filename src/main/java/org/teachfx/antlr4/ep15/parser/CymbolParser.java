// Generated from ./parser/Cymbol.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep15.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CymbolParser extends Parser {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            ID = 18, INT = 19, FLOAT = 20, WS = 21, SLCOMMENT = 22;
    public static final int
            RULE_file = 0, RULE_varDecl = 1, RULE_type = 2, RULE_functionDecl = 3,
            RULE_formalParameters = 4, RULE_formalParameter = 5, RULE_block = 6, RULE_statatment = 7,
            RULE_expr = 8, RULE_primary = 9;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\30\u0081\4\2\t\2" +
                    "\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\3\2\3\2\6\2\31\n\2\r\2\16\2\32\3\3\3\3\3\3\3\3\5\3!\n\3\3\3\3\3" +
                    "\3\4\3\4\3\5\3\5\3\5\3\5\5\5+\n\5\3\5\3\5\3\5\3\6\3\6\3\6\7\6\63\n\6\f" +
                    "\6\16\6\66\13\6\3\7\3\7\3\7\3\b\3\b\7\b=\n\b\f\b\16\b@\13\b\3\b\3\b\3" +
                    "\t\3\t\3\t\3\t\5\tH\n\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\tS\n\t\3" +
                    "\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\n_\n\n\3\n\3\n\3\n\3\n\3\n\3" +
                    "\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\7\no\n\n\f\n\16\nr\13\n\5\nt\n\n\3" +
                    "\n\7\nw\n\n\f\n\16\nz\13\n\3\13\3\13\3\13\5\13\177\n\13\3\13\2\3\22\f" +
                    "\2\4\6\b\n\f\16\20\22\24\2\5\3\2\5\7\3\2\20\21\4\2\16\16\22\22\2\u008c" +
                    "\2\30\3\2\2\2\4\34\3\2\2\2\6$\3\2\2\2\b&\3\2\2\2\n/\3\2\2\2\f\67\3\2\2" +
                    "\2\16:\3\2\2\2\20R\3\2\2\2\22^\3\2\2\2\24~\3\2\2\2\26\31\5\b\5\2\27\31" +
                    "\5\4\3\2\30\26\3\2\2\2\30\27\3\2\2\2\31\32\3\2\2\2\32\30\3\2\2\2\32\33" +
                    "\3\2\2\2\33\3\3\2\2\2\34\35\5\6\4\2\35 \7\24\2\2\36\37\7\3\2\2\37!\5\22" +
                    "\n\2 \36\3\2\2\2 !\3\2\2\2!\"\3\2\2\2\"#\7\4\2\2#\5\3\2\2\2$%\t\2\2\2" +
                    "%\7\3\2\2\2&\'\5\6\4\2\'(\7\24\2\2(*\7\b\2\2)+\5\n\6\2*)\3\2\2\2*+\3\2" +
                    "\2\2+,\3\2\2\2,-\7\t\2\2-.\5\16\b\2.\t\3\2\2\2/\64\5\f\7\2\60\61\7\n\2" +
                    "\2\61\63\5\f\7\2\62\60\3\2\2\2\63\66\3\2\2\2\64\62\3\2\2\2\64\65\3\2\2" +
                    "\2\65\13\3\2\2\2\66\64\3\2\2\2\678\5\6\4\289\7\24\2\29\r\3\2\2\2:>\7\13" +
                    "\2\2;=\5\20\t\2<;\3\2\2\2=@\3\2\2\2><\3\2\2\2>?\3\2\2\2?A\3\2\2\2@>\3" +
                    "\2\2\2AB\7\f\2\2B\17\3\2\2\2CS\5\16\b\2DS\5\4\3\2EG\7\r\2\2FH\5\22\n\2" +
                    "GF\3\2\2\2GH\3\2\2\2HI\3\2\2\2IS\7\4\2\2JK\5\22\n\2KL\7\3\2\2LM\5\22\n" +
                    "\2MN\7\4\2\2NS\3\2\2\2OP\5\22\n\2PQ\7\4\2\2QS\3\2\2\2RC\3\2\2\2RD\3\2" +
                    "\2\2RE\3\2\2\2RJ\3\2\2\2RO\3\2\2\2S\21\3\2\2\2TU\b\n\1\2UV\7\16\2\2V_" +
                    "\5\22\n\tWX\7\17\2\2X_\5\22\n\bY_\5\24\13\2Z[\7\b\2\2[\\\5\22\n\2\\]\7" +
                    "\t\2\2]_\3\2\2\2^T\3\2\2\2^W\3\2\2\2^Y\3\2\2\2^Z\3\2\2\2_x\3\2\2\2`a\f" +
                    "\7\2\2ab\t\3\2\2bw\5\22\n\bcd\f\6\2\2de\t\4\2\2ew\5\22\n\7fg\f\5\2\2g" +
                    "h\7\23\2\2hw\5\22\n\6ij\f\n\2\2js\7\b\2\2kp\5\22\n\2lm\7\n\2\2mo\5\22" +
                    "\n\2nl\3\2\2\2or\3\2\2\2pn\3\2\2\2pq\3\2\2\2qt\3\2\2\2rp\3\2\2\2sk\3\2" +
                    "\2\2st\3\2\2\2tu\3\2\2\2uw\7\t\2\2v`\3\2\2\2vc\3\2\2\2vf\3\2\2\2vi\3\2" +
                    "\2\2wz\3\2\2\2xv\3\2\2\2xy\3\2\2\2y\23\3\2\2\2zx\3\2\2\2{\177\7\24\2\2" +
                    "|\177\7\25\2\2}\177\7\26\2\2~{\3\2\2\2~|\3\2\2\2~}\3\2\2\2\177\25\3\2" +
                    "\2\2\20\30\32 *\64>GR^psvx~";
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

    public CymbolParser(TokenStream input) {
        super(input);
        _interp = new ParserATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "file", "varDecl", "type", "functionDecl", "formalParameters", "formalParameter",
                "block", "statatment", "expr", "primary"
        };
    }

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'='", "';'", "'float'", "'int'", "'void'", "'('", "')'", "','",
                "'{'", "'}'", "'return'", "'-'", "'!'", "'*'", "'/'", "'+'", "'=='"
        };
    }

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, "ID", "INT", "FLOAT", "WS", "SLCOMMENT"
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
        return "Cymbol.g4";
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

    public final FileContext file() throws RecognitionException {
        FileContext _localctx = new FileContext(_ctx, getState());
        enterRule(_localctx, 0, RULE_file);
        int _la;
        try {
            _localctx = new CompilationUnitContext(_localctx);
            enterOuterAlt(_localctx, 1);
            {
                setState(22);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        setState(22);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                            case 1: {
                                setState(20);
                                functionDecl();
                            }
                            break;
                            case 2: {
                                setState(21);
                                varDecl();
                            }
                            break;
                        }
                    }
                    setState(24);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4))) != 0));
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

    public final VarDeclContext varDecl() throws RecognitionException {
        VarDeclContext _localctx = new VarDeclContext(_ctx, getState());
        enterRule(_localctx, 2, RULE_varDecl);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(26);
                type();
                setState(27);
                match(ID);
                setState(30);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__0) {
                    {
                        setState(28);
                        match(T__0);
                        setState(29);
                        expr(0);
                    }
                }

                setState(32);
                match(T__1);
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

    public final TypeContext type() throws RecognitionException {
        TypeContext _localctx = new TypeContext(_ctx, getState());
        enterRule(_localctx, 4, RULE_type);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(34);
                _la = _input.LA(1);
                if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4))) != 0))) {
                    _errHandler.recoverInline(this);
                } else {
                    if (_input.LA(1) == Token.EOF) matchedEOF = true;
                    _errHandler.reportMatch(this);
                    consume();
                }
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

    public final FunctionDeclContext functionDecl() throws RecognitionException {
        FunctionDeclContext _localctx = new FunctionDeclContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_functionDecl);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(36);
                type();
                setState(37);
                match(ID);
                setState(38);
                match(T__5);
                setState(40);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4))) != 0)) {
                    {
                        setState(39);
                        formalParameters();
                    }
                }

                setState(42);
                match(T__6);
                setState(43);
                block();
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

    public final FormalParametersContext formalParameters() throws RecognitionException {
        FormalParametersContext _localctx = new FormalParametersContext(_ctx, getState());
        enterRule(_localctx, 8, RULE_formalParameters);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(45);
                formalParameter();
                setState(50);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__7) {
                    {
                        {
                            setState(46);
                            match(T__7);
                            setState(47);
                            formalParameter();
                        }
                    }
                    setState(52);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
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

    public final FormalParameterContext formalParameter() throws RecognitionException {
        FormalParameterContext _localctx = new FormalParameterContext(_ctx, getState());
        enterRule(_localctx, 10, RULE_formalParameter);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(53);
                type();
                setState(54);
                match(ID);
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

    public final BlockContext block() throws RecognitionException {
        BlockContext _localctx = new BlockContext(_ctx, getState());
        enterRule(_localctx, 12, RULE_block);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(56);
                match(T__8);
                setState(60);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__8) | (1L << T__10) | (1L << T__11) | (1L << T__12) | (1L << ID) | (1L << INT) | (1L << FLOAT))) != 0)) {
                    {
                        {
                            setState(57);
                            statatment();
                        }
                    }
                    setState(62);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(63);
                match(T__9);
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

    public final StatatmentContext statatment() throws RecognitionException {
        StatatmentContext _localctx = new StatatmentContext(_ctx, getState());
        enterRule(_localctx, 14, RULE_statatment);
        int _la;
        try {
            setState(80);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 7, _ctx)) {
                case 1:
                    _localctx = new StatBlockContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                {
                    setState(65);
                    block();
                }
                break;
                case 2:
                    _localctx = new StatVarDeclContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                {
                    setState(66);
                    varDecl();
                }
                break;
                case 3:
                    _localctx = new StatReturnContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                {
                    setState(67);
                    match(T__10);
                    setState(69);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__11) | (1L << T__12) | (1L << ID) | (1L << INT) | (1L << FLOAT))) != 0)) {
                        {
                            setState(68);
                            expr(0);
                        }
                    }

                    setState(71);
                    match(T__1);
                }
                break;
                case 4:
                    _localctx = new StatAssignContext(_localctx);
                    enterOuterAlt(_localctx, 4);
                {
                    setState(72);
                    expr(0);
                    setState(73);
                    match(T__0);
                    setState(74);
                    expr(0);
                    setState(75);
                    match(T__1);
                }
                break;
                case 5:
                    _localctx = new StatContext(_localctx);
                    enterOuterAlt(_localctx, 5);
                {
                    setState(77);
                    expr(0);
                    setState(78);
                    match(T__1);
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

    public final ExprContext expr() throws RecognitionException {
        return expr(0);
    }

    private ExprContext expr(int _p) throws RecognitionException {
        ParserRuleContext _parentctx = _ctx;
        int _parentState = getState();
        ExprContext _localctx = new ExprContext(_ctx, _parentState);
        ExprContext _prevctx = _localctx;
        int _startState = 16;
        enterRecursionRule(_localctx, 16, RULE_expr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(92);
                _errHandler.sync(this);
                switch (_input.LA(1)) {
                    case T__11: {
                        _localctx = new ExprUnaryContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(83);
                        match(T__11);
                        setState(84);
                        expr(7);
                    }
                    break;
                    case T__12: {
                        _localctx = new ExprUnaryContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(85);
                        match(T__12);
                        setState(86);
                        expr(6);
                    }
                    break;
                    case ID:
                    case INT:
                    case FLOAT: {
                        _localctx = new ExprPrimaryContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(87);
                        primary();
                    }
                    break;
                    case T__5: {
                        _localctx = new ExprGroupContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(88);
                        match(T__5);
                        setState(89);
                        expr(0);
                        setState(90);
                        match(T__6);
                    }
                    break;
                    default:
                        throw new NoViableAltException(this);
                }
                _ctx.stop = _input.LT(-1);
                setState(118);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 12, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(116);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 11, _ctx)) {
                                case 1: {
                                    _localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(94);
                                    if (!(precpred(_ctx, 5)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                    setState(95);
                                    _la = _input.LA(1);
                                    if (!(_la == T__13 || _la == T__14)) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(96);
                                    expr(6);
                                }
                                break;
                                case 2: {
                                    _localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(97);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(98);
                                    _la = _input.LA(1);
                                    if (!(_la == T__11 || _la == T__15)) {
                                        _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(99);
                                    expr(5);
                                }
                                break;
                                case 3: {
                                    _localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(100);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(101);
                                    match(T__16);
                                    setState(102);
                                    expr(4);
                                }
                                break;
                                case 4: {
                                    _localctx = new ExprFuncCallContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(103);
                                    if (!(precpred(_ctx, 8)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 8)");
                                    setState(104);
                                    match(T__5);
                                    setState(113);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__11) | (1L << T__12) | (1L << ID) | (1L << INT) | (1L << FLOAT))) != 0)) {
                                        {
                                            setState(105);
                                            expr(0);
                                            setState(110);
                                            _errHandler.sync(this);
                                            _la = _input.LA(1);
                                            while (_la == T__7) {
                                                {
                                                    {
                                                        setState(106);
                                                        match(T__7);
                                                        setState(107);
                                                        expr(0);
                                                    }
                                                }
                                                setState(112);
                                                _errHandler.sync(this);
                                                _la = _input.LA(1);
                                            }
                                        }
                                    }

                                    setState(115);
                                    match(T__6);
                                }
                                break;
                            }
                        }
                    }
                    setState(120);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 12, _ctx);
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

    public final PrimaryContext primary() throws RecognitionException {
        PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
        enterRule(_localctx, 18, RULE_primary);
        try {
            setState(124);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case ID:
                    _localctx = new PrimaryIDContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                {
                    setState(121);
                    match(ID);
                }
                break;
                case INT:
                    _localctx = new PrimaryINTContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                {
                    setState(122);
                    match(INT);
                }
                break;
                case FLOAT:
                    _localctx = new PrimaryFLOATContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                {
                    setState(123);
                    match(FLOAT);
                }
                break;
                default:
                    throw new NoViableAltException(this);
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

    public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
        switch (ruleIndex) {
            case 8:
                return expr_sempred((ExprContext) _localctx, predIndex);
        }
        return true;
    }

    private boolean expr_sempred(ExprContext _localctx, int predIndex) {
        switch (predIndex) {
            case 0:
                return precpred(_ctx, 5);
            case 1:
                return precpred(_ctx, 4);
            case 2:
                return precpred(_ctx, 3);
            case 3:
                return precpred(_ctx, 8);
        }
        return true;
    }

    public static class FileContext extends ParserRuleContext {
        public FileContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public FileContext() {
        }

        @Override
        public int getRuleIndex() {
            return RULE_file;
        }

        public void copyFrom(FileContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class CompilationUnitContext extends FileContext {
        public CompilationUnitContext(FileContext ctx) {
            copyFrom(ctx);
        }

        public List<FunctionDeclContext> functionDecl() {
            return getRuleContexts(FunctionDeclContext.class);
        }

        public FunctionDeclContext functionDecl(int i) {
            return getRuleContext(FunctionDeclContext.class, i);
        }

        public List<VarDeclContext> varDecl() {
            return getRuleContexts(VarDeclContext.class);
        }

        public VarDeclContext varDecl(int i) {
            return getRuleContext(VarDeclContext.class, i);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor)
                return ((CymbolVisitor<? extends T>) visitor).visitCompilationUnit(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class VarDeclContext extends ParserRuleContext {
        public VarDeclContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TypeContext type() {
            return getRuleContext(TypeContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(CymbolParser.ID, 0);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_varDecl;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitVarDecl(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class TypeContext extends ParserRuleContext {
        public TypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_type;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitType(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FunctionDeclContext extends ParserRuleContext {
        public FunctionDeclContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TypeContext type() {
            return getRuleContext(TypeContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(CymbolParser.ID, 0);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        public FormalParametersContext formalParameters() {
            return getRuleContext(FormalParametersContext.class, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_functionDecl;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitFunctionDecl(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FormalParametersContext extends ParserRuleContext {
        public FormalParametersContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<FormalParameterContext> formalParameter() {
            return getRuleContexts(FormalParameterContext.class);
        }

        public FormalParameterContext formalParameter(int i) {
            return getRuleContext(FormalParameterContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_formalParameters;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor)
                return ((CymbolVisitor<? extends T>) visitor).visitFormalParameters(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FormalParameterContext extends ParserRuleContext {
        public FormalParameterContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public TypeContext type() {
            return getRuleContext(TypeContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(CymbolParser.ID, 0);
        }

        @Override
        public int getRuleIndex() {
            return RULE_formalParameter;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor)
                return ((CymbolVisitor<? extends T>) visitor).visitFormalParameter(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class BlockContext extends ParserRuleContext {
        public BlockContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public List<StatatmentContext> statatment() {
            return getRuleContexts(StatatmentContext.class);
        }

        public StatatmentContext statatment(int i) {
            return getRuleContext(StatatmentContext.class, i);
        }

        @Override
        public int getRuleIndex() {
            return RULE_block;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitBlock(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class StatatmentContext extends ParserRuleContext {
        public StatatmentContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public StatatmentContext() {
        }

        @Override
        public int getRuleIndex() {
            return RULE_statatment;
        }

        public void copyFrom(StatatmentContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class StatBlockContext extends StatatmentContext {
        public StatBlockContext(StatatmentContext ctx) {
            copyFrom(ctx);
        }

        public BlockContext block() {
            return getRuleContext(BlockContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitStatBlock(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class StatContext extends StatatmentContext {
        public StatContext(StatatmentContext ctx) {
            copyFrom(ctx);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitStat(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class StatVarDeclContext extends StatatmentContext {
        public StatVarDeclContext(StatatmentContext ctx) {
            copyFrom(ctx);
        }

        public VarDeclContext varDecl() {
            return getRuleContext(VarDeclContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitStatVarDecl(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class StatAssignContext extends StatatmentContext {
        public StatAssignContext(StatatmentContext ctx) {
            copyFrom(ctx);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitStatAssign(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class StatReturnContext extends StatatmentContext {
        public StatReturnContext(StatatmentContext ctx) {
            copyFrom(ctx);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitStatReturn(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExprContext extends ParserRuleContext {
        public ExprContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public ExprContext() {
        }

        @Override
        public int getRuleIndex() {
            return RULE_expr;
        }

        public void copyFrom(ExprContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class ExprBinaryContext extends ExprContext {
        public ExprBinaryContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitExprBinary(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExprGroupContext extends ExprContext {
        public ExprGroupContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitExprGroup(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExprUnaryContext extends ExprContext {
        public ExprUnaryContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitExprUnary(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExprPrimaryContext extends ExprContext {
        public ExprPrimaryContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        public PrimaryContext primary() {
            return getRuleContext(PrimaryContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitExprPrimary(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class ExprFuncCallContext extends ExprContext {
        public ExprFuncCallContext(ExprContext ctx) {
            copyFrom(ctx);
        }

        public List<ExprContext> expr() {
            return getRuleContexts(ExprContext.class);
        }

        public ExprContext expr(int i) {
            return getRuleContext(ExprContext.class, i);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitExprFuncCall(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class PrimaryContext extends ParserRuleContext {
        public PrimaryContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public PrimaryContext() {
        }

        @Override
        public int getRuleIndex() {
            return RULE_primary;
        }

        public void copyFrom(PrimaryContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class PrimaryFLOATContext extends PrimaryContext {
        public PrimaryFLOATContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        public TerminalNode FLOAT() {
            return getToken(CymbolParser.FLOAT, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitPrimaryFLOAT(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class PrimaryIDContext extends PrimaryContext {
        public PrimaryIDContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        public TerminalNode ID() {
            return getToken(CymbolParser.ID, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitPrimaryID(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class PrimaryINTContext extends PrimaryContext {
        public PrimaryINTContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        public TerminalNode INT() {
            return getToken(CymbolParser.INT, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitPrimaryINT(this);
            else return visitor.visitChildren(this);
        }
    }
}