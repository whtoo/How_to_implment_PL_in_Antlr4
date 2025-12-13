// Generated from VMAssembler.g4 by ANTLR 4.13.2

package org.teachfx.antlr4.ep18.parser;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue", "this-escape"})
public class VMAssemblerParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.13.2", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, REG=9, 
		ID=10, FUNC=11, INT=12, CHAR=13, BOOL=14, STRING=15, FLOAT=16, WS=17, 
		NEWLINE=18;
	public static final int
		RULE_program = 0, RULE_globals = 1, RULE_globalVariable = 2, RULE_functionDeclaration = 3, 
		RULE_instr = 4, RULE_temp = 5, RULE_label = 6;
	private static String[] makeRuleNames() {
		return new String[] {
			"program", "globals", "globalVariable", "functionDeclaration", "instr", 
			"temp", "label"
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

	@Override
	public String getGrammarFileName() { return "VMAssembler.g4"; }

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
		public GlobalVariableContext globalVariable() {
			return getRuleContext(GlobalVariableContext.class,0);
		}
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
			setState(16);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
			case 1:
				{
				setState(14);
				globalVariable();
				}
				break;
			case 2:
				{
				setState(15);
				globals();
				}
				break;
			}
			setState(22); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(22);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
				case 1:
					{
					setState(18);
					functionDeclaration();
					}
					break;
				case 2:
					{
					setState(19);
					instr();
					}
					break;
				case 3:
					{
					setState(20);
					label();
					}
					break;
				case 4:
					{
					setState(21);
					match(NEWLINE);
					}
					break;
				}
				}
				setState(24); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & 263176L) != 0) );
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
			setState(29);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==NEWLINE) {
				{
				{
				setState(26);
				match(NEWLINE);
				}
				}
				setState(31);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(32);
			match(T__0);
			setState(33);
			((GlobalsContext)_localctx).intVal = match(INT);
			setState(34);
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
	public static class GlobalVariableContext extends ParserRuleContext {
		public Token type;
		public Token name;
		public TerminalNode NEWLINE() { return getToken(VMAssemblerParser.NEWLINE, 0); }
		public List<TerminalNode> ID() { return getTokens(VMAssemblerParser.ID); }
		public TerminalNode ID(int i) {
			return getToken(VMAssemblerParser.ID, i);
		}
		public GlobalVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_globalVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).enterGlobalVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof VMAssemblerListener ) ((VMAssemblerListener)listener).exitGlobalVariable(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof VMAssemblerVisitor ) return ((VMAssemblerVisitor<? extends T>)visitor).visitGlobalVariable(this);
			else return visitor.visitChildren(this);
		}
	}

	public final GlobalVariableContext globalVariable() throws RecognitionException {
		GlobalVariableContext _localctx = new GlobalVariableContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_globalVariable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			match(T__1);
			setState(37);
			((GlobalVariableContext)_localctx).type = match(ID);
			setState(38);
			((GlobalVariableContext)_localctx).name = match(ID);
			setState(39);
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
		enterRule(_localctx, 6, RULE_functionDeclaration);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(41);
			match(T__2);
			setState(42);
			((FunctionDeclarationContext)_localctx).name = match(ID);
			setState(43);
			match(T__3);
			setState(44);
			match(T__4);
			setState(45);
			match(T__5);
			setState(46);
			((FunctionDeclarationContext)_localctx).a = match(INT);
			setState(47);
			match(T__6);
			setState(48);
			match(T__7);
			setState(49);
			match(T__5);
			setState(50);
			((FunctionDeclarationContext)_localctx).lo = match(INT);
			setState(51);
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
		enterRule(_localctx, 8, RULE_instr);
		try {
			setState(73);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,4,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(53);
				((InstrContext)_localctx).op = match(ID);
				setState(54);
				match(NEWLINE);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(55);
				((InstrContext)_localctx).op = match(ID);
				setState(56);
				((InstrContext)_localctx).a = temp();
				setState(57);
				match(NEWLINE);
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(59);
				((InstrContext)_localctx).op = match(ID);
				setState(60);
				((InstrContext)_localctx).a = temp();
				setState(61);
				match(T__6);
				setState(62);
				((InstrContext)_localctx).b = temp();
				setState(63);
				match(NEWLINE);
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(65);
				((InstrContext)_localctx).op = match(ID);
				setState(66);
				((InstrContext)_localctx).a = temp();
				setState(67);
				match(T__6);
				setState(68);
				((InstrContext)_localctx).b = temp();
				setState(69);
				match(T__6);
				setState(70);
				((InstrContext)_localctx).c = temp();
				setState(71);
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
		enterRule(_localctx, 10, RULE_temp);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(75);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & 130560L) != 0)) ) {
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
		enterRule(_localctx, 12, RULE_label);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			match(ID);
			setState(78);
			match(T__3);
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
		"\u0004\u0001\u0012Q\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0001\u0000\u0001\u0000\u0003"+
		"\u0000\u0011\b\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0001\u0000\u0004"+
		"\u0000\u0017\b\u0000\u000b\u0000\f\u0000\u0018\u0001\u0001\u0005\u0001"+
		"\u001c\b\u0001\n\u0001\f\u0001\u001f\t\u0001\u0001\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0002\u0001\u0002\u0001"+
		"\u0002\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001\u0003\u0001"+
		"\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001\u0004\u0001"+
		"\u0004\u0001\u0004\u0001\u0004\u0003\u0004J\b\u0004\u0001\u0005\u0001"+
		"\u0005\u0001\u0006\u0001\u0006\u0001\u0006\u0001\u0006\u0000\u0000\u0007"+
		"\u0000\u0002\u0004\u0006\b\n\f\u0000\u0001\u0001\u0000\t\u0010S\u0000"+
		"\u0010\u0001\u0000\u0000\u0000\u0002\u001d\u0001\u0000\u0000\u0000\u0004"+
		"$\u0001\u0000\u0000\u0000\u0006)\u0001\u0000\u0000\u0000\bI\u0001\u0000"+
		"\u0000\u0000\nK\u0001\u0000\u0000\u0000\fM\u0001\u0000\u0000\u0000\u000e"+
		"\u0011\u0003\u0004\u0002\u0000\u000f\u0011\u0003\u0002\u0001\u0000\u0010"+
		"\u000e\u0001\u0000\u0000\u0000\u0010\u000f\u0001\u0000\u0000\u0000\u0010"+
		"\u0011\u0001\u0000\u0000\u0000\u0011\u0016\u0001\u0000\u0000\u0000\u0012"+
		"\u0017\u0003\u0006\u0003\u0000\u0013\u0017\u0003\b\u0004\u0000\u0014\u0017"+
		"\u0003\f\u0006\u0000\u0015\u0017\u0005\u0012\u0000\u0000\u0016\u0012\u0001"+
		"\u0000\u0000\u0000\u0016\u0013\u0001\u0000\u0000\u0000\u0016\u0014\u0001"+
		"\u0000\u0000\u0000\u0016\u0015\u0001\u0000\u0000\u0000\u0017\u0018\u0001"+
		"\u0000\u0000\u0000\u0018\u0016\u0001\u0000\u0000\u0000\u0018\u0019\u0001"+
		"\u0000\u0000\u0000\u0019\u0001\u0001\u0000\u0000\u0000\u001a\u001c\u0005"+
		"\u0012\u0000\u0000\u001b\u001a\u0001\u0000\u0000\u0000\u001c\u001f\u0001"+
		"\u0000\u0000\u0000\u001d\u001b\u0001\u0000\u0000\u0000\u001d\u001e\u0001"+
		"\u0000\u0000\u0000\u001e \u0001\u0000\u0000\u0000\u001f\u001d\u0001\u0000"+
		"\u0000\u0000 !\u0005\u0001\u0000\u0000!\"\u0005\f\u0000\u0000\"#\u0005"+
		"\u0012\u0000\u0000#\u0003\u0001\u0000\u0000\u0000$%\u0005\u0002\u0000"+
		"\u0000%&\u0005\n\u0000\u0000&\'\u0005\n\u0000\u0000\'(\u0005\u0012\u0000"+
		"\u0000(\u0005\u0001\u0000\u0000\u0000)*\u0005\u0003\u0000\u0000*+\u0005"+
		"\n\u0000\u0000+,\u0005\u0004\u0000\u0000,-\u0005\u0005\u0000\u0000-.\u0005"+
		"\u0006\u0000\u0000./\u0005\f\u0000\u0000/0\u0005\u0007\u0000\u000001\u0005"+
		"\b\u0000\u000012\u0005\u0006\u0000\u000023\u0005\f\u0000\u000034\u0005"+
		"\u0012\u0000\u00004\u0007\u0001\u0000\u0000\u000056\u0005\n\u0000\u0000"+
		"6J\u0005\u0012\u0000\u000078\u0005\n\u0000\u000089\u0003\n\u0005\u0000"+
		"9:\u0005\u0012\u0000\u0000:J\u0001\u0000\u0000\u0000;<\u0005\n\u0000\u0000"+
		"<=\u0003\n\u0005\u0000=>\u0005\u0007\u0000\u0000>?\u0003\n\u0005\u0000"+
		"?@\u0005\u0012\u0000\u0000@J\u0001\u0000\u0000\u0000AB\u0005\n\u0000\u0000"+
		"BC\u0003\n\u0005\u0000CD\u0005\u0007\u0000\u0000DE\u0003\n\u0005\u0000"+
		"EF\u0005\u0007\u0000\u0000FG\u0003\n\u0005\u0000GH\u0005\u0012\u0000\u0000"+
		"HJ\u0001\u0000\u0000\u0000I5\u0001\u0000\u0000\u0000I7\u0001\u0000\u0000"+
		"\u0000I;\u0001\u0000\u0000\u0000IA\u0001\u0000\u0000\u0000J\t\u0001\u0000"+
		"\u0000\u0000KL\u0007\u0000\u0000\u0000L\u000b\u0001\u0000\u0000\u0000"+
		"MN\u0005\n\u0000\u0000NO\u0005\u0004\u0000\u0000O\r\u0001\u0000\u0000"+
		"\u0000\u0005\u0010\u0016\u0018\u001dI";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}