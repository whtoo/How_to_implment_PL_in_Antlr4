// Generated from ./parser/Cymbol.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep15.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CymbolLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		ID=18, INT=19, FLOAT=20, WS=21, SLCOMMENT=22;
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
			"ID", "LETTER", "INT", "FLOAT", "WS", "SLCOMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "';'", "'float'", "'int'", "'void'", "'('", "')'", "','", 
			"'{'", "'}'", "'return'", "'-'", "'!'", "'*'", "'/'", "'+'", "'=='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, "ID", "INT", "FLOAT", "WS", "SLCOMMENT"
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\30\u008b\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\3\2"+
		"\3\2\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\6\3\6\3\6\3\6\3"+
		"\6\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\f\3\f\3\f\3\f\3"+
		"\f\3\r\3\r\3\16\3\16\3\17\3\17\3\20\3\20\3\21\3\21\3\22\3\22\3\22\3\23"+
		"\3\23\3\23\7\23f\n\23\f\23\16\23i\13\23\3\24\3\24\3\25\6\25n\n\25\r\25"+
		"\16\25o\3\26\5\26s\n\26\3\26\3\26\3\26\3\27\6\27y\n\27\r\27\16\27z\3\27"+
		"\3\27\3\30\3\30\3\30\3\30\7\30\u0083\n\30\f\30\16\30\u0086\13\30\3\30"+
		"\3\30\3\30\3\30\3\u0084\2\31\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13"+
		"\25\f\27\r\31\16\33\17\35\20\37\21!\22#\23%\24\'\2)\25+\26-\27/\30\3\2"+
		"\5\3\2\62;\4\2C\\c|\5\2\13\f\17\17\"\"\2\u008f\2\3\3\2\2\2\2\5\3\2\2\2"+
		"\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3"+
		"\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2"+
		"\2\2\35\3\2\2\2\2\37\3\2\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2)\3\2\2"+
		"\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\3\61\3\2\2\2\5\63\3\2\2\2\7\65\3\2"+
		"\2\2\t;\3\2\2\2\13?\3\2\2\2\rD\3\2\2\2\17F\3\2\2\2\21H\3\2\2\2\23J\3\2"+
		"\2\2\25L\3\2\2\2\27N\3\2\2\2\31U\3\2\2\2\33W\3\2\2\2\35Y\3\2\2\2\37[\3"+
		"\2\2\2!]\3\2\2\2#_\3\2\2\2%b\3\2\2\2\'j\3\2\2\2)m\3\2\2\2+r\3\2\2\2-x"+
		"\3\2\2\2/~\3\2\2\2\61\62\7?\2\2\62\4\3\2\2\2\63\64\7=\2\2\64\6\3\2\2\2"+
		"\65\66\7h\2\2\66\67\7n\2\2\678\7q\2\289\7c\2\29:\7v\2\2:\b\3\2\2\2;<\7"+
		"k\2\2<=\7p\2\2=>\7v\2\2>\n\3\2\2\2?@\7x\2\2@A\7q\2\2AB\7k\2\2BC\7f\2\2"+
		"C\f\3\2\2\2DE\7*\2\2E\16\3\2\2\2FG\7+\2\2G\20\3\2\2\2HI\7.\2\2I\22\3\2"+
		"\2\2JK\7}\2\2K\24\3\2\2\2LM\7\177\2\2M\26\3\2\2\2NO\7t\2\2OP\7g\2\2PQ"+
		"\7v\2\2QR\7w\2\2RS\7t\2\2ST\7p\2\2T\30\3\2\2\2UV\7/\2\2V\32\3\2\2\2WX"+
		"\7#\2\2X\34\3\2\2\2YZ\7,\2\2Z\36\3\2\2\2[\\\7\61\2\2\\ \3\2\2\2]^\7-\2"+
		"\2^\"\3\2\2\2_`\7?\2\2`a\7?\2\2a$\3\2\2\2bg\5\'\24\2cf\5\'\24\2df\t\2"+
		"\2\2ec\3\2\2\2ed\3\2\2\2fi\3\2\2\2ge\3\2\2\2gh\3\2\2\2h&\3\2\2\2ig\3\2"+
		"\2\2jk\t\3\2\2k(\3\2\2\2ln\t\2\2\2ml\3\2\2\2no\3\2\2\2om\3\2\2\2op\3\2"+
		"\2\2p*\3\2\2\2qs\5)\25\2rq\3\2\2\2rs\3\2\2\2st\3\2\2\2tu\7\60\2\2uv\5"+
		")\25\2v,\3\2\2\2wy\t\4\2\2xw\3\2\2\2yz\3\2\2\2zx\3\2\2\2z{\3\2\2\2{|\3"+
		"\2\2\2|}\b\27\2\2}.\3\2\2\2~\177\7\61\2\2\177\u0080\7\61\2\2\u0080\u0084"+
		"\3\2\2\2\u0081\u0083\13\2\2\2\u0082\u0081\3\2\2\2\u0083\u0086\3\2\2\2"+
		"\u0084\u0085\3\2\2\2\u0084\u0082\3\2\2\2\u0085\u0087\3\2\2\2\u0086\u0084"+
		"\3\2\2\2\u0087\u0088\7\f\2\2\u0088\u0089\3\2\2\2\u0089\u008a\b\30\2\2"+
		"\u008a\60\3\2\2\2\t\2egorz\u0084\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}