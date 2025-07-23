// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

package org.teachfx.antlr4.ep18.parser;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class VMAssemblerParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.0-SNAPSHOT", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, REG=8, ID=9, FUNC=10, 
		INT=11, CHAR=12, BOOL=13, STRING=14, FLOAT=15, WS=16, NEWLINE=17;
	public static final int
		RULE_program = 0, RULE_globals = 1, RULE_functionDeclaration = 2, RULE_instr = 3, 
		RULE_temp = 4, RULE_label = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "globals", "functionDeclaration", "instr", "temp", "label"
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

	@Override
	public String getGrammarFileName() { return "java-escape"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public VMAssemblerParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ProgramContext extends ParserRuleContext {
		public GlobalsContext globals() {
			return getRuleContext(GlobalsContext.class,0);
		}
		public List<FunctionDeclarationContext> functionDeclaration() {
			return getRuleContexts(FunctionDeclarationContext.class);
		}
		public FunctionDeclarationContext functionDeclaration(int i) {
			return getRuleContext(FunctionDeclarationContext.class,i);
		}
		public List<InstrContext> instr() {
			return getRuleContexts(InstrContext.class);
		}
		public InstrContext instr(int i) {
			return getRuleContext(InstrContext.class,i);
		}
		public List<LabelContext> label() {
			return getRuleContexts(LabelContext.class);
		}
		public LabelContext label(int i) {
			return getRuleContext(LabelContext.class,i);
		}
		public List<TerminalNode> NEWLINE() { return getTokens(VMAssemblerParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(VMAssemblerParser.NEWLINE, i);
		}
		public ProgramContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_program; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).enterProgram(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).exitProgram(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VMAssemblerVisitor ) return ((VMAssemblerVisitor<? extends T>)visitor).visitProgram(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ProgramContext program() throws RecognitionException {
		ProgramContext _localctx = new ProgramContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_program);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(13);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(12);
				globals();
				}
				break;
			}
			setState(19); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(19);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(15);
					functionDeclaration();
					}
					break;
				case 2:
					{
					setState(16);
					instr();
					}
					break;
				case 3:
					{
					setState(17);
					label();
					}
					break;
				case 4:
					{
					setState(18);
					match(NEWLINE);
					}
					break;
				}
				}
				setState(21); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 131588L) != 0 );
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

	@SuppressWarnings("CheckReturnValue")
	public static class GlobalsContext extends ParserRuleContext {
		public Token intVal;
		public List<TerminalNode> NEWLINE() { return getTokens(VMAssemblerParser.NEWLINE); }
		public TerminalNode NEWLINE(int i) {
			return getToken(VMAssemblerParser.NEWLINE, i);
		}
		public TerminalNode INT() { return getToken(VMAssemblerParser.INT, 0); }
		public GlobalsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_globals; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).enterGlobals(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).exitGlobals(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VMAssemblerVisitor ) return ((VMAssemblerVisitor<? extends T>)visitor).visitGlobals(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GlobalsContext globals() throws RecognitionException {
		GlobalsContext _localctx = new GlobalsContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_globals);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(23);
				match(NEWLINE);
				}
				}
				setState(28);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(29);
			match(T__0);
			setState(30);
			((GlobalsContext)_localctx).intVal = match(INT);
			setState(31);
			match(NEWLINE);
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

	@SuppressWarnings("CheckReturnValue")
	public static class FunctionDeclarationContext extends ParserRuleContext {
		public Token name;
		public Token a;
		public Token lo;
		public TerminalNode NEWLINE() { return getToken(VMAssemblerParser.NEWLINE, 0); }
		public TerminalNode ID() { return getToken(VMAssemblerParser.ID, 0); }
		public List<TerminalNode> INT() { return getTokens(VMAssemblerParser.INT); }
		public TerminalNode INT(int i) {
			return getToken(VMAssemblerParser.INT, i);
		}
		public FunctionDeclarationContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionDeclaration; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).enterFunctionDeclaration(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).exitFunctionDeclaration(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VMAssemblerVisitor ) return ((VMAssemblerVisitor<? extends T>)visitor).visitFunctionDeclaration(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionDeclarationContext functionDeclaration() throws RecognitionException {
		FunctionDeclarationContext _localctx = new FunctionDeclarationContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_functionDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(33);
			match(T__1);
			setState(34);
			((FunctionDeclarationContext)_localctx).name = match(ID);
			setState(35);
			match(T__2);
			setState(36);
			match(T__3);
			setState(37);
			match(T__4);
			setState(38);
			((FunctionDeclarationContext)_localctx).a = match(INT);
			setState(39);
			match(T__5);
			setState(40);
			match(T__6);
			setState(41);
			match(T__4);
			setState(42);
			((FunctionDeclarationContext)_localctx).lo = match(INT);
			setState(43);
			match(NEWLINE);
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

	@SuppressWarnings("CheckReturnValue")
	public static class InstrContext extends ParserRuleContext {
		public Token op;
		public TempContext a;
		public TempContext b;
		public TempContext c;
		public TerminalNode NEWLINE() { return getToken(VMAssemblerParser.NEWLINE, 0); }
		public TerminalNode ID() { return getToken(VMAssemblerParser.ID, 0); }
		public List<TempContext> temp() {
			return getRuleContexts(TempContext.class);
		}
		public TempContext temp(int i) {
			return getRuleContext(TempContext.class,i);
		}
		public InstrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_instr; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).enterInstr(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).exitInstr(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VMAssemblerVisitor ) return ((VMAssemblerVisitor<? extends T>)visitor).visitInstr(this);
			else return visitor.visitChildren(this);
		}
	}

	public final InstrContext instr() throws RecognitionException {
		InstrContext _localctx = new InstrContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_instr);
		try {
			setState(65);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(45);
				((InstrContext)_localctx).op = match(ID);
				setState(46);
				match(NEWLINE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(47);
				((InstrContext)_localctx).op = match(ID);
				setState(48);
				((InstrContext)_localctx).a = temp();
				setState(49);
				match(NEWLINE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(51);
				((InstrContext)_localctx).op = match(ID);
				setState(52);
				((InstrContext)_localctx).a = temp();
				setState(53);
				match(T__5);
				setState(54);
				((InstrContext)_localctx).b = temp();
				setState(55);
				match(NEWLINE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(57);
				((InstrContext)_localctx).op = match(ID);
				setState(58);
				((InstrContext)_localctx).a = temp();
				setState(59);
				match(T__5);
				setState(60);
				((InstrContext)_localctx).b = temp();
				setState(61);
				match(T__5);
				setState(62);
				((InstrContext)_localctx).c = temp();
				setState(63);
				match(NEWLINE);
				}
				break;
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

	@SuppressWarnings("CheckReturnValue")
	public static class TempContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(VMAssemblerParser.ID, 0); }
		public TerminalNode REG() { return getToken(VMAssemblerParser.REG, 0); }
		public TerminalNode FUNC() { return getToken(VMAssemblerParser.FUNC, 0); }
		public TerminalNode INT() { return getToken(VMAssemblerParser.INT, 0); }
		public TerminalNode BOOL() { return getToken(VMAssemblerParser.BOOL, 0); }
		public TerminalNode CHAR() { return getToken(VMAssemblerParser.CHAR, 0); }
		public TerminalNode STRING() { return getToken(VMAssemblerParser.STRING, 0); }
		public TerminalNode FLOAT() { return getToken(VMAssemblerParser.FLOAT, 0); }
		public TempContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_temp; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).enterTemp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).exitTemp(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VMAssemblerVisitor ) return ((VMAssemblerVisitor<? extends T>)visitor).visitTemp(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TempContext temp() throws RecognitionException {
		TempContext _localctx = new TempContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_temp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(67);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 65280L) != 0) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
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

	@SuppressWarnings("CheckReturnValue")
	public static class LabelContext extends ParserRuleContext {
		public TerminalNode ID() { return getToken(VMAssemblerParser.ID, 0); }
		public LabelContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_label; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).enterLabel(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).exitLabel(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VMAssemblerVisitor ) return ((VMAssemblerVisitor<? extends T>)visitor).visitLabel(this);
			else return visitor.visitChildren(this);
		}
	}

	public final LabelContext label() throws RecognitionException {
		LabelContext _localctx = new LabelContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_label);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(69);
			match(ID);
			setState(70);
			match(T__2);
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
		"\u0004\u0001\u0011I\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0001\u0000\u0003\u0000\u000e\b\u0000\u0001\u0000\u0001"+
		"\u0000\u0001\u0000\u0001\u0000\u0004\u0000\u0014\b\u0000\u000b\u0000\f"+
		"\u0000\u0015\u0001\u0001\u0005\u0001\u0019\b\u0001\n\u0001\f\u0001\u001c"+
		"\t\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0003\u0003B\b\u0003\u0001\u0004\u0001\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0005\u0001\u0005\u0000\u0000\u0006\u0000\u0002\u0004\u0006"+
		"\b\n\u0000\u0001\u0001\u0000\b\u000fK\u0000\r\u0001\u0000\u0000\u0000"+
		"\u0002\u001a\u0001\u0000\u0000\u0000\u0004!\u0001\u0000\u0000\u0000\u0006"+
		"A\u0001\u0000\u0000\u0000\bC\u0001\u0000\u0000\u0000\nE\u0001\u0000\u0000"+
		"\u0000\f\u000e\u0003\u0002\u0001\u0000\r\f\u0001\u0000\u0000\u0000\r\u000e"+
		"\u0001\u0000\u0000\u0000\u000e\u0013\u0001\u0000\u0000\u0000\u000f\u0014"+
		"\u0003\u0004\u0002\u0000\u0010\u0014\u0003\u0006\u0003\u0000\u0011\u0014"+
		"\u0003\n\u0005\u0000\u0012\u0014\u0005\u0011\u0000\u0000\u0013\u000f\u0001"+
		"\u0000\u0000\u0000\u0013\u0010\u0001\u0000\u0000\u0000\u0013\u0011\u0001"+
		"\u0000\u0000\u0000\u0013\u0012\u0001\u0000\u0000\u0000\u0014\u0015\u0001"+
		"\u0000\u0000\u0000\u0015\u0013\u0001\u0000\u0000\u0000\u0015\u0016\u0001"+
		"\u0000\u0000\u0000\u0016\u0001\u0001\u0000\u0000\u0000\u0017\u0019\u0005"+
		"\u0011\u0000\u0000\u0018\u0017\u0001\u0000\u0000\u0000\u0019\u001c\u0001"+
		"\u0000\u0000\u0000\u001a\u0018\u0001\u0000\u0000\u0000\u001a\u001b\u0001"+
		"\u0000\u0000\u0000\u001b\u001d\u0001\u0000\u0000\u0000\u001c\u001a\u0001"+
		"\u0000\u0000\u0000\u001d\u001e\u0005\u0001\u0000\u0000\u001e\u001f\u0005"+
		"\u000b\u0000\u0000\u001f \u0005\u0011\u0000\u0000 \u0003\u0001\u0000\u0000"+
		"\u0000!\"\u0005\u0002\u0000\u0000\"#\u0005\t\u0000\u0000#$\u0005\u0003"+
		"\u0000\u0000$%\u0005\u0004\u0000\u0000%&\u0005\u0005\u0000\u0000&\'\u0005"+
		"\u000b\u0000\u0000\'(\u0005\u0006\u0000\u0000()\u0005\u0007\u0000\u0000"+
		")*\u0005\u0005\u0000\u0000*+\u0005\u000b\u0000\u0000+,\u0005\u0011\u0000"+
		"\u0000,\u0005\u0001\u0000\u0000\u0000-.\u0005\t\u0000\u0000.B\u0005\u0011"+
		"\u0000\u0000/0\u0005\t\u0000\u000001\u0003\b\u0004\u000012\u0005\u0011"+
		"\u0000\u00002B\u0001\u0000\u0000\u000034\u0005\t\u0000\u000045\u0003\b"+
		"\u0004\u000056\u0005\u0006\u0000\u000067\u0003\b\u0004\u000078\u0005\u0011"+
		"\u0000\u00008B\u0001\u0000\u0000\u00009:\u0005\t\u0000\u0000:;\u0003\b"+
		"\u0004\u0000;<\u0005\u0006\u0000\u0000<=\u0003\b\u0004\u0000=>\u0005\u0006"+
		"\u0000\u0000>?\u0003\b\u0004\u0000?@\u0005\u0011\u0000\u0000@B\u0001\u0000"+
		"\u0000\u0000A-\u0001\u0000\u0000\u0000A/\u0001\u0000\u0000\u0000A3\u0001"+
		"\u0000\u0000\u0000A9\u0001\u0000\u0000\u0000B\u0007\u0001\u0000\u0000"+
		"\u0000CD\u0007\u0000\u0000\u0000D\t\u0001\u0000\u0000\u0000EF\u0005\t"+
		"\u0000\u0000FG\u0005\u0003\u0000\u0000G\u000b\u0001\u0000\u0000\u0000"+
		"\u0005\r\u0013\u0015\u001aA";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}