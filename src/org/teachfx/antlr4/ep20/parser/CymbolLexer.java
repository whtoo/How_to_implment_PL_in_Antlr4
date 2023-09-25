// Generated from ./parser/Cymbol.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep20.parser;

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
		T__24=25, T__25=26, T__26=27, T__27=28, ID=29, BOOLEAN=30, NULL=31, INT=32, 
		FLOAT=33, WS=34, CHAR=35, STRING=36, SLCOMMENT=37, COMMNET=38;
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
			"T__17", "T__18", "T__19", "T__20", "T__21", "T__22", "T__23", "T__24", 
			"T__25", "T__26", "T__27", "ID", "BOOLEAN", "NULL", "LETTER", "INT", 
			"FLOAT", "WS", "CHAR", "STRING", "SLCOMMENT", "COMMNET"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "';'", "'float'", "'int'", "'void'", "'bool'", "'string'", 
			"'object'", "'('", "')'", "','", "'{'", "'}'", "'return'", "'if'", "'else'", 
			"'while'", "'-'", "'!'", "'*'", "'/'", "'+'", "'=='", "'!='", "'>'", 
			"'>='", "'<'", "'<='", null, null, "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, "ID", "BOOLEAN", "NULL", "INT", "FLOAT", 
			"WS", "CHAR", "STRING", "SLCOMMENT", "COMMNET"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2(\u0103\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\3\2\3\2\3\3\3\3\3\4"+
		"\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3\6\3\7\3\7\3\7\3"+
		"\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\n\3\n"+
		"\3\13\3\13\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17"+
		"\3\20\3\20\3\20\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22"+
		"\3\23\3\23\3\24\3\24\3\25\3\25\3\26\3\26\3\27\3\27\3\30\3\30\3\30\3\31"+
		"\3\31\3\31\3\32\3\32\3\33\3\33\3\33\3\34\3\34\3\35\3\35\3\35\3\36\3\36"+
		"\3\36\7\36\u00b4\n\36\f\36\16\36\u00b7\13\36\3\37\3\37\3\37\3\37\3\37"+
		"\3\37\3\37\3\37\3\37\5\37\u00c2\n\37\3 \3 \3 \3 \3 \3!\3!\3\"\6\"\u00cc"+
		"\n\"\r\"\16\"\u00cd\3#\5#\u00d1\n#\3#\3#\3#\3$\6$\u00d7\n$\r$\16$\u00d8"+
		"\3$\3$\3%\3%\3%\3%\3&\3&\7&\u00e3\n&\f&\16&\u00e6\13&\3&\3&\3\'\3\'\3"+
		"\'\3\'\7\'\u00ee\n\'\f\'\16\'\u00f1\13\'\3\'\3\'\3\'\3\'\3(\3(\3(\3(\7"+
		"(\u00fb\n(\f(\16(\u00fe\13(\3(\3(\3(\3(\4\u00ef\u00fc\2)\3\3\5\4\7\5\t"+
		"\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23"+
		"%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37= ?!A\2C\"E#"+
		"G$I%K&M\'O(\3\2\6\3\2\62;\4\2C\\c|\5\2\13\f\17\17\"\"\5\2\f\f\17\17$$"+
		"\2\u010a\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2"+
		"\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2"+
		"\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2"+
		"\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\2G\3\2\2\2"+
		"\2I\3\2\2\2\2K\3\2\2\2\2M\3\2\2\2\2O\3\2\2\2\3Q\3\2\2\2\5S\3\2\2\2\7U"+
		"\3\2\2\2\t[\3\2\2\2\13_\3\2\2\2\rd\3\2\2\2\17i\3\2\2\2\21p\3\2\2\2\23"+
		"w\3\2\2\2\25y\3\2\2\2\27{\3\2\2\2\31}\3\2\2\2\33\177\3\2\2\2\35\u0081"+
		"\3\2\2\2\37\u0088\3\2\2\2!\u008b\3\2\2\2#\u0090\3\2\2\2%\u0096\3\2\2\2"+
		"\'\u0098\3\2\2\2)\u009a\3\2\2\2+\u009c\3\2\2\2-\u009e\3\2\2\2/\u00a0\3"+
		"\2\2\2\61\u00a3\3\2\2\2\63\u00a6\3\2\2\2\65\u00a8\3\2\2\2\67\u00ab\3\2"+
		"\2\29\u00ad\3\2\2\2;\u00b0\3\2\2\2=\u00c1\3\2\2\2?\u00c3\3\2\2\2A\u00c8"+
		"\3\2\2\2C\u00cb\3\2\2\2E\u00d0\3\2\2\2G\u00d6\3\2\2\2I\u00dc\3\2\2\2K"+
		"\u00e0\3\2\2\2M\u00e9\3\2\2\2O\u00f6\3\2\2\2QR\7?\2\2R\4\3\2\2\2ST\7="+
		"\2\2T\6\3\2\2\2UV\7h\2\2VW\7n\2\2WX\7q\2\2XY\7c\2\2YZ\7v\2\2Z\b\3\2\2"+
		"\2[\\\7k\2\2\\]\7p\2\2]^\7v\2\2^\n\3\2\2\2_`\7x\2\2`a\7q\2\2ab\7k\2\2"+
		"bc\7f\2\2c\f\3\2\2\2de\7d\2\2ef\7q\2\2fg\7q\2\2gh\7n\2\2h\16\3\2\2\2i"+
		"j\7u\2\2jk\7v\2\2kl\7t\2\2lm\7k\2\2mn\7p\2\2no\7i\2\2o\20\3\2\2\2pq\7"+
		"q\2\2qr\7d\2\2rs\7l\2\2st\7g\2\2tu\7e\2\2uv\7v\2\2v\22\3\2\2\2wx\7*\2"+
		"\2x\24\3\2\2\2yz\7+\2\2z\26\3\2\2\2{|\7.\2\2|\30\3\2\2\2}~\7}\2\2~\32"+
		"\3\2\2\2\177\u0080\7\177\2\2\u0080\34\3\2\2\2\u0081\u0082\7t\2\2\u0082"+
		"\u0083\7g\2\2\u0083\u0084\7v\2\2\u0084\u0085\7w\2\2\u0085\u0086\7t\2\2"+
		"\u0086\u0087\7p\2\2\u0087\36\3\2\2\2\u0088\u0089\7k\2\2\u0089\u008a\7"+
		"h\2\2\u008a \3\2\2\2\u008b\u008c\7g\2\2\u008c\u008d\7n\2\2\u008d\u008e"+
		"\7u\2\2\u008e\u008f\7g\2\2\u008f\"\3\2\2\2\u0090\u0091\7y\2\2\u0091\u0092"+
		"\7j\2\2\u0092\u0093\7k\2\2\u0093\u0094\7n\2\2\u0094\u0095\7g\2\2\u0095"+
		"$\3\2\2\2\u0096\u0097\7/\2\2\u0097&\3\2\2\2\u0098\u0099\7#\2\2\u0099("+
		"\3\2\2\2\u009a\u009b\7,\2\2\u009b*\3\2\2\2\u009c\u009d\7\61\2\2\u009d"+
		",\3\2\2\2\u009e\u009f\7-\2\2\u009f.\3\2\2\2\u00a0\u00a1\7?\2\2\u00a1\u00a2"+
		"\7?\2\2\u00a2\60\3\2\2\2\u00a3\u00a4\7#\2\2\u00a4\u00a5\7?\2\2\u00a5\62"+
		"\3\2\2\2\u00a6\u00a7\7@\2\2\u00a7\64\3\2\2\2\u00a8\u00a9\7@\2\2\u00a9"+
		"\u00aa\7?\2\2\u00aa\66\3\2\2\2\u00ab\u00ac\7>\2\2\u00ac8\3\2\2\2\u00ad"+
		"\u00ae\7>\2\2\u00ae\u00af\7?\2\2\u00af:\3\2\2\2\u00b0\u00b5\5A!\2\u00b1"+
		"\u00b4\5A!\2\u00b2\u00b4\t\2\2\2\u00b3\u00b1\3\2\2\2\u00b3\u00b2\3\2\2"+
		"\2\u00b4\u00b7\3\2\2\2\u00b5\u00b3\3\2\2\2\u00b5\u00b6\3\2\2\2\u00b6<"+
		"\3\2\2\2\u00b7\u00b5\3\2\2\2\u00b8\u00b9\7v\2\2\u00b9\u00ba\7t\2\2\u00ba"+
		"\u00bb\7w\2\2\u00bb\u00c2\7g\2\2\u00bc\u00bd\7h\2\2\u00bd\u00be\7c\2\2"+
		"\u00be\u00bf\7n\2\2\u00bf\u00c0\7u\2\2\u00c0\u00c2\7g\2\2\u00c1\u00b8"+
		"\3\2\2\2\u00c1\u00bc\3\2\2\2\u00c2>\3\2\2\2\u00c3\u00c4\7p\2\2\u00c4\u00c5"+
		"\7w\2\2\u00c5\u00c6\7n\2\2\u00c6\u00c7\7n\2\2\u00c7@\3\2\2\2\u00c8\u00c9"+
		"\t\3\2\2\u00c9B\3\2\2\2\u00ca\u00cc\t\2\2\2\u00cb\u00ca\3\2\2\2\u00cc"+
		"\u00cd\3\2\2\2\u00cd\u00cb\3\2\2\2\u00cd\u00ce\3\2\2\2\u00ceD\3\2\2\2"+
		"\u00cf\u00d1\5C\"\2\u00d0\u00cf\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d2"+
		"\3\2\2\2\u00d2\u00d3\7\60\2\2\u00d3\u00d4\5C\"\2\u00d4F\3\2\2\2\u00d5"+
		"\u00d7\t\4\2\2\u00d6\u00d5\3\2\2\2\u00d7\u00d8\3\2\2\2\u00d8\u00d6\3\2"+
		"\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00da\3\2\2\2\u00da\u00db\b$\2\2\u00db"+
		"H\3\2\2\2\u00dc\u00dd\7)\2\2\u00dd\u00de\13\2\2\2\u00de\u00df\7)\2\2\u00df"+
		"J\3\2\2\2\u00e0\u00e4\7$\2\2\u00e1\u00e3\n\5\2\2\u00e2\u00e1\3\2\2\2\u00e3"+
		"\u00e6\3\2\2\2\u00e4\u00e2\3\2\2\2\u00e4\u00e5\3\2\2\2\u00e5\u00e7\3\2"+
		"\2\2\u00e6\u00e4\3\2\2\2\u00e7\u00e8\7$\2\2\u00e8L\3\2\2\2\u00e9\u00ea"+
		"\7\61\2\2\u00ea\u00eb\7\61\2\2\u00eb\u00ef\3\2\2\2\u00ec\u00ee\13\2\2"+
		"\2\u00ed\u00ec\3\2\2\2\u00ee\u00f1\3\2\2\2\u00ef\u00f0\3\2\2\2\u00ef\u00ed"+
		"\3\2\2\2\u00f0\u00f2\3\2\2\2\u00f1\u00ef\3\2\2\2\u00f2\u00f3\7\f\2\2\u00f3"+
		"\u00f4\3\2\2\2\u00f4\u00f5\b\'\2\2\u00f5N\3\2\2\2\u00f6\u00f7\7\61\2\2"+
		"\u00f7\u00f8\7,\2\2\u00f8\u00fc\3\2\2\2\u00f9\u00fb\13\2\2\2\u00fa\u00f9"+
		"\3\2\2\2\u00fb\u00fe\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fc\u00fa\3\2\2\2\u00fd"+
		"\u00ff\3\2\2\2\u00fe\u00fc\3\2\2\2\u00ff\u0100\7\f\2\2\u0100\u0101\3\2"+
		"\2\2\u0101\u0102\b(\2\2\u0102P\3\2\2\2\f\2\u00b3\u00b5\u00c1\u00cd\u00d0"+
		"\u00d8\u00e4\u00ef\u00fc\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}