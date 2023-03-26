// Generated from Math.g4 by ANTLR 4.8
package org.teachfx.antlr4.ep11;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class MathLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, OP_ADD=3, OP_SUB=4, OP_MUL=5, OP_DIV=6, NUM=7, ID=8, WS=9;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "OP_ADD", "OP_SUB", "OP_MUL", "OP_DIV", "NUM", "ID", 
			"WS"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'('", "')'", "'+'", "'-'", "'*'", "'/'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, "OP_ADD", "OP_SUB", "OP_MUL", "OP_DIV", "NUM", "ID", 
			"WS"
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


	public MathLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "Math.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\13B\b\1\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\3\2\3\2"+
		"\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\7\3\7\3\b\6\b#\n\b\r\b\16\b$\3\b\3"+
		"\b\6\b)\n\b\r\b\16\b*\5\b-\n\b\3\b\3\b\5\b\61\n\b\3\b\6\b\64\n\b\r\b\16"+
		"\b\65\5\b8\n\b\3\t\6\t;\n\t\r\t\16\t<\3\n\3\n\3\n\3\n\2\2\13\3\3\5\4\7"+
		"\5\t\6\13\7\r\b\17\t\21\n\23\13\3\2\7\3\2\62;\4\2GGgg\4\2--//\4\2C\\c"+
		"|\5\2\13\f\17\17\"\"\2H\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2"+
		"\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\3\25"+
		"\3\2\2\2\5\27\3\2\2\2\7\31\3\2\2\2\t\33\3\2\2\2\13\35\3\2\2\2\r\37\3\2"+
		"\2\2\17\"\3\2\2\2\21:\3\2\2\2\23>\3\2\2\2\25\26\7*\2\2\26\4\3\2\2\2\27"+
		"\30\7+\2\2\30\6\3\2\2\2\31\32\7-\2\2\32\b\3\2\2\2\33\34\7/\2\2\34\n\3"+
		"\2\2\2\35\36\7,\2\2\36\f\3\2\2\2\37 \7\61\2\2 \16\3\2\2\2!#\t\2\2\2\""+
		"!\3\2\2\2#$\3\2\2\2$\"\3\2\2\2$%\3\2\2\2%,\3\2\2\2&(\7\60\2\2\')\t\2\2"+
		"\2(\'\3\2\2\2)*\3\2\2\2*(\3\2\2\2*+\3\2\2\2+-\3\2\2\2,&\3\2\2\2,-\3\2"+
		"\2\2-\67\3\2\2\2.\60\t\3\2\2/\61\t\4\2\2\60/\3\2\2\2\60\61\3\2\2\2\61"+
		"\63\3\2\2\2\62\64\t\2\2\2\63\62\3\2\2\2\64\65\3\2\2\2\65\63\3\2\2\2\65"+
		"\66\3\2\2\2\668\3\2\2\2\67.\3\2\2\2\678\3\2\2\28\20\3\2\2\29;\t\5\2\2"+
		":9\3\2\2\2;<\3\2\2\2<:\3\2\2\2<=\3\2\2\2=\22\3\2\2\2>?\t\6\2\2?@\3\2\2"+
		"\2@A\b\n\2\2A\24\3\2\2\2\n\2$*,\60\65\67<\3\2\3\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}