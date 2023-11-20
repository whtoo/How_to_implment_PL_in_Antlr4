// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

package org.teachfx.antlr4.ep18.parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class VMAssemblerLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.11.0-SNAPSHOT", RuntimeMetaData.VERSION); }

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
		"\u0004\u0000\u0011\u00a1\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0004"+
		"\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006\u0001\u0006\u0001\u0006"+
		"\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\b\u0001\b\u0001\b\u0005\bN\b\b\n\b\f\bQ\t\b\u0001\t"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0001\t\u0001\n\u0001\n\u0001\u000b\u0003"+
		"\u000b\\\b\u000b\u0001\u000b\u0004\u000b_\b\u000b\u000b\u000b\f\u000b"+
		"`\u0001\f\u0001\f\u0001\f\u0001\f\u0001\r\u0001\r\u0001\r\u0001\r\u0001"+
		"\r\u0001\r\u0001\r\u0001\r\u0001\r\u0003\rp\b\r\u0001\u000e\u0001\u000e"+
		"\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000f\u0005\u000fx\b\u000f"+
		"\n\u000f\f\u000f{\t\u000f\u0001\u0010\u0001\u0010\u0001\u0010\u0005\u0010"+
		"\u0080\b\u0010\n\u0010\f\u0010\u0083\t\u0010\u0001\u0010\u0001\u0010\u0004"+
		"\u0010\u0087\b\u0010\u000b\u0010\f\u0010\u0088\u0003\u0010\u008b\b\u0010"+
		"\u0001\u0011\u0004\u0011\u008e\b\u0011\u000b\u0011\f\u0011\u008f\u0001"+
		"\u0011\u0001\u0011\u0001\u0012\u0001\u0012\u0005\u0012\u0096\b\u0012\n"+
		"\u0012\f\u0012\u0099\t\u0012\u0003\u0012\u009b\b\u0012\u0001\u0012\u0003"+
		"\u0012\u009e\b\u0012\u0001\u0012\u0001\u0012\u0001\u0097\u0000\u0013\u0001"+
		"\u0001\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007"+
		"\u000f\b\u0011\t\u0013\n\u0015\u0000\u0017\u000b\u0019\f\u001b\r\u001d"+
		"\u000e\u001f\u0000!\u000f#\u0010%\u0011\u0001\u0000\u0004\u0002\u0000"+
		"09__\u0002\u0000AZaz\u0001\u0000\"\"\u0002\u0000\t\t  \u00ab\u0000\u0001"+
		"\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005"+
		"\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001"+
		"\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000"+
		"\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000"+
		"\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0017\u0001\u0000"+
		"\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000"+
		"\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000!\u0001\u0000\u0000"+
		"\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000\u0001"+
		"\'\u0001\u0000\u0000\u0000\u00030\u0001\u0000\u0000\u0000\u00055\u0001"+
		"\u0000\u0000\u0000\u00077\u0001\u0000\u0000\u0000\t<\u0001\u0000\u0000"+
		"\u0000\u000b>\u0001\u0000\u0000\u0000\r@\u0001\u0000\u0000\u0000\u000f"+
		"G\u0001\u0000\u0000\u0000\u0011J\u0001\u0000\u0000\u0000\u0013R\u0001"+
		"\u0000\u0000\u0000\u0015X\u0001\u0000\u0000\u0000\u0017[\u0001\u0000\u0000"+
		"\u0000\u0019b\u0001\u0000\u0000\u0000\u001bo\u0001\u0000\u0000\u0000\u001d"+
		"q\u0001\u0000\u0000\u0000\u001fy\u0001\u0000\u0000\u0000!\u008a\u0001"+
		"\u0000\u0000\u0000#\u008d\u0001\u0000\u0000\u0000%\u009a\u0001\u0000\u0000"+
		"\u0000\'(\u0005.\u0000\u0000()\u0005g\u0000\u0000)*\u0005l\u0000\u0000"+
		"*+\u0005o\u0000\u0000+,\u0005b\u0000\u0000,-\u0005a\u0000\u0000-.\u0005"+
		"l\u0000\u0000./\u0005s\u0000\u0000/\u0002\u0001\u0000\u0000\u000001\u0005"+
		".\u0000\u000012\u0005d\u0000\u000023\u0005e\u0000\u000034\u0005f\u0000"+
		"\u00004\u0004\u0001\u0000\u0000\u000056\u0005:\u0000\u00006\u0006\u0001"+
		"\u0000\u0000\u000078\u0005a\u0000\u000089\u0005r\u0000\u00009:\u0005g"+
		"\u0000\u0000:;\u0005s\u0000\u0000;\b\u0001\u0000\u0000\u0000<=\u0005="+
		"\u0000\u0000=\n\u0001\u0000\u0000\u0000>?\u0005,\u0000\u0000?\f\u0001"+
		"\u0000\u0000\u0000@A\u0005l\u0000\u0000AB\u0005o\u0000\u0000BC\u0005c"+
		"\u0000\u0000CD\u0005a\u0000\u0000DE\u0005l\u0000\u0000EF\u0005s\u0000"+
		"\u0000F\u000e\u0001\u0000\u0000\u0000GH\u0005r\u0000\u0000HI\u0003\u0017"+
		"\u000b\u0000I\u0010\u0001\u0000\u0000\u0000JO\u0003\u0015\n\u0000KN\u0003"+
		"\u0015\n\u0000LN\u0007\u0000\u0000\u0000MK\u0001\u0000\u0000\u0000ML\u0001"+
		"\u0000\u0000\u0000NQ\u0001\u0000\u0000\u0000OM\u0001\u0000\u0000\u0000"+
		"OP\u0001\u0000\u0000\u0000P\u0012\u0001\u0000\u0000\u0000QO\u0001\u0000"+
		"\u0000\u0000RS\u0003\u0011\b\u0000ST\u0005(\u0000\u0000TU\u0005)\u0000"+
		"\u0000UV\u0001\u0000\u0000\u0000VW\u0006\t\u0000\u0000W\u0014\u0001\u0000"+
		"\u0000\u0000XY\u0007\u0001\u0000\u0000Y\u0016\u0001\u0000\u0000\u0000"+
		"Z\\\u0005-\u0000\u0000[Z\u0001\u0000\u0000\u0000[\\\u0001\u0000\u0000"+
		"\u0000\\^\u0001\u0000\u0000\u0000]_\u000209\u0000^]\u0001\u0000\u0000"+
		"\u0000_`\u0001\u0000\u0000\u0000`^\u0001\u0000\u0000\u0000`a\u0001\u0000"+
		"\u0000\u0000a\u0018\u0001\u0000\u0000\u0000bc\u0005\'\u0000\u0000cd\t"+
		"\u0000\u0000\u0000de\u0005\'\u0000\u0000e\u001a\u0001\u0000\u0000\u0000"+
		"fg\u0005t\u0000\u0000gh\u0005r\u0000\u0000hi\u0005u\u0000\u0000ip\u0005"+
		"e\u0000\u0000jk\u0005f\u0000\u0000kl\u0005a\u0000\u0000lm\u0005l\u0000"+
		"\u0000mn\u0005s\u0000\u0000np\u0005e\u0000\u0000of\u0001\u0000\u0000\u0000"+
		"oj\u0001\u0000\u0000\u0000p\u001c\u0001\u0000\u0000\u0000qr\u0005\"\u0000"+
		"\u0000rs\u0003\u001f\u000f\u0000st\u0005\"\u0000\u0000tu\u0006\u000e\u0001"+
		"\u0000u\u001e\u0001\u0000\u0000\u0000vx\b\u0002\u0000\u0000wv\u0001\u0000"+
		"\u0000\u0000x{\u0001\u0000\u0000\u0000yw\u0001\u0000\u0000\u0000yz\u0001"+
		"\u0000\u0000\u0000z \u0001\u0000\u0000\u0000{y\u0001\u0000\u0000\u0000"+
		"|}\u0003\u0017\u000b\u0000}\u0081\u0005.\u0000\u0000~\u0080\u0003\u0017"+
		"\u000b\u0000\u007f~\u0001\u0000\u0000\u0000\u0080\u0083\u0001\u0000\u0000"+
		"\u0000\u0081\u007f\u0001\u0000\u0000\u0000\u0081\u0082\u0001\u0000\u0000"+
		"\u0000\u0082\u008b\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000"+
		"\u0000\u0084\u0086\u0005.\u0000\u0000\u0085\u0087\u0003\u0017\u000b\u0000"+
		"\u0086\u0085\u0001\u0000\u0000\u0000\u0087\u0088\u0001\u0000\u0000\u0000"+
		"\u0088\u0086\u0001\u0000\u0000\u0000\u0088\u0089\u0001\u0000\u0000\u0000"+
		"\u0089\u008b\u0001\u0000\u0000\u0000\u008a|\u0001\u0000\u0000\u0000\u008a"+
		"\u0084\u0001\u0000\u0000\u0000\u008b\"\u0001\u0000\u0000\u0000\u008c\u008e"+
		"\u0007\u0003\u0000\u0000\u008d\u008c\u0001\u0000\u0000\u0000\u008e\u008f"+
		"\u0001\u0000\u0000\u0000\u008f\u008d\u0001\u0000\u0000\u0000\u008f\u0090"+
		"\u0001\u0000\u0000\u0000\u0090\u0091\u0001\u0000\u0000\u0000\u0091\u0092"+
		"\u0006\u0011\u0002\u0000\u0092$\u0001\u0000\u0000\u0000\u0093\u0097\u0005"+
		";\u0000\u0000\u0094\u0096\t\u0000\u0000\u0000\u0095\u0094\u0001\u0000"+
		"\u0000\u0000\u0096\u0099\u0001\u0000\u0000\u0000\u0097\u0098\u0001\u0000"+
		"\u0000\u0000\u0097\u0095\u0001\u0000\u0000\u0000\u0098\u009b\u0001\u0000"+
		"\u0000\u0000\u0099\u0097\u0001\u0000\u0000\u0000\u009a\u0093\u0001\u0000"+
		"\u0000\u0000\u009a\u009b\u0001\u0000\u0000\u0000\u009b\u009d\u0001\u0000"+
		"\u0000\u0000\u009c\u009e\u0005\r\u0000\u0000\u009d\u009c\u0001\u0000\u0000"+
		"\u0000\u009d\u009e\u0001\u0000\u0000\u0000\u009e\u009f\u0001\u0000\u0000"+
		"\u0000\u009f\u00a0\u0005\n\u0000\u0000\u00a0&\u0001\u0000\u0000\u0000"+
		"\u000e\u0000MO[`oy\u0081\u0088\u008a\u008f\u0097\u009a\u009d\u0003\u0001"+
		"\t\u0000\u0001\u000e\u0001\u0001\u0011\u0002";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}