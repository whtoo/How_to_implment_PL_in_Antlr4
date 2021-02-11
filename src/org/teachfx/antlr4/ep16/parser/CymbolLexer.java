// Generated from ./parser/Cymbol.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep16.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CymbolLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		ID=25, BOOLEAN=26, NULL=27, INT=28, FLOAT=29, WS=30, CHAR=31, STRING=32, 
		SLCOMMENT=33;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "T__15", "T__16", 
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "ID", 
			"BOOLEAN", "NULL", "LETTER", "INT", "FLOAT", "WS", "CHAR", "STRING", 
			"SLCOMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "';'", "'float'", "'int'", "'void'", "'('", "')'", "','", 
			"'{'", "'}'", "'return'", "'if'", "'else'", "'-'", "'!'", "'*'", "'/'", 
			"'+'", "'=='", "'!='", "'>'", "'>='", "'<'", "'<='", null, null, "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, "ID", "BOOLEAN", "NULL", "INT", "FLOAT", "WS", "CHAR", "STRING", 
			"SLCOMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
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

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}


	public CymbolLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Cymbol.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2#\u00d3\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\3\2\3\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5"+
		"\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f"+
		"\3\f\3\f\3\f\3\f\3\f\3\f\3\r\3\r\3\r\3\16\3\16\3\16\3\16\3\16\3\17\3\17"+
		"\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\3\24\3\24\3\25\3\25\3\25"+
		"\3\26\3\26\3\27\3\27\3\27\3\30\3\30\3\31\3\31\3\31\3\32\3\32\3\32\7\32"+
		"\u0091\n\32\f\32\16\32\u0094\13\32\3\33\3\33\3\33\3\33\3\33\3\33\3\33"+
		"\3\33\3\33\5\33\u009f\n\33\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\36\6\36"+
		"\u00a9\n\36\r\36\16\36\u00aa\3\37\5\37\u00ae\n\37\3\37\3\37\3\37\3 \6"+
		" \u00b4\n \r \16 \u00b5\3 \3 \3!\3!\3!\3!\3\"\3\"\7\"\u00c0\n\"\f\"\16"+
		"\"\u00c3\13\"\3\"\3\"\3#\3#\3#\3#\7#\u00cb\n#\f#\16#\u00ce\13#\3#\3#\3"+
		"#\3#\3\u00cc\2$\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\16\33\17\35\20\37\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65"+
		"\34\67\359\2;\36=\37? A!C\"E#\3\2\6\3\2\62;\4\2C\\c|\5\2\13\f\17\17\""+
		"\"\5\2\f\f\17\17$$\2\u00d9\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2"+
		"\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2"+
		"\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3"+
		"\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2"+
		"\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67"+
		"\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2"+
		"\2\2\3G\3\2\2\2\5I\3\2\2\2\7K\3\2\2\2\tQ\3\2\2\2\13U\3\2\2\2\rZ\3\2\2"+
		"\2\17\\\3\2\2\2\21^\3\2\2\2\23`\3\2\2\2\25b\3\2\2\2\27d\3\2\2\2\31k\3"+
		"\2\2\2\33n\3\2\2\2\35s\3\2\2\2\37u\3\2\2\2!w\3\2\2\2#y\3\2\2\2%{\3\2\2"+
		"\2\'}\3\2\2\2)\u0080\3\2\2\2+\u0083\3\2\2\2-\u0085\3\2\2\2/\u0088\3\2"+
		"\2\2\61\u008a\3\2\2\2\63\u008d\3\2\2\2\65\u009e\3\2\2\2\67\u00a0\3\2\2"+
		"\29\u00a5\3\2\2\2;\u00a8\3\2\2\2=\u00ad\3\2\2\2?\u00b3\3\2\2\2A\u00b9"+
		"\3\2\2\2C\u00bd\3\2\2\2E\u00c6\3\2\2\2GH\7?\2\2H\4\3\2\2\2IJ\7=\2\2J\6"+
		"\3\2\2\2KL\7h\2\2LM\7n\2\2MN\7q\2\2NO\7c\2\2OP\7v\2\2P\b\3\2\2\2QR\7k"+
		"\2\2RS\7p\2\2ST\7v\2\2T\n\3\2\2\2UV\7x\2\2VW\7q\2\2WX\7k\2\2XY\7f\2\2"+
		"Y\f\3\2\2\2Z[\7*\2\2[\16\3\2\2\2\\]\7+\2\2]\20\3\2\2\2^_\7.\2\2_\22\3"+
		"\2\2\2`a\7}\2\2a\24\3\2\2\2bc\7\177\2\2c\26\3\2\2\2de\7t\2\2ef\7g\2\2"+
		"fg\7v\2\2gh\7w\2\2hi\7t\2\2ij\7p\2\2j\30\3\2\2\2kl\7k\2\2lm\7h\2\2m\32"+
		"\3\2\2\2no\7g\2\2op\7n\2\2pq\7u\2\2qr\7g\2\2r\34\3\2\2\2st\7/\2\2t\36"+
		"\3\2\2\2uv\7#\2\2v \3\2\2\2wx\7,\2\2x\"\3\2\2\2yz\7\61\2\2z$\3\2\2\2{"+
		"|\7-\2\2|&\3\2\2\2}~\7?\2\2~\177\7?\2\2\177(\3\2\2\2\u0080\u0081\7#\2"+
		"\2\u0081\u0082\7?\2\2\u0082*\3\2\2\2\u0083\u0084\7@\2\2\u0084,\3\2\2\2"+
		"\u0085\u0086\7@\2\2\u0086\u0087\7?\2\2\u0087.\3\2\2\2\u0088\u0089\7>\2"+
		"\2\u0089\60\3\2\2\2\u008a\u008b\7>\2\2\u008b\u008c\7?\2\2\u008c\62\3\2"+
		"\2\2\u008d\u0092\59\35\2\u008e\u0091\59\35\2\u008f\u0091\t\2\2\2\u0090"+
		"\u008e\3\2\2\2\u0090\u008f\3\2\2\2\u0091\u0094\3\2\2\2\u0092\u0090\3\2"+
		"\2\2\u0092\u0093\3\2\2\2\u0093\64\3\2\2\2\u0094\u0092\3\2\2\2\u0095\u0096"+
		"\7v\2\2\u0096\u0097\7t\2\2\u0097\u0098\7w\2\2\u0098\u009f\7g\2\2\u0099"+
		"\u009a\7h\2\2\u009a\u009b\7c\2\2\u009b\u009c\7n\2\2\u009c\u009d\7u\2\2"+
		"\u009d\u009f\7g\2\2\u009e\u0095\3\2\2\2\u009e\u0099\3\2\2\2\u009f\66\3"+
		"\2\2\2\u00a0\u00a1\7p\2\2\u00a1\u00a2\7w\2\2\u00a2\u00a3\7n\2\2\u00a3"+
		"\u00a4\7n\2\2\u00a48\3\2\2\2\u00a5\u00a6\t\3\2\2\u00a6:\3\2\2\2\u00a7"+
		"\u00a9\t\2\2\2\u00a8\u00a7\3\2\2\2\u00a9\u00aa\3\2\2\2\u00aa\u00a8\3\2"+
		"\2\2\u00aa\u00ab\3\2\2\2\u00ab<\3\2\2\2\u00ac\u00ae\5;\36\2\u00ad\u00ac"+
		"\3\2\2\2\u00ad\u00ae\3\2\2\2\u00ae\u00af\3\2\2\2\u00af\u00b0\7\60\2\2"+
		"\u00b0\u00b1\5;\36\2\u00b1>\3\2\2\2\u00b2\u00b4\t\4\2\2\u00b3\u00b2\3"+
		"\2\2\2\u00b4\u00b5\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6"+
		"\u00b7\3\2\2\2\u00b7\u00b8\b \2\2\u00b8@\3\2\2\2\u00b9\u00ba\7)\2\2\u00ba"+
		"\u00bb\13\2\2\2\u00bb\u00bc\7)\2\2\u00bcB\3\2\2\2\u00bd\u00c1\7$\2\2\u00be"+
		"\u00c0\n\5\2\2\u00bf\u00be\3\2\2\2\u00c0\u00c3\3\2\2\2\u00c1\u00bf\3\2"+
		"\2\2\u00c1\u00c2\3\2\2\2\u00c2\u00c4\3\2\2\2\u00c3\u00c1\3\2\2\2\u00c4"+
		"\u00c5\7$\2\2\u00c5D\3\2\2\2\u00c6\u00c7\7\61\2\2\u00c7\u00c8\7\61\2\2"+
		"\u00c8\u00cc\3\2\2\2\u00c9\u00cb\13\2\2\2\u00ca\u00c9\3\2\2\2\u00cb\u00ce"+
		"\3\2\2\2\u00cc\u00cd\3\2\2\2\u00cc\u00ca\3\2\2\2\u00cd\u00cf\3\2\2\2\u00ce"+
		"\u00cc\3\2\2\2\u00cf\u00d0\7\f\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d2\b#"+
		"\2\2\u00d2F\3\2\2\2\13\2\u0090\u0092\u009e\u00aa\u00ad\u00b5\u00c1\u00cc"+
		"\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}