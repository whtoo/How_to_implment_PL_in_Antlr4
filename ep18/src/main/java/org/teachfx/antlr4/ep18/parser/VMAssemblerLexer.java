// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

package org.teachfx.antlr4.ep18.parser;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class VMAssemblerLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.11.0-SNAPSHOT", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, REG=9, 
		ID=10, FUNC=11, INT=12, CHAR=13, BOOL=14, STRING=15, FLOAT=16, WS=17, 
		NEWLINE=18;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "REG", 
			"ID", "FUNC", "LETTER", "INT", "CHAR", "BOOL", "STRING", "STR_CHARS", 
			"FLOAT", "WS", "NEWLINE"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'.globals'", "'.global'", "'.def'", "':'", "'args'", "'='", "','", 
			"'locals'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, "REG", "ID", "FUNC", 
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
		case 10:
			FUNC_action((RuleContext)_localctx, actionIndex);
			break;
		case 15:
			STRING_action((RuleContext)_localctx, actionIndex);
			break;
		case 18:
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
		"\u0004\u0000\u0012\u00ab\u0006\uffff\uffff\u0002\u0000\u0007\u0000\u0002"+
		"\u0001\u0007\u0001\u0002\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002"+
		"\u0004\u0007\u0004\u0002\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002"+
		"\u0007\u0007\u0007\u0002\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0002"+
		"\u000b\u0007\u000b\u0002\f\u0007\f\u0002\r\u0007\r\u0002\u000e\u0007\u000e"+
		"\u0002\u000f\u0007\u000f\u0002\u0010\u0007\u0010\u0002\u0011\u0007\u0011"+
		"\u0002\u0012\u0007\u0012\u0002\u0013\u0007\u0013\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000"+
		"\u0001\u0000\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002"+
		"\u0001\u0002\u0001\u0002\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004"+
		"\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0005\u0001\u0005\u0001\u0006"+
		"\u0001\u0006\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\b\u0001\b\u0001\b\u0001\t\u0001\t\u0001"+
		"\t\u0005\tX\b\t\n\t\f\t[\t\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001"+
		"\n\u0001\u000b\u0001\u000b\u0001\f\u0003\ff\b\f\u0001\f\u0004\fi\b\f\u000b"+
		"\f\f\fj\u0001\r\u0001\r\u0001\r\u0001\r\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001\u000e\u0001"+
		"\u000e\u0003\u000ez\b\u000e\u0001\u000f\u0001\u000f\u0001\u000f\u0001"+
		"\u000f\u0001\u000f\u0001\u0010\u0005\u0010\u0082\b\u0010\n\u0010\f\u0010"+
		"\u0085\t\u0010\u0001\u0011\u0001\u0011\u0001\u0011\u0005\u0011\u008a\b"+
		"\u0011\n\u0011\f\u0011\u008d\t\u0011\u0001\u0011\u0001\u0011\u0004\u0011"+
		"\u0091\b\u0011\u000b\u0011\f\u0011\u0092\u0003\u0011\u0095\b\u0011\u0001"+
		"\u0012\u0004\u0012\u0098\b\u0012\u000b\u0012\f\u0012\u0099\u0001\u0012"+
		"\u0001\u0012\u0001\u0013\u0001\u0013\u0005\u0013\u00a0\b\u0013\n\u0013"+
		"\f\u0013\u00a3\t\u0013\u0003\u0013\u00a5\b\u0013\u0001\u0013\u0003\u0013"+
		"\u00a8\b\u0013\u0001\u0013\u0001\u0013\u0001\u00a1\u0000\u0014\u0001\u0001"+
		"\u0003\u0002\u0005\u0003\u0007\u0004\t\u0005\u000b\u0006\r\u0007\u000f"+
		"\b\u0011\t\u0013\n\u0015\u000b\u0017\u0000\u0019\f\u001b\r\u001d\u000e"+
		"\u001f\u000f!\u0000#\u0010%\u0011\'\u0012\u0001\u0000\u0004\u0002\u0000"+
		"09__\u0002\u0000AZaz\u0001\u0000\"\"\u0002\u0000\t\t  \u00b5\u0000\u0001"+
		"\u0001\u0000\u0000\u0000\u0000\u0003\u0001\u0000\u0000\u0000\u0000\u0005"+
		"\u0001\u0000\u0000\u0000\u0000\u0007\u0001\u0000\u0000\u0000\u0000\t\u0001"+
		"\u0000\u0000\u0000\u0000\u000b\u0001\u0000\u0000\u0000\u0000\r\u0001\u0000"+
		"\u0000\u0000\u0000\u000f\u0001\u0000\u0000\u0000\u0000\u0011\u0001\u0000"+
		"\u0000\u0000\u0000\u0013\u0001\u0000\u0000\u0000\u0000\u0015\u0001\u0000"+
		"\u0000\u0000\u0000\u0019\u0001\u0000\u0000\u0000\u0000\u001b\u0001\u0000"+
		"\u0000\u0000\u0000\u001d\u0001\u0000\u0000\u0000\u0000\u001f\u0001\u0000"+
		"\u0000\u0000\u0000#\u0001\u0000\u0000\u0000\u0000%\u0001\u0000\u0000\u0000"+
		"\u0000\'\u0001\u0000\u0000\u0000\u0001)\u0001\u0000\u0000\u0000\u0003"+
		"2\u0001\u0000\u0000\u0000\u0005:\u0001\u0000\u0000\u0000\u0007?\u0001"+
		"\u0000\u0000\u0000\tA\u0001\u0000\u0000\u0000\u000bF\u0001\u0000\u0000"+
		"\u0000\rH\u0001\u0000\u0000\u0000\u000fJ\u0001\u0000\u0000\u0000\u0011"+
		"Q\u0001\u0000\u0000\u0000\u0013T\u0001\u0000\u0000\u0000\u0015\\\u0001"+
		"\u0000\u0000\u0000\u0017b\u0001\u0000\u0000\u0000\u0019e\u0001\u0000\u0000"+
		"\u0000\u001bl\u0001\u0000\u0000\u0000\u001dy\u0001\u0000\u0000\u0000\u001f"+
		"{\u0001\u0000\u0000\u0000!\u0083\u0001\u0000\u0000\u0000#\u0094\u0001"+
		"\u0000\u0000\u0000%\u0097\u0001\u0000\u0000\u0000\'\u00a4\u0001\u0000"+
		"\u0000\u0000)*\u0005.\u0000\u0000*+\u0005g\u0000\u0000+,\u0005l\u0000"+
		"\u0000,-\u0005o\u0000\u0000-.\u0005b\u0000\u0000./\u0005a\u0000\u0000"+
		"/0\u0005l\u0000\u000001\u0005s\u0000\u00001\u0002\u0001\u0000\u0000\u0000"+
		"23\u0005.\u0000\u000034\u0005g\u0000\u000045\u0005l\u0000\u000056\u0005"+
		"o\u0000\u000067\u0005b\u0000\u000078\u0005a\u0000\u000089\u0005l\u0000"+
		"\u00009\u0004\u0001\u0000\u0000\u0000:;\u0005.\u0000\u0000;<\u0005d\u0000"+
		"\u0000<=\u0005e\u0000\u0000=>\u0005f\u0000\u0000>\u0006\u0001\u0000\u0000"+
		"\u0000?@\u0005:\u0000\u0000@\b\u0001\u0000\u0000\u0000AB\u0005a\u0000"+
		"\u0000BC\u0005r\u0000\u0000CD\u0005g\u0000\u0000DE\u0005s\u0000\u0000"+
		"E\n\u0001\u0000\u0000\u0000FG\u0005=\u0000\u0000G\f\u0001\u0000\u0000"+
		"\u0000HI\u0005,\u0000\u0000I\u000e\u0001\u0000\u0000\u0000JK\u0005l\u0000"+
		"\u0000KL\u0005o\u0000\u0000LM\u0005c\u0000\u0000MN\u0005a\u0000\u0000"+
		"NO\u0005l\u0000\u0000OP\u0005s\u0000\u0000P\u0010\u0001\u0000\u0000\u0000"+
		"QR\u0005r\u0000\u0000RS\u0003\u0019\f\u0000S\u0012\u0001\u0000\u0000\u0000"+
		"TY\u0003\u0017\u000b\u0000UX\u0003\u0017\u000b\u0000VX\u0007\u0000\u0000"+
		"\u0000WU\u0001\u0000\u0000\u0000WV\u0001\u0000\u0000\u0000X[\u0001\u0000"+
		"\u0000\u0000YW\u0001\u0000\u0000\u0000YZ\u0001\u0000\u0000\u0000Z\u0014"+
		"\u0001\u0000\u0000\u0000[Y\u0001\u0000\u0000\u0000\\]\u0003\u0013\t\u0000"+
		"]^\u0005(\u0000\u0000^_\u0005)\u0000\u0000_`\u0001\u0000\u0000\u0000`"+
		"a\u0006\n\u0000\u0000a\u0016\u0001\u0000\u0000\u0000bc\u0007\u0001\u0000"+
		"\u0000c\u0018\u0001\u0000\u0000\u0000df\u0005-\u0000\u0000ed\u0001\u0000"+
		"\u0000\u0000ef\u0001\u0000\u0000\u0000fh\u0001\u0000\u0000\u0000gi\u0002"+
		"09\u0000hg\u0001\u0000\u0000\u0000ij\u0001\u0000\u0000\u0000jh\u0001\u0000"+
		"\u0000\u0000jk\u0001\u0000\u0000\u0000k\u001a\u0001\u0000\u0000\u0000"+
		"lm\u0005\'\u0000\u0000mn\t\u0000\u0000\u0000no\u0005\'\u0000\u0000o\u001c"+
		"\u0001\u0000\u0000\u0000pq\u0005t\u0000\u0000qr\u0005r\u0000\u0000rs\u0005"+
		"u\u0000\u0000sz\u0005e\u0000\u0000tu\u0005f\u0000\u0000uv\u0005a\u0000"+
		"\u0000vw\u0005l\u0000\u0000wx\u0005s\u0000\u0000xz\u0005e\u0000\u0000"+
		"yp\u0001\u0000\u0000\u0000yt\u0001\u0000\u0000\u0000z\u001e\u0001\u0000"+
		"\u0000\u0000{|\u0005\"\u0000\u0000|}\u0003!\u0010\u0000}~\u0005\"\u0000"+
		"\u0000~\u007f\u0006\u000f\u0001\u0000\u007f \u0001\u0000\u0000\u0000\u0080"+
		"\u0082\b\u0002\u0000\u0000\u0081\u0080\u0001\u0000\u0000\u0000\u0082\u0085"+
		"\u0001\u0000\u0000\u0000\u0083\u0081\u0001\u0000\u0000\u0000\u0083\u0084"+
		"\u0001\u0000\u0000\u0000\u0084\"\u0001\u0000\u0000\u0000\u0085\u0083\u0001"+
		"\u0000\u0000\u0000\u0086\u0087\u0003\u0019\f\u0000\u0087\u008b\u0005."+
		"\u0000\u0000\u0088\u008a\u0003\u0019\f\u0000\u0089\u0088\u0001\u0000\u0000"+
		"\u0000\u008a\u008d\u0001\u0000\u0000\u0000\u008b\u0089\u0001\u0000\u0000"+
		"\u0000\u008b\u008c\u0001\u0000\u0000\u0000\u008c\u0095\u0001\u0000\u0000"+
		"\u0000\u008d\u008b\u0001\u0000\u0000\u0000\u008e\u0090\u0005.\u0000\u0000"+
		"\u008f\u0091\u0003\u0019\f\u0000\u0090\u008f\u0001\u0000\u0000\u0000\u0091"+
		"\u0092\u0001\u0000\u0000\u0000\u0092\u0090\u0001\u0000\u0000\u0000\u0092"+
		"\u0093\u0001\u0000\u0000\u0000\u0093\u0095\u0001\u0000\u0000\u0000\u0094"+
		"\u0086\u0001\u0000\u0000\u0000\u0094\u008e\u0001\u0000\u0000\u0000\u0095"+
		"$\u0001\u0000\u0000\u0000\u0096\u0098\u0007\u0003\u0000\u0000\u0097\u0096"+
		"\u0001\u0000\u0000\u0000\u0098\u0099\u0001\u0000\u0000\u0000\u0099\u0097"+
		"\u0001\u0000\u0000\u0000\u0099\u009a\u0001\u0000\u0000\u0000\u009a\u009b"+
		"\u0001\u0000\u0000\u0000\u009b\u009c\u0006\u0012\u0002\u0000\u009c&\u0001"+
		"\u0000\u0000\u0000\u009d\u00a1\u0005;\u0000\u0000\u009e\u00a0\t\u0000"+
		"\u0000\u0000\u009f\u009e\u0001\u0000\u0000\u0000\u00a0\u00a3\u0001\u0000"+
		"\u0000\u0000\u00a1\u00a2\u0001\u0000\u0000\u0000\u00a1\u009f\u0001\u0000"+
		"\u0000\u0000\u00a2\u00a5\u0001\u0000\u0000\u0000\u00a3\u00a1\u0001\u0000"+
		"\u0000\u0000\u00a4\u009d\u0001\u0000\u0000\u0000\u00a4\u00a5\u0001\u0000"+
		"\u0000\u0000\u00a5\u00a7\u0001\u0000\u0000\u0000\u00a6\u00a8\u0005\r\u0000"+
		"\u0000\u00a7\u00a6\u0001\u0000\u0000\u0000\u00a7\u00a8\u0001\u0000\u0000"+
		"\u0000\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u00aa\u0005\n\u0000\u0000"+
		"\u00aa(\u0001\u0000\u0000\u0000\u000e\u0000WYejy\u0083\u008b\u0092\u0094"+
		"\u0099\u00a1\u00a4\u00a7\u0003\u0001\n\u0000\u0001\u000f\u0001\u0001\u0012"+
		"\u0002";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}