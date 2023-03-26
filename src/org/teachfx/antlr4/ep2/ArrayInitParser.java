// Generated from ArrayInit.g4 by ANTLR 4.0
package org.teachfx.antlr4.ep2;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ArrayInitParser extends Parser {
	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__2=1, T__1=2, T__0=3, INT=4, WS=5;
	public static final String[] tokenNames = {
		"<INVALID>", "'{'", "','", "'}'", "INT", "WS"
	};
	public static final int
		RULE_array = 0, RULE_value = 1;
	public static final String[] ruleNames = {
		"array", "value"
	};

	@Override
	public String getGrammarFileName() { return "ArrayInit.g4"; }

	@Override
	public String[] getTokenNames() { return tokenNames; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public ArrayInitParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}
	public static class ArrayContext extends ParserRuleContext {
		public List<ValueContext> value() {
			return getRuleContexts(ValueContext.class);
		}
		public ValueContext value(int i) {
			return getRuleContext(ValueContext.class,i);
		}
		public ArrayContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_array; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArrayInitListener ) ((ArrayInitListener)listener).enterArray(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArrayInitListener ) ((ArrayInitListener)listener).exitArray(this);
		}
	}

	public final ArrayContext array() throws RecognitionException {
		ArrayContext _localctx = new ArrayContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_array);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(4); match(1);
			setState(5); value();
			setState(10);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==2) {
				{
				{
				setState(6); match(2);
				setState(7); value();
				}
				}
				setState(12);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(13); match(3);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ValueContext extends ParserRuleContext {
		public ArrayContext array() {
			return getRuleContext(ArrayContext.class,0);
		}
		public TerminalNode INT() { return getToken(ArrayInitParser.INT, 0); }
		public ValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_value; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof ArrayInitListener ) ((ArrayInitListener)listener).enterValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof ArrayInitListener ) ((ArrayInitListener)listener).exitValue(this);
		}
	}

	public final ValueContext value() throws RecognitionException {
		ValueContext _localctx = new ValueContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_value);
		try {
			setState(17);
			switch (_input.LA(1)) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(15); array();
				}
				break;
			case INT:
				enterOuterAlt(_localctx, 2);
				{
				setState(16); match(INT);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static final String _serializedATN =
		"\2\3\7\26\4\2\t\2\4\3\t\3\3\2\3\2\3\2\3\2\7\2\13\n\2\f\2\16\2\16\13\2"+
		"\3\2\3\2\3\3\3\3\5\3\24\n\3\3\3\2\4\2\4\2\2\25\2\6\3\2\2\2\4\23\3\2\2"+
		"\2\6\7\7\3\2\2\7\f\5\4\3\2\b\t\7\4\2\2\t\13\5\4\3\2\n\b\3\2\2\2\13\16"+
		"\3\2\2\2\f\n\3\2\2\2\f\r\3\2\2\2\r\17\3\2\2\2\16\f\3\2\2\2\17\20\7\5\2"+
		"\2\20\3\3\2\2\2\21\24\5\2\2\2\22\24\7\6\2\2\23\21\3\2\2\2\23\22\3\2\2"+
		"\2\24\5\3\2\2\2\4\f\23";
	public static final ATN _ATN =
		ATNSimulator.deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
	}
}