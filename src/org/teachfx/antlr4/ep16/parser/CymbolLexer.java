// Generated from ./parser/Cymbol.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep16.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CymbolLexer extends Lexer {
    public static final int
            T__0 = 1, T__1 = 2, T__2 = 3, T__3 = 4, T__4 = 5, T__5 = 6, T__6 = 7, T__7 = 8, T__8 = 9,
            T__9 = 10, T__10 = 11, T__11 = 12, T__12 = 13, T__13 = 14, T__14 = 15, T__15 = 16, T__16 = 17,
            T__17 = 18, T__18 = 19, T__19 = 20, T__20 = 21, T__21 = 22, T__22 = 23, T__23 = 24,
            T__24 = 25, ID = 26, BOOLEAN = 27, NULL = 28, INT = 29, FLOAT = 30, WS = 31, CHAR = 32,
            STRING = 33, SLCOMMENT = 34;
    public static final String[] ruleNames = makeRuleNames();
    /**
     * @deprecated Use {@link #VOCABULARY} instead.
     */
    @Deprecated
    public static final String[] tokenNames;
    public static final String _serializedATN =
            "\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2$\u00db\b\1\4\2\t" +
                    "\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13" +
                    "\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22" +
                    "\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31" +
                    "\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!" +
                    "\t!\4\"\t\"\4#\t#\4$\t$\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3" +
                    "\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3" +
                    "\13\3\f\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3" +
                    "\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3" +
                    "\24\3\24\3\25\3\25\3\25\3\26\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\31\3" +
                    "\31\3\32\3\32\3\32\3\33\3\33\3\33\7\33\u0099\n\33\f\33\16\33\u009c\13" +
                    "\33\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\3\34\5\34\u00a7\n\34\3\35" +
                    "\3\35\3\35\3\35\3\35\3\36\3\36\3\37\6\37\u00b1\n\37\r\37\16\37\u00b2\3" +
                    " \5 \u00b6\n \3 \3 \3 \3!\6!\u00bc\n!\r!\16!\u00bd\3!\3!\3\"\3\"\3\"\3" +
                    "\"\3#\3#\7#\u00c8\n#\f#\16#\u00cb\13#\3#\3#\3$\3$\3$\3$\7$\u00d3\n$\f" +
                    "$\16$\u00d6\13$\3$\3$\3$\3$\3\u00d4\2%\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21" +
                    "\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30" +
                    "/\31\61\32\63\33\65\34\67\359\36;\2=\37? A!C\"E#G$\3\2\6\3\2\62;\4\2C" +
                    "\\c|\5\2\13\f\17\17\"\"\5\2\f\f\17\17$$\2\u00e1\2\3\3\2\2\2\2\5\3\2\2" +
                    "\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21" +
                    "\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2" +
                    "\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3" +
                    "\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3" +
                    "\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3" +
                    "\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2\3I\3\2\2\2\5K\3\2\2\2\7M\3\2\2" +
                    "\2\tS\3\2\2\2\13W\3\2\2\2\r\\\3\2\2\2\17^\3\2\2\2\21`\3\2\2\2\23b\3\2" +
                    "\2\2\25d\3\2\2\2\27f\3\2\2\2\31m\3\2\2\2\33p\3\2\2\2\35u\3\2\2\2\37{\3" +
                    "\2\2\2!}\3\2\2\2#\177\3\2\2\2%\u0081\3\2\2\2\'\u0083\3\2\2\2)\u0085\3" +
                    "\2\2\2+\u0088\3\2\2\2-\u008b\3\2\2\2/\u008d\3\2\2\2\61\u0090\3\2\2\2\63" +
                    "\u0092\3\2\2\2\65\u0095\3\2\2\2\67\u00a6\3\2\2\29\u00a8\3\2\2\2;\u00ad" +
                    "\3\2\2\2=\u00b0\3\2\2\2?\u00b5\3\2\2\2A\u00bb\3\2\2\2C\u00c1\3\2\2\2E" +
                    "\u00c5\3\2\2\2G\u00ce\3\2\2\2IJ\7?\2\2J\4\3\2\2\2KL\7=\2\2L\6\3\2\2\2" +
                    "MN\7h\2\2NO\7n\2\2OP\7q\2\2PQ\7c\2\2QR\7v\2\2R\b\3\2\2\2ST\7k\2\2TU\7" +
                    "p\2\2UV\7v\2\2V\n\3\2\2\2WX\7x\2\2XY\7q\2\2YZ\7k\2\2Z[\7f\2\2[\f\3\2\2" +
                    "\2\\]\7*\2\2]\16\3\2\2\2^_\7+\2\2_\20\3\2\2\2`a\7.\2\2a\22\3\2\2\2bc\7" +
                    "}\2\2c\24\3\2\2\2de\7\177\2\2e\26\3\2\2\2fg\7t\2\2gh\7g\2\2hi\7v\2\2i" +
                    "j\7w\2\2jk\7t\2\2kl\7p\2\2l\30\3\2\2\2mn\7k\2\2no\7h\2\2o\32\3\2\2\2p" +
                    "q\7g\2\2qr\7n\2\2rs\7u\2\2st\7g\2\2t\34\3\2\2\2uv\7y\2\2vw\7j\2\2wx\7" +
                    "k\2\2xy\7n\2\2yz\7g\2\2z\36\3\2\2\2{|\7/\2\2| \3\2\2\2}~\7#\2\2~\"\3\2" +
                    "\2\2\177\u0080\7,\2\2\u0080$\3\2\2\2\u0081\u0082\7\61\2\2\u0082&\3\2\2" +
                    "\2\u0083\u0084\7-\2\2\u0084(\3\2\2\2\u0085\u0086\7?\2\2\u0086\u0087\7" +
                    "?\2\2\u0087*\3\2\2\2\u0088\u0089\7#\2\2\u0089\u008a\7?\2\2\u008a,\3\2" +
                    "\2\2\u008b\u008c\7@\2\2\u008c.\3\2\2\2\u008d\u008e\7@\2\2\u008e\u008f" +
                    "\7?\2\2\u008f\60\3\2\2\2\u0090\u0091\7>\2\2\u0091\62\3\2\2\2\u0092\u0093" +
                    "\7>\2\2\u0093\u0094\7?\2\2\u0094\64\3\2\2\2\u0095\u009a\5;\36\2\u0096" +
                    "\u0099\5;\36\2\u0097\u0099\t\2\2\2\u0098\u0096\3\2\2\2\u0098\u0097\3\2" +
                    "\2\2\u0099\u009c\3\2\2\2\u009a\u0098\3\2\2\2\u009a\u009b\3\2\2\2\u009b" +
                    "\66\3\2\2\2\u009c\u009a\3\2\2\2\u009d\u009e\7v\2\2\u009e\u009f\7t\2\2" +
                    "\u009f\u00a0\7w\2\2\u00a0\u00a7\7g\2\2\u00a1\u00a2\7h\2\2\u00a2\u00a3" +
                    "\7c\2\2\u00a3\u00a4\7n\2\2\u00a4\u00a5\7u\2\2\u00a5\u00a7\7g\2\2\u00a6" +
                    "\u009d\3\2\2\2\u00a6\u00a1\3\2\2\2\u00a78\3\2\2\2\u00a8\u00a9\7p\2\2\u00a9" +
                    "\u00aa\7w\2\2\u00aa\u00ab\7n\2\2\u00ab\u00ac\7n\2\2\u00ac:\3\2\2\2\u00ad" +
                    "\u00ae\t\3\2\2\u00ae<\3\2\2\2\u00af\u00b1\t\2\2\2\u00b0\u00af\3\2\2\2" +
                    "\u00b1\u00b2\3\2\2\2\u00b2\u00b0\3\2\2\2\u00b2\u00b3\3\2\2\2\u00b3>\3" +
                    "\2\2\2\u00b4\u00b6\5=\37\2\u00b5\u00b4\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6" +
                    "\u00b7\3\2\2\2\u00b7\u00b8\7\60\2\2\u00b8\u00b9\5=\37\2\u00b9@\3\2\2\2" +
                    "\u00ba\u00bc\t\4\2\2\u00bb\u00ba\3\2\2\2\u00bc\u00bd\3\2\2\2\u00bd\u00bb" +
                    "\3\2\2\2\u00bd\u00be\3\2\2\2\u00be\u00bf\3\2\2\2\u00bf\u00c0\b!\2\2\u00c0" +
                    "B\3\2\2\2\u00c1\u00c2\7)\2\2\u00c2\u00c3\13\2\2\2\u00c3\u00c4\7)\2\2\u00c4" +
                    "D\3\2\2\2\u00c5\u00c9\7$\2\2\u00c6\u00c8\n\5\2\2\u00c7\u00c6\3\2\2\2\u00c8" +
                    "\u00cb\3\2\2\2\u00c9\u00c7\3\2\2\2\u00c9\u00ca\3\2\2\2\u00ca\u00cc\3\2" +
                    "\2\2\u00cb\u00c9\3\2\2\2\u00cc\u00cd\7$\2\2\u00cdF\3\2\2\2\u00ce\u00cf" +
                    "\7\61\2\2\u00cf\u00d0\7\61\2\2\u00d0\u00d4\3\2\2\2\u00d1\u00d3\13\2\2" +
                    "\2\u00d2\u00d1\3\2\2\2\u00d3\u00d6\3\2\2\2\u00d4\u00d5\3\2\2\2\u00d4\u00d2" +
                    "\3\2\2\2\u00d5\u00d7\3\2\2\2\u00d6\u00d4\3\2\2\2\u00d7\u00d8\7\f\2\2\u00d8" +
                    "\u00d9\3\2\2\2\u00d9\u00da\b$\2\2\u00daH\3\2\2\2\13\2\u0098\u009a\u00a6" +
                    "\u00b2\u00b5\u00bd\u00c9\u00d4\3\b\2\2";
    public static final ATN _ATN =
            new ATNDeserializer().deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    private static final String[] _LITERAL_NAMES = makeLiteralNames();
    private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
    public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);
    public static String[] channelNames = {
            "DEFAULT_TOKEN_CHANNEL", "HIDDEN"
    };
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

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

    public CymbolLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    private static String[] makeRuleNames() {
        return new String[]{
                "T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
                "T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16",
                "T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24",
                "ID", "BOOLEAN", "NULL", "LETTER", "INT", "FLOAT", "WS", "CHAR", "STRING",
                "SLCOMMENT"
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
    public String[] getChannelNames() {
        return channelNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }
}