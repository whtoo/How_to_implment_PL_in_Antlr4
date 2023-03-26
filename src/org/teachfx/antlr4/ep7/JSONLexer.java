// Generated from JSON.g4 by ANTLR 4.8
package org.teachfx.antlr4.ep7;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class JSONLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, STRING=11, NUMBER=12, WS=13;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "STRING", "ESC", "UNICODE", "HEX", "NUMBER", "INT", "EXP", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{}'", "'{'", "','", "'}'", "':'", "'true'", "'false'", "'null'", 
			"'['", "']'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "STRING", 
			"NUMBER", "WS"
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


	public JSONLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "JSON.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\17\u0089\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\3\2\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3"+
		"\7\3\7\3\7\3\b\3\b\3\b\3\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\3\n\3\n\3\13\3"+
		"\13\3\f\3\f\3\f\7\fJ\n\f\f\f\16\fM\13\f\3\f\3\f\3\r\3\r\3\r\5\rT\n\r\3"+
		"\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\20\5\20_\n\20\3\20\3\20\3\20"+
		"\3\20\5\20e\n\20\3\20\5\20h\n\20\3\20\3\20\3\20\3\20\5\20n\n\20\3\20\5"+
		"\20q\n\20\3\21\3\21\3\21\7\21v\n\21\f\21\16\21y\13\21\5\21{\n\21\3\22"+
		"\3\22\5\22\177\n\22\3\22\3\22\3\23\6\23\u0084\n\23\r\23\16\23\u0085\3"+
		"\23\3\23\2\2\24\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31"+
		"\2\33\2\35\2\37\16!\2#\2%\17\3\2\n\4\2$$^^\n\2$$\61\61^^ddhhppttvv\5\2"+
		"\62;CHch\3\2\63;\3\2\62;\4\2GGgg\4\2--//\5\2\13\f\17\17\"\"\2\u0090\2"+
		"\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2"+
		"\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2"+
		"\37\3\2\2\2\2%\3\2\2\2\3\'\3\2\2\2\5*\3\2\2\2\7,\3\2\2\2\t.\3\2\2\2\13"+
		"\60\3\2\2\2\r\62\3\2\2\2\17\67\3\2\2\2\21=\3\2\2\2\23B\3\2\2\2\25D\3\2"+
		"\2\2\27F\3\2\2\2\31P\3\2\2\2\33U\3\2\2\2\35[\3\2\2\2\37p\3\2\2\2!z\3\2"+
		"\2\2#|\3\2\2\2%\u0083\3\2\2\2\'(\7}\2\2()\7\177\2\2)\4\3\2\2\2*+\7}\2"+
		"\2+\6\3\2\2\2,-\7.\2\2-\b\3\2\2\2./\7\177\2\2/\n\3\2\2\2\60\61\7<\2\2"+
		"\61\f\3\2\2\2\62\63\7v\2\2\63\64\7t\2\2\64\65\7w\2\2\65\66\7g\2\2\66\16"+
		"\3\2\2\2\678\7h\2\289\7c\2\29:\7n\2\2:;\7u\2\2;<\7g\2\2<\20\3\2\2\2=>"+
		"\7p\2\2>?\7w\2\2?@\7n\2\2@A\7n\2\2A\22\3\2\2\2BC\7]\2\2C\24\3\2\2\2DE"+
		"\7_\2\2E\26\3\2\2\2FK\7$\2\2GJ\5\31\r\2HJ\n\2\2\2IG\3\2\2\2IH\3\2\2\2"+
		"JM\3\2\2\2KI\3\2\2\2KL\3\2\2\2LN\3\2\2\2MK\3\2\2\2NO\7$\2\2O\30\3\2\2"+
		"\2PS\7^\2\2QT\t\3\2\2RT\5\33\16\2SQ\3\2\2\2SR\3\2\2\2T\32\3\2\2\2UV\7"+
		"w\2\2VW\5\35\17\2WX\5\35\17\2XY\5\35\17\2YZ\5\35\17\2Z\34\3\2\2\2[\\\t"+
		"\4\2\2\\\36\3\2\2\2]_\7/\2\2^]\3\2\2\2^_\3\2\2\2_`\3\2\2\2`a\5!\21\2a"+
		"b\7\60\2\2bd\5!\21\2ce\5#\22\2dc\3\2\2\2de\3\2\2\2eq\3\2\2\2fh\7/\2\2"+
		"gf\3\2\2\2gh\3\2\2\2hi\3\2\2\2ij\5!\21\2jk\5#\22\2kq\3\2\2\2ln\7/\2\2"+
		"ml\3\2\2\2mn\3\2\2\2no\3\2\2\2oq\5!\21\2p^\3\2\2\2pg\3\2\2\2pm\3\2\2\2"+
		"q \3\2\2\2r{\7\62\2\2sw\t\5\2\2tv\t\6\2\2ut\3\2\2\2vy\3\2\2\2wu\3\2\2"+
		"\2wx\3\2\2\2x{\3\2\2\2yw\3\2\2\2zr\3\2\2\2zs\3\2\2\2{\"\3\2\2\2|~\t\7"+
		"\2\2}\177\t\b\2\2~}\3\2\2\2~\177\3\2\2\2\177\u0080\3\2\2\2\u0080\u0081"+
		"\5!\21\2\u0081$\3\2\2\2\u0082\u0084\t\t\2\2\u0083\u0082\3\2\2\2\u0084"+
		"\u0085\3\2\2\2\u0085\u0083\3\2\2\2\u0085\u0086\3\2\2\2\u0086\u0087\3\2"+
		"\2\2\u0087\u0088\b\23\2\2\u0088&\3\2\2\2\17\2IKS^dgmpwz~\u0085\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}