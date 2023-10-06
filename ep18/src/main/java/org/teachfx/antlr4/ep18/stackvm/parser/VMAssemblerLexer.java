// Generated from src/main/java/org/teachfx/antlr4/ep18/stackvm/parser/VMAssembler.g4 by ANTLR 4.9.2
package org.teachfx.antlr4.ep18.stackvm.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class VMAssemblerLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, REG=8, ID=9, FUNC=10, 
		INT=11, CHAR=12, BOOL=13, STRING=14, FLOAT=15, WS=16, NEWLINE=17;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "REG", "ID", 
			"FUNC", "LETTER", "INT", "CHAR", "BOOL", "STRING", "STR_CHARS", "FLOAT", 
			"WS", "NEWLINE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.globals'", "'.def'", "':'", "'args'", "'='", "','", "'locals'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, "REG", "ID", "FUNC", 
			"INT", "CHAR", "BOOL", "STRING", "FLOAT", "WS", "NEWLINE"
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


	public VMAssemblerLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "VMAssembler.g4"; }

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

	@Override
	public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
		switch (ruleIndex) {
		case 9:
			FUNC_action((RuleContext)_localctx, actionIndex);
			break;
		case 14:
			STRING_action((RuleContext)_localctx, actionIndex);
			break;
		case 17:
			WS_action((RuleContext)_localctx, actionIndex);
			break;
		}
	}
	private void FUNC_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 0:
			setText(getText().substring(0,getText().length()-2)); 
			break;
		}
	}
	private void STRING_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 1:
			 setText(getText().substring(1, getText().length() - 1)); 
			break;
		}
	}
	private void WS_action(RuleContext _localctx, int actionIndex) {
		switch (actionIndex) {
		case 2:
			skip();
			break;
		}
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\23\u00a3\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\2\3\3\3\3"+
		"\3\3\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3"+
		"\b\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n\3\n\7\nP\n\n\f\n\16\nS\13\n\3\13\3"+
		"\13\3\13\3\13\3\13\3\13\3\f\3\f\3\r\5\r^\n\r\3\r\6\ra\n\r\r\r\16\rb\3"+
		"\16\3\16\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\3\17\5\17r"+
		"\n\17\3\20\3\20\3\20\3\20\3\20\3\21\7\21z\n\21\f\21\16\21}\13\21\3\22"+
		"\3\22\3\22\7\22\u0082\n\22\f\22\16\22\u0085\13\22\3\22\3\22\6\22\u0089"+
		"\n\22\r\22\16\22\u008a\5\22\u008d\n\22\3\23\6\23\u0090\n\23\r\23\16\23"+
		"\u0091\3\23\3\23\3\24\3\24\7\24\u0098\n\24\f\24\16\24\u009b\13\24\5\24"+
		"\u009d\n\24\3\24\5\24\u00a0\n\24\3\24\3\24\3\u0099\2\25\3\3\5\4\7\5\t"+
		"\6\13\7\r\b\17\t\21\n\23\13\25\f\27\2\31\r\33\16\35\17\37\20!\2#\21%\22"+
		"\'\23\3\2\6\4\2\62;aa\4\2C\\c|\3\2$$\4\2\13\13\"\"\2\u00ad\2\3\3\2\2\2"+
		"\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2"+
		"\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2"+
		"\2\35\3\2\2\2\2\37\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\3)\3\2\2"+
		"\2\5\62\3\2\2\2\7\67\3\2\2\2\t9\3\2\2\2\13>\3\2\2\2\r@\3\2\2\2\17B\3\2"+
		"\2\2\21I\3\2\2\2\23L\3\2\2\2\25T\3\2\2\2\27Z\3\2\2\2\31]\3\2\2\2\33d\3"+
		"\2\2\2\35q\3\2\2\2\37s\3\2\2\2!{\3\2\2\2#\u008c\3\2\2\2%\u008f\3\2\2\2"+
		"\'\u009c\3\2\2\2)*\7\60\2\2*+\7i\2\2+,\7n\2\2,-\7q\2\2-.\7d\2\2./\7c\2"+
		"\2/\60\7n\2\2\60\61\7u\2\2\61\4\3\2\2\2\62\63\7\60\2\2\63\64\7f\2\2\64"+
		"\65\7g\2\2\65\66\7h\2\2\66\6\3\2\2\2\678\7<\2\28\b\3\2\2\29:\7c\2\2:;"+
		"\7t\2\2;<\7i\2\2<=\7u\2\2=\n\3\2\2\2>?\7?\2\2?\f\3\2\2\2@A\7.\2\2A\16"+
		"\3\2\2\2BC\7n\2\2CD\7q\2\2DE\7e\2\2EF\7c\2\2FG\7n\2\2GH\7u\2\2H\20\3\2"+
		"\2\2IJ\7t\2\2JK\5\31\r\2K\22\3\2\2\2LQ\5\27\f\2MP\5\27\f\2NP\t\2\2\2O"+
		"M\3\2\2\2ON\3\2\2\2PS\3\2\2\2QO\3\2\2\2QR\3\2\2\2R\24\3\2\2\2SQ\3\2\2"+
		"\2TU\5\23\n\2UV\7*\2\2VW\7+\2\2WX\3\2\2\2XY\b\13\2\2Y\26\3\2\2\2Z[\t\3"+
		"\2\2[\30\3\2\2\2\\^\7/\2\2]\\\3\2\2\2]^\3\2\2\2^`\3\2\2\2_a\4\62;\2`_"+
		"\3\2\2\2ab\3\2\2\2b`\3\2\2\2bc\3\2\2\2c\32\3\2\2\2de\7)\2\2ef\13\2\2\2"+
		"fg\7)\2\2g\34\3\2\2\2hi\7v\2\2ij\7t\2\2jk\7w\2\2kr\7g\2\2lm\7h\2\2mn\7"+
		"c\2\2no\7n\2\2op\7u\2\2pr\7g\2\2qh\3\2\2\2ql\3\2\2\2r\36\3\2\2\2st\7$"+
		"\2\2tu\5!\21\2uv\7$\2\2vw\b\20\3\2w \3\2\2\2xz\n\4\2\2yx\3\2\2\2z}\3\2"+
		"\2\2{y\3\2\2\2{|\3\2\2\2|\"\3\2\2\2}{\3\2\2\2~\177\5\31\r\2\177\u0083"+
		"\7\60\2\2\u0080\u0082\5\31\r\2\u0081\u0080\3\2\2\2\u0082\u0085\3\2\2\2"+
		"\u0083\u0081\3\2\2\2\u0083\u0084\3\2\2\2\u0084\u008d\3\2\2\2\u0085\u0083"+
		"\3\2\2\2\u0086\u0088\7\60\2\2\u0087\u0089\5\31\r\2\u0088\u0087\3\2\2\2"+
		"\u0089\u008a\3\2\2\2\u008a\u0088\3\2\2\2\u008a\u008b\3\2\2\2\u008b\u008d"+
		"\3\2\2\2\u008c~\3\2\2\2\u008c\u0086\3\2\2\2\u008d$\3\2\2\2\u008e\u0090"+
		"\t\5\2\2\u008f\u008e\3\2\2\2\u0090\u0091\3\2\2\2\u0091\u008f\3\2\2\2\u0091"+
		"\u0092\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0094\b\23\4\2\u0094&\3\2\2\2"+
		"\u0095\u0099\7=\2\2\u0096\u0098\13\2\2\2\u0097\u0096\3\2\2\2\u0098\u009b"+
		"\3\2\2\2\u0099\u009a\3\2\2\2\u0099\u0097\3\2\2\2\u009a\u009d\3\2\2\2\u009b"+
		"\u0099\3\2\2\2\u009c\u0095\3\2\2\2\u009c\u009d\3\2\2\2\u009d\u009f\3\2"+
		"\2\2\u009e\u00a0\7\17\2\2\u009f\u009e\3\2\2\2\u009f\u00a0\3\2\2\2\u00a0"+
		"\u00a1\3\2\2\2\u00a1\u00a2\7\f\2\2\u00a2(\3\2\2\2\20\2OQ]bq{\u0083\u008a"+
		"\u008c\u0091\u0099\u009c\u009f\5\3\13\2\3\20\3\3\23\4";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}