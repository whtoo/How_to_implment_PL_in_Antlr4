// Generated from ./parser/Cymbol.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep17.parser;

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
            T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22, T__22 = 23, T__23 = 24,
            T__24 = 25, ID = 26, BOOLEAN = 27, NULL = 28, INT = 29, FLOAT = 30, WS = 31, CHAR = 32,
            STRING = 33, SLCOMMENT = 34;
    public static final int
            RULE_file = 0, RULE_varDecl = 1, RULE_type = 2, RULE_primaryType = 3,
            RULE_functionDecl = 4, RULE_formalParameters = 5, RULE_formalParameter = 6,
            RULE_block = 7, RULE_statetment = 8, RULE_expr = 9, RULE_primary = 10;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3$\u009a\4\2\t\2\4" +
                    "\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t" +
                    "\13\4\f\t\f\3\2\3\2\3\2\6\2\34\n\2\r\2\16\2\35\3\3\3\3\3\3\3\3\5\3$\n" +
                    "\3\3\3\3\3\3\4\3\4\5\4*\n\4\3\5\3\5\3\6\3\6\3\6\3\6\5\6\62\n\6\3\6\3\6" +
                    "\3\6\3\7\3\7\3\7\7\7:\n\7\f\7\16\7=\13\7\3\b\3\b\3\b\3\t\3\t\7\tD\n\t" +
                    "\f\t\16\tG\13\t\3\t\3\t\3\n\3\n\3\n\3\n\5\nO\n\n\3\n\3\n\3\n\3\n\3\n\3" +
                    "\n\3\n\3\n\5\nY\n\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3" +
                    "\n\3\n\5\ni\n\n\3\13\3\13\3\13\3\13\3\13\3\13\7\13q\n\13\f\13\16\13t\13" +
                    "\13\5\13v\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\5\13" +
                    "\u0082\n\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\3\13\7\13\u008d\n" +
                    "\13\f\13\16\13\u0090\13\13\3\f\3\f\3\f\3\f\3\f\3\f\5\f\u0098\n\f\3\f\2" +
                    "\3\24\r\2\4\6\b\n\f\16\20\22\24\26\2\6\3\2\5\7\3\2\23\24\4\2\21\21\25" +
                    "\25\3\2\26\33\2\u00ac\2\33\3\2\2\2\4\37\3\2\2\2\6)\3\2\2\2\b+\3\2\2\2" +
                    "\n-\3\2\2\2\f\66\3\2\2\2\16>\3\2\2\2\20A\3\2\2\2\22h\3\2\2\2\24\u0081" +
                    "\3\2\2\2\26\u0097\3\2\2\2\30\34\5\n\6\2\31\34\5\4\3\2\32\34\5\22\n\2\33" +
                    "\30\3\2\2\2\33\31\3\2\2\2\33\32\3\2\2\2\34\35\3\2\2\2\35\33\3\2\2\2\35" +
                    "\36\3\2\2\2\36\3\3\2\2\2\37 \5\6\4\2 #\7\34\2\2!\"\7\3\2\2\"$\5\24\13" +
                    "\2#!\3\2\2\2#$\3\2\2\2$%\3\2\2\2%&\7\4\2\2&\5\3\2\2\2\'*\5\b\5\2(*\7\34" +
                    "\2\2)\'\3\2\2\2)(\3\2\2\2*\7\3\2\2\2+,\t\2\2\2,\t\3\2\2\2-.\5\6\4\2./" +
                    "\7\34\2\2/\61\7\b\2\2\60\62\5\f\7\2\61\60\3\2\2\2\61\62\3\2\2\2\62\63" +
                    "\3\2\2\2\63\64\7\t\2\2\64\65\5\20\t\2\65\13\3\2\2\2\66;\5\16\b\2\678\7" +
                    "\n\2\28:\5\16\b\29\67\3\2\2\2:=\3\2\2\2;9\3\2\2\2;<\3\2\2\2<\r\3\2\2\2" +
                    "=;\3\2\2\2>?\5\6\4\2?@\7\34\2\2@\17\3\2\2\2AE\7\13\2\2BD\5\22\n\2CB\3" +
                    "\2\2\2DG\3\2\2\2EC\3\2\2\2EF\3\2\2\2FH\3\2\2\2GE\3\2\2\2HI\7\f\2\2I\21" +
                    "\3\2\2\2Ji\5\20\t\2Ki\5\4\3\2LN\7\r\2\2MO\5\24\13\2NM\3\2\2\2NO\3\2\2" +
                    "\2OP\3\2\2\2Pi\7\4\2\2QR\7\16\2\2RS\7\b\2\2ST\5\24\13\2TU\7\t\2\2UX\5" +
                    "\22\n\2VW\7\17\2\2WY\5\22\n\2XV\3\2\2\2XY\3\2\2\2Yi\3\2\2\2Z[\7\20\2\2" +
                    "[\\\7\b\2\2\\]\5\24\13\2]^\7\t\2\2^_\5\22\n\2_i\3\2\2\2`a\5\24\13\2ab" +
                    "\7\3\2\2bc\5\24\13\2cd\7\4\2\2di\3\2\2\2ef\5\24\13\2fg\7\4\2\2gi\3\2\2" +
                    "\2hJ\3\2\2\2hK\3\2\2\2hL\3\2\2\2hQ\3\2\2\2hZ\3\2\2\2h`\3\2\2\2he\3\2\2" +
                    "\2i\23\3\2\2\2jk\b\13\1\2kl\7\34\2\2lu\7\b\2\2mr\5\24\13\2no\7\n\2\2o" +
                    "q\5\24\13\2pn\3\2\2\2qt\3\2\2\2rp\3\2\2\2rs\3\2\2\2sv\3\2\2\2tr\3\2\2" +
                    "\2um\3\2\2\2uv\3\2\2\2vw\3\2\2\2w\u0082\7\t\2\2xy\7\21\2\2y\u0082\5\24" +
                    "\13\tz{\7\22\2\2{\u0082\5\24\13\b|\u0082\5\26\f\2}~\7\b\2\2~\177\5\24" +
                    "\13\2\177\u0080\7\t\2\2\u0080\u0082\3\2\2\2\u0081j\3\2\2\2\u0081x\3\2" +
                    "\2\2\u0081z\3\2\2\2\u0081|\3\2\2\2\u0081}\3\2\2\2\u0082\u008e\3\2\2\2" +
                    "\u0083\u0084\f\7\2\2\u0084\u0085\t\3\2\2\u0085\u008d\5\24\13\b\u0086\u0087" +
                    "\f\6\2\2\u0087\u0088\t\4\2\2\u0088\u008d\5\24\13\7\u0089\u008a\f\5\2\2" +
                    "\u008a\u008b\t\5\2\2\u008b\u008d\5\24\13\6\u008c\u0083\3\2\2\2\u008c\u0086" +
                    "\3\2\2\2\u008c\u0089\3\2\2\2\u008d\u0090\3\2\2\2\u008e\u008c\3\2\2\2\u008e" +
                    "\u008f\3\2\2\2\u008f\25\3\2\2\2\u0090\u008e\3\2\2\2\u0091\u0098\7\34\2" +
                    "\2\u0092\u0098\7\37\2\2\u0093\u0098\7 \2\2\u0094\u0098\7\"\2\2\u0095\u0098" +
                    "\7#\2\2\u0096\u0098\7\35\2\2\u0097\u0091\3\2\2\2\u0097\u0092\3\2\2\2\u0097" +
                    "\u0093\3\2\2\2\u0097\u0094\3\2\2\2\u0097\u0095\3\2\2\2\u0097\u0096\3\2" +
                    "\2\2\u0098\27\3\2\2\2\22\33\35#)\61;ENXhru\u0081\u008c\u008e\u0097";
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
                "file", "varDecl", "type", "primaryType", "functionDecl", "formalParameters",
                "formalParameter", "block", "statetment", "expr", "primary"
        };
    }

    private static String[] makeLiteralNames() {
        return new String[]{
                null, "'='", "';'", "'float'", "'int'", "'void'", "'('", "')'", "','",
                "'{'", "'}'", "'return'", "'if'", "'else'", "'while'", "'-'", "'!'",
                "'*'", "'/'", "'+'", "'=='", "'!='", "'>'", "'>='", "'<'", "'<='", null,
                null, "'null'"
        };
    }

    private static String[] makeSymbolicNames() {
        return new String[]{
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, "ID", "BOOLEAN", "NULL", "INT", "FLOAT", "WS", "CHAR", "STRING",
                "SLCOMMENT"
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
                setState(25);
                _errHandler.sync(this);
                _la = _input.LA(1);
                do {
                    {
                        setState(25);
                        _errHandler.sync(this);
                        switch (getInterpreter().adaptivePredict(_input, 0, _ctx)) {
                            case 1: {
                                setState(22);
                                functionDecl();
                            }
                            break;
                            case 2: {
                                setState(23);
                                varDecl();
                            }
                            break;
                            case 3: {
                                setState(24);
                                statetment();
                            }
                            break;
                        }
                    }
                    setState(27);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                } while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__8) | (1L << T__10) | (1L << T__11) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << ID) | (1L << BOOLEAN) | (1L << INT) | (1L << FLOAT) | (1L << CHAR) | (1L << STRING))) != 0));
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
                setState(29);
                type();
                setState(30);
                match(ID);
                setState(33);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if (_la == T__0) {
                    {
                        setState(31);
                        match(T__0);
                        setState(32);
                        expr(0);
                    }
                }

                setState(35);
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
        try {
            setState(39);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case T__2:
                case T__3:
                case T__4:
                    enterOuterAlt(_localctx, 1);
                {
                    setState(37);
                    primaryType();
                }
                break;
                case ID:
                    enterOuterAlt(_localctx, 2);
                {
                    setState(38);
                    match(ID);
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

    public final PrimaryTypeContext primaryType() throws RecognitionException {
        PrimaryTypeContext _localctx = new PrimaryTypeContext(_ctx, getState());
        enterRule(_localctx, 6, RULE_primaryType);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(41);
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
        enterRule(_localctx, 8, RULE_functionDecl);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(43);
                ((FunctionDeclContext) _localctx).retType = type();
                setState(44);
                ((FunctionDeclContext) _localctx).funcName = match(ID);
                setState(45);
                match(T__5);
                setState(47);
                _errHandler.sync(this);
                _la = _input.LA(1);
                if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << ID))) != 0)) {
                    {
                        setState(46);
                        ((FunctionDeclContext) _localctx).params = formalParameters();
                    }
                }

                setState(49);
                match(T__6);
                setState(50);
                ((FunctionDeclContext) _localctx).blockDef = block();
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
        enterRule(_localctx, 10, RULE_formalParameters);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(52);
                formalParameter();
                setState(57);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while (_la == T__7) {
                    {
                        {
                            setState(53);
                            match(T__7);
                            setState(54);
                            formalParameter();
                        }
                    }
                    setState(59);
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
        enterRule(_localctx, 12, RULE_formalParameter);
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(60);
                type();
                setState(61);
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
        enterRule(_localctx, 14, RULE_block);
        int _la;
        try {
            enterOuterAlt(_localctx, 1);
            {
                setState(63);
                match(T__8);
                setState(67);
                _errHandler.sync(this);
                _la = _input.LA(1);
                while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__8) | (1L << T__10) | (1L << T__11) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << ID) | (1L << BOOLEAN) | (1L << INT) | (1L << FLOAT) | (1L << CHAR) | (1L << STRING))) != 0)) {
                    {
                        {
                            setState(64);
                            statetment();
                        }
                    }
                    setState(69);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                }
                setState(70);
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

    public final StatetmentContext statetment() throws RecognitionException {
        StatetmentContext _localctx = new StatetmentContext(_ctx, getState());
        enterRule(_localctx, 16, RULE_statetment);
        int _la;
        try {
            setState(102);
            _errHandler.sync(this);
            switch (getInterpreter().adaptivePredict(_input, 9, _ctx)) {
                case 1:
                    _localctx = new StatBlockContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                {
                    setState(72);
                    block();
                }
                break;
                case 2:
                    _localctx = new StatVarDeclContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                {
                    setState(73);
                    varDecl();
                }
                break;
                case 3:
                    _localctx = new StatReturnContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                {
                    setState(74);
                    match(T__10);
                    setState(76);
                    _errHandler.sync(this);
                    _la = _input.LA(1);
                    if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__14) | (1L << T__15) | (1L << ID) | (1L << BOOLEAN) | (1L << INT) | (1L << FLOAT) | (1L << CHAR) | (1L << STRING))) != 0)) {
                        {
                            setState(75);
                            expr(0);
                        }
                    }

                    setState(78);
                    match(T__1);
                }
                break;
                case 4:
                    _localctx = new StateConditionContext(_localctx);
                    enterOuterAlt(_localctx, 4);
                {
                    setState(79);
                    match(T__11);
                    setState(80);
                    match(T__5);
                    setState(81);
                    ((StateConditionContext) _localctx).cond = expr(0);
                    setState(82);
                    match(T__6);
                    setState(83);
                    ((StateConditionContext) _localctx).then = statetment();
                    setState(86);
                    _errHandler.sync(this);
                    switch (getInterpreter().adaptivePredict(_input, 8, _ctx)) {
                        case 1: {
                            setState(84);
                            match(T__12);
                            setState(85);
                            ((StateConditionContext) _localctx).elseDo = statetment();
                        }
                        break;
                    }
                }
                break;
                case 5:
                    _localctx = new StateWhileContext(_localctx);
                    enterOuterAlt(_localctx, 5);
                {
                    setState(88);
                    match(T__13);
                    setState(89);
                    match(T__5);
                    setState(90);
                    ((StateWhileContext) _localctx).cond = expr(0);
                    setState(91);
                    match(T__6);
                    setState(92);
                    ((StateWhileContext) _localctx).then = statetment();
                }
                break;
                case 6:
                    _localctx = new StatAssignContext(_localctx);
                    enterOuterAlt(_localctx, 6);
                {
                    setState(94);
                    expr(0);
                    setState(95);
                    match(T__0);
                    setState(96);
                    expr(0);
                    setState(97);
                    match(T__1);
                }
                break;
                case 7:
                    _localctx = new StatContext(_localctx);
                    enterOuterAlt(_localctx, 7);
                {
                    setState(99);
                    expr(0);
                    setState(100);
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
        int _startState = 18;
        enterRecursionRule(_localctx, 18, RULE_expr, _p);
        int _la;
        try {
            int _alt;
            enterOuterAlt(_localctx, 1);
            {
                setState(127);
                _errHandler.sync(this);
                switch (getInterpreter().adaptivePredict(_input, 12, _ctx)) {
                    case 1: {
                        _localctx = new ExprFuncCallContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;

                        setState(105);
                        match(ID);
                        setState(106);
                        match(T__5);
                        setState(115);
                        _errHandler.sync(this);
                        _la = _input.LA(1);
                        if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__14) | (1L << T__15) | (1L << ID) | (1L << BOOLEAN) | (1L << INT) | (1L << FLOAT) | (1L << CHAR) | (1L << STRING))) != 0)) {
                            {
                                setState(107);
                                expr(0);
                                setState(112);
                                _errHandler.sync(this);
                                _la = _input.LA(1);
                                while (_la == T__7) {
                                    {
                                        {
                                            setState(108);
                                            match(T__7);
                                            setState(109);
                                            expr(0);
                                        }
                                    }
                                    setState(114);
                                    _errHandler.sync(this);
                                    _la = _input.LA(1);
                                }
                            }
                        }

                        setState(117);
                        match(T__6);
                    }
                    break;
                    case 2: {
                        _localctx = new ExprUnaryContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(118);
                        match(T__14);
                        setState(119);
                        expr(7);
                    }
                    break;
                    case 3: {
                        _localctx = new ExprUnaryContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(120);
                        match(T__15);
                        setState(121);
                        expr(6);
                    }
                    break;
                    case 4: {
                        _localctx = new ExprPrimaryContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(122);
                        primary();
                    }
                    break;
                    case 5: {
                        _localctx = new ExprGroupContext(_localctx);
                        _ctx = _localctx;
                        _prevctx = _localctx;
                        setState(123);
                        match(T__5);
                        setState(124);
                        expr(0);
                        setState(125);
                        match(T__6);
                    }
                    break;
                }
                _ctx.stop = _input.LT(-1);
                setState(140);
                _errHandler.sync(this);
                _alt = getInterpreter().adaptivePredict(_input, 14, _ctx);
                while (_alt != 2 && _alt != org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER) {
                    if (_alt == 1) {
                        if (_parseListeners != null) triggerExitRuleEvent();
                        _prevctx = _localctx;
                        {
                            setState(138);
                            _errHandler.sync(this);
                            switch (getInterpreter().adaptivePredict(_input, 13, _ctx)) {
                                case 1: {
                                    _localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(129);
                                    if (!(precpred(_ctx, 5)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 5)");
                                    setState(130);
                                    ((ExprBinaryContext) _localctx).o = _input.LT(1);
                                    _la = _input.LA(1);
                                    if (!(_la == T__16 || _la == T__17)) {
                                        ((ExprBinaryContext) _localctx).o = (Token) _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(131);
                                    expr(6);
                                }
                                break;
                                case 2: {
                                    _localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(132);
                                    if (!(precpred(_ctx, 4)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 4)");
                                    setState(133);
                                    ((ExprBinaryContext) _localctx).o = _input.LT(1);
                                    _la = _input.LA(1);
                                    if (!(_la == T__14 || _la == T__18)) {
                                        ((ExprBinaryContext) _localctx).o = (Token) _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(134);
                                    expr(5);
                                }
                                break;
                                case 3: {
                                    _localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
                                    pushNewRecursionContext(_localctx, _startState, RULE_expr);
                                    setState(135);
                                    if (!(precpred(_ctx, 3)))
                                        throw new FailedPredicateException(this, "precpred(_ctx, 3)");
                                    setState(136);
                                    ((ExprBinaryContext) _localctx).o = _input.LT(1);
                                    _la = _input.LA(1);
                                    if (!((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__19) | (1L << T__20) | (1L << T__21) | (1L << T__22) | (1L << T__23) | (1L << T__24))) != 0))) {
                                        ((ExprBinaryContext) _localctx).o = (Token) _errHandler.recoverInline(this);
                                    } else {
                                        if (_input.LA(1) == Token.EOF) matchedEOF = true;
                                        _errHandler.reportMatch(this);
                                        consume();
                                    }
                                    setState(137);
                                    expr(4);
                                }
                                break;
                            }
                        }
                    }
                    setState(142);
                    _errHandler.sync(this);
                    _alt = getInterpreter().adaptivePredict(_input, 14, _ctx);
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
        enterRule(_localctx, 20, RULE_primary);
        try {
            setState(149);
            _errHandler.sync(this);
            switch (_input.LA(1)) {
                case ID:
                    _localctx = new PrimaryIDContext(_localctx);
                    enterOuterAlt(_localctx, 1);
                {
                    setState(143);
                    match(ID);
                }
                break;
                case INT:
                    _localctx = new PrimaryINTContext(_localctx);
                    enterOuterAlt(_localctx, 2);
                {
                    setState(144);
                    match(INT);
                }
                break;
                case FLOAT:
                    _localctx = new PrimaryFLOATContext(_localctx);
                    enterOuterAlt(_localctx, 3);
                {
                    setState(145);
                    match(FLOAT);
                }
                break;
                case CHAR:
                    _localctx = new PrimaryCHARContext(_localctx);
                    enterOuterAlt(_localctx, 4);
                {
                    setState(146);
                    match(CHAR);
                }
                break;
                case STRING:
                    _localctx = new PrimarySTRINGContext(_localctx);
                    enterOuterAlt(_localctx, 5);
                {
                    setState(147);
                    match(STRING);
                }
                break;
                case BOOLEAN:
                    _localctx = new PrimaryBOOLContext(_localctx);
                    enterOuterAlt(_localctx, 6);
                {
                    setState(148);
                    match(BOOLEAN);
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
            case 9:
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

        public List<StatetmentContext> statetment() {
            return getRuleContexts(StatetmentContext.class);
        }

        public StatetmentContext statetment(int i) {
            return getRuleContext(StatetmentContext.class, i);
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

        public PrimaryTypeContext primaryType() {
            return getRuleContext(PrimaryTypeContext.class, 0);
        }

        public TerminalNode ID() {
            return getToken(CymbolParser.ID, 0);
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

    public static class PrimaryTypeContext extends ParserRuleContext {
        public PrimaryTypeContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        @Override
        public int getRuleIndex() {
            return RULE_primaryType;
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitPrimaryType(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class FunctionDeclContext extends ParserRuleContext {
        public TypeContext retType;
        public Token funcName;
        public FormalParametersContext params;
        public BlockContext blockDef;

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

        public List<StatetmentContext> statetment() {
            return getRuleContexts(StatetmentContext.class);
        }

        public StatetmentContext statetment(int i) {
            return getRuleContext(StatetmentContext.class, i);
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

    public static class StatetmentContext extends ParserRuleContext {
        public StatetmentContext(ParserRuleContext parent, int invokingState) {
            super(parent, invokingState);
        }

        public StatetmentContext() {
        }

        @Override
        public int getRuleIndex() {
            return RULE_statetment;
        }

        public void copyFrom(StatetmentContext ctx) {
            super.copyFrom(ctx);
        }
    }

    public static class StatBlockContext extends StatetmentContext {
        public StatBlockContext(StatetmentContext ctx) {
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

    public static class StatContext extends StatetmentContext {
        public StatContext(StatetmentContext ctx) {
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

    public static class StatVarDeclContext extends StatetmentContext {
        public StatVarDeclContext(StatetmentContext ctx) {
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

    public static class StateWhileContext extends StatetmentContext {
        public ExprContext cond;
        public StatetmentContext then;

        public StateWhileContext(StatetmentContext ctx) {
            copyFrom(ctx);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public StatetmentContext statetment() {
            return getRuleContext(StatetmentContext.class, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitStateWhile(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class StatAssignContext extends StatetmentContext {
        public StatAssignContext(StatetmentContext ctx) {
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

    public static class StatReturnContext extends StatetmentContext {
        public StatReturnContext(StatetmentContext ctx) {
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

    public static class StateConditionContext extends StatetmentContext {
        public ExprContext cond;
        public StatetmentContext then;
        public StatetmentContext elseDo;

        public StateConditionContext(StatetmentContext ctx) {
            copyFrom(ctx);
        }

        public ExprContext expr() {
            return getRuleContext(ExprContext.class, 0);
        }

        public List<StatetmentContext> statetment() {
            return getRuleContexts(StatetmentContext.class);
        }

        public StatetmentContext statetment(int i) {
            return getRuleContext(StatetmentContext.class, i);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor)
                return ((CymbolVisitor<? extends T>) visitor).visitStateCondition(this);
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
        public Token o;

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

        public TerminalNode ID() {
            return getToken(CymbolParser.ID, 0);
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

    public static class PrimaryCHARContext extends PrimaryContext {
        public PrimaryCHARContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        public TerminalNode CHAR() {
            return getToken(CymbolParser.CHAR, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitPrimaryCHAR(this);
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

    public static class PrimarySTRINGContext extends PrimaryContext {
        public PrimarySTRINGContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        public TerminalNode STRING() {
            return getToken(CymbolParser.STRING, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor)
                return ((CymbolVisitor<? extends T>) visitor).visitPrimarySTRING(this);
            else return visitor.visitChildren(this);
        }
    }

    public static class PrimaryBOOLContext extends PrimaryContext {
        public PrimaryBOOLContext(PrimaryContext ctx) {
            copyFrom(ctx);
        }

        public TerminalNode BOOLEAN() {
            return getToken(CymbolParser.BOOLEAN, 0);
        }

        @Override
        public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
            if (visitor instanceof CymbolVisitor) return ((CymbolVisitor<? extends T>) visitor).visitPrimaryBOOL(this);
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