// Generated from ./compiler/MathExpr.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep14.compiler;
import org.teachfx.antlr4.ep14.symtab.*;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MathExprLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, OP_ADD=5, OP_SUB=6, OP_MUL=7, OP_DIV=8, 
		NUM=9, ID=10, WS=11;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "OP_ADD", "OP_SUB", "OP_MUL", "OP_DIV", 
			"NUM", "ID", "WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "','", "'='", "'+'", "'-'", "'*'", "'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, "OP_ADD", "OP_SUB", "OP_MUL", "OP_DIV", 
			"NUM", "ID", "WS"
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

	 SymbolTable symtab;

	public MathExprLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "MathExpr.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\rJ\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\3\b\3"+
		"\t\3\t\3\n\6\n+\n\n\r\n\16\n,\3\n\3\n\6\n\61\n\n\r\n\16\n\62\5\n\65\n"+
		"\n\3\n\3\n\5\n9\n\n\3\n\6\n<\n\n\r\n\16\n=\5\n@\n\n\3\13\6\13C\n\13\r"+
		"\13\16\13D\3\f\3\f\3\f\3\f\2\2\r\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23"+
		"\13\25\f\27\r\3\2\7\3\2\62;\4\2GGgg\4\2--//\4\2C\\c|\5\2\13\f\17\17\""+
		"\"\2P\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2\r"+
		"\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3\2"+
		"\2\2\3\31\3\2\2\2\5\33\3\2\2\2\7\35\3\2\2\2\t\37\3\2\2\2\13!\3\2\2\2\r"+
		"#\3\2\2\2\17%\3\2\2\2\21\'\3\2\2\2\23*\3\2\2\2\25B\3\2\2\2\27F\3\2\2\2"+
		"\31\32\7*\2\2\32\4\3\2\2\2\33\34\7+\2\2\34\6\3\2\2\2\35\36\7.\2\2\36\b"+
		"\3\2\2\2\37 \7?\2\2 \n\3\2\2\2!\"\7-\2\2\"\f\3\2\2\2#$\7/\2\2$\16\3\2"+
		"\2\2%&\7,\2\2&\20\3\2\2\2\'(\7\61\2\2(\22\3\2\2\2)+\t\2\2\2*)\3\2\2\2"+
		"+,\3\2\2\2,*\3\2\2\2,-\3\2\2\2-\64\3\2\2\2.\60\7\60\2\2/\61\t\2\2\2\60"+
		"/\3\2\2\2\61\62\3\2\2\2\62\60\3\2\2\2\62\63\3\2\2\2\63\65\3\2\2\2\64."+
		"\3\2\2\2\64\65\3\2\2\2\65?\3\2\2\2\668\t\3\2\2\679\t\4\2\28\67\3\2\2\2"+
		"89\3\2\2\29;\3\2\2\2:<\t\2\2\2;:\3\2\2\2<=\3\2\2\2=;\3\2\2\2=>\3\2\2\2"+
		">@\3\2\2\2?\66\3\2\2\2?@\3\2\2\2@\24\3\2\2\2AC\t\5\2\2BA\3\2\2\2CD\3\2"+
		"\2\2DB\3\2\2\2DE\3\2\2\2E\26\3\2\2\2FG\t\6\2\2GH\3\2\2\2HI\b\f\2\2I\30"+
		"\3\2\2\2\n\2,\62\648=?D\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}