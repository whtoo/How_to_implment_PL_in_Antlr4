// Generated from java-escape by ANTLR 4.11.0-SNAPSHOT

package org.teachfx.antlr4.ep20.parser;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast", "CheckReturnValue"})
public class CymbolParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.11.0-SNAPSHOT", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, T__20=21, T__21=22, T__22=23, T__23=24, 
		T__24=25, T__25=26, T__26=27, T__27=28, T__28=29, T__29=30, T__30=31, 
		T__31=32, T__32=33, T__33=34, ID=35, BOOLEAN=36, NULL=37, INT=38, FLOAT=39, 
		WS=40, CHAR=41, STRING=42, SLCOMMENT=43, COMMNET=44;
	public static final int
		RULE_file = 0, RULE_varDecl = 1, RULE_primaryType = 2, RULE_functionDecl = 3, 
		RULE_formalParameters = 4, RULE_formalParameter = 5, RULE_block = 6, RULE_statetment = 7, 
		RULE_expr = 8, RULE_arrayInitializer = 9, RULE_primary = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "varDecl", "primaryType", "functionDecl", "formalParameters", 
			"formalParameter", "block", "statetment", "expr", "arrayInitializer", 
			"primary"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "']'", "'='", "';'", "'float'", "'int'", "'void'", "'bool'", 
			"'string'", "'object'", "'('", "')'", "','", "'{'", "'}'", "'return'", 
			"'if'", "'else'", "'while'", "'break'", "'continue'", "'-'", "'!'", "'*'", 
			"'/'", "'%'", "'+'", "'=='", "'!='", "'>'", "'>='", "'<'", "'<='", "'&&'", 
			null, null, "'null'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, null, null, "ID", 
			"BOOLEAN", "NULL", "INT", "FLOAT", "WS", "CHAR", "STRING", "SLCOMMENT", 
			"COMMNET"
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

	public CymbolParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@SuppressWarnings("CheckReturnValue")
	public static class FileContext extends ParserRuleContext {
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
	 
		public FileContext() { }
		public void copyFrom(FileContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class CompilationUnitContext extends FileContext {
		public List<FunctionDeclContext> functionDecl() {
			return getRuleContexts(FunctionDeclContext.class);
		}
		public FunctionDeclContext functionDecl(int i) {
			return getRuleContext(FunctionDeclContext.class,i);
		}
		public List<VarDeclContext> varDecl() {
			return getRuleContexts(VarDeclContext.class);
		}
		public VarDeclContext varDecl(int i) {
			return getRuleContext(VarDeclContext.class,i);
		}
		public CompilationUnitContext(FileContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterCompilationUnit(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitCompilationUnit(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitCompilationUnit(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			_localctx = new CompilationUnitContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(24); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				setState(24);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,0,_ctx) ) {
				case 1:
					{
					setState(22);
					functionDecl();
					}
					break;
				case 2:
					{
					setState(23);
					varDecl();
					}
					break;
				}
				}
				setState(26); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( ((_la) & ~0x3f) == 0 && ((1L << _la) & 2016L) != 0 );
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
	public static class VarDeclContext extends ParserRuleContext {
		public PrimaryTypeContext primaryType() {
			return getRuleContext(PrimaryTypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ArrayInitializerContext arrayInitializer() {
			return getRuleContext(ArrayInitializerContext.class,0);
		}
		public VarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterVarDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitVarDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitVarDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final VarDeclContext varDecl() throws RecognitionException {
		VarDeclContext _localctx = new VarDeclContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_varDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(28);
			primaryType();
			setState(29);
			match(ID);
			setState(34);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(30);
				match(T__0);
				setState(31);
				expr(0);
				setState(32);
				match(T__1);
				}
			}

			setState(41);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__2) {
				{
				setState(36);
				match(T__2);
				setState(39);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__10:
				case T__21:
				case T__22:
				case ID:
				case BOOLEAN:
				case INT:
				case FLOAT:
				case CHAR:
				case STRING:
					{
					setState(37);
					expr(0);
					}
					break;
				case T__13:
					{
					setState(38);
					arrayInitializer();
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
			}

			setState(43);
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

	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryTypeContext extends ParserRuleContext {
		public PrimaryTypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primaryType; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterPrimaryType(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitPrimaryType(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryTypeContext primaryType() throws RecognitionException {
		PrimaryTypeContext _localctx = new PrimaryTypeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_primaryType);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(45);
			_la = _input.LA(1);
			if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 2016L) != 0) ) {
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
	public static class FunctionDeclContext extends ParserRuleContext {
		public PrimaryTypeContext retType;
		public Token funcName;
		public FormalParametersContext params;
		public BlockContext blockDef;
		public PrimaryTypeContext primaryType() {
			return getRuleContext(PrimaryTypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public FormalParametersContext formalParameters() {
			return getRuleContext(FormalParametersContext.class,0);
		}
		public FunctionDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_functionDecl; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterFunctionDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitFunctionDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitFunctionDecl(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FunctionDeclContext functionDecl() throws RecognitionException {
		FunctionDeclContext _localctx = new FunctionDeclContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_functionDecl);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(47);
			((FunctionDeclContext)_localctx).retType = primaryType();
			setState(48);
			((FunctionDeclContext)_localctx).funcName = match(ID);
			setState(49);
			match(T__10);
			setState(51);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (((_la) & ~0x3f) == 0 && ((1L << _la) & 2016L) != 0) {
				{
				setState(50);
				((FunctionDeclContext)_localctx).params = formalParameters();
				}
			}

			setState(53);
			match(T__11);
			setState(54);
			((FunctionDeclContext)_localctx).blockDef = block();
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
	public static class FormalParametersContext extends ParserRuleContext {
		public List<FormalParameterContext> formalParameter() {
			return getRuleContexts(FormalParameterContext.class);
		}
		public FormalParameterContext formalParameter(int i) {
			return getRuleContext(FormalParameterContext.class,i);
		}
		public FormalParametersContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameters; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterFormalParameters(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitFormalParameters(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitFormalParameters(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParametersContext formalParameters() throws RecognitionException {
		FormalParametersContext _localctx = new FormalParametersContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_formalParameters);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(56);
			formalParameter();
			setState(61);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__12) {
				{
				{
				setState(57);
				match(T__12);
				setState(58);
				formalParameter();
				}
				}
				setState(63);
				_errHandler.sync(this);
				_la = _input.LA(1);
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
	public static class FormalParameterContext extends ParserRuleContext {
		public PrimaryTypeContext primaryType() {
			return getRuleContext(PrimaryTypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public FormalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameter; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterFormalParameter(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitFormalParameter(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitFormalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterContext formalParameter() throws RecognitionException {
		FormalParameterContext _localctx = new FormalParameterContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_formalParameter);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(64);
			primaryType();
			setState(65);
			match(ID);
			setState(68);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(66);
				match(T__0);
				setState(67);
				match(T__1);
				}
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
	public static class BlockContext extends ParserRuleContext {
		public StatetmentContext stmts;
		public List<StatetmentContext> statetment() {
			return getRuleContexts(StatetmentContext.class);
		}
		public StatetmentContext statetment(int i) {
			return getRuleContext(StatetmentContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitBlock(this);
			else return visitor.visitChildren(this);
		}
	}

	public final BlockContext block() throws RecognitionException {
		BlockContext _localctx = new BlockContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_block);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(70);
			match(T__13);
			setState(74);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (((_la) & ~0x3f) == 0 && ((1L << _la) & 7524799172576L) != 0) {
				{
				{
				setState(71);
				((BlockContext)_localctx).stmts = statetment();
				}
				}
				setState(76);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(77);
			match(T__14);
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
	public static class StatetmentContext extends ParserRuleContext {
		public StatetmentContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statetment; }
	 
		public StatetmentContext() { }
		public void copyFrom(StatetmentContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatBlockContext extends StatetmentContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public StatBlockContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterStatBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitStatBlock(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitStatBlock(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatVarDeclContext extends StatetmentContext {
		public VarDeclContext varDecl() {
			return getRuleContext(VarDeclContext.class,0);
		}
		public StatVarDeclContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterStatVarDecl(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitStatVarDecl(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitStatVarDecl(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprStatContext extends StatetmentContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprStatContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprStat(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprStat(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprStat(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StateWhileContext extends StatetmentContext {
		public ExprContext cond;
		public StatetmentContext then;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public StatetmentContext statetment() {
			return getRuleContext(StatetmentContext.class,0);
		}
		public StateWhileContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterStateWhile(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitStateWhile(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitStateWhile(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VisitContinueContext extends StatetmentContext {
		public VisitContinueContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterVisitContinue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitVisitContinue(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitVisitContinue(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatAssignContext extends StatetmentContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public StatAssignContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterStatAssign(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitStatAssign(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitStatAssign(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StatReturnContext extends StatetmentContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public StatReturnContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterStatReturn(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitStatReturn(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitStatReturn(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class StateConditionContext extends StatetmentContext {
		public ExprContext cond;
		public StatetmentContext then;
		public StatetmentContext elseDo;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public List<StatetmentContext> statetment() {
			return getRuleContexts(StatetmentContext.class);
		}
		public StatetmentContext statetment(int i) {
			return getRuleContext(StatetmentContext.class,i);
		}
		public StateConditionContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterStateCondition(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitStateCondition(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitStateCondition(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class VisitBreakContext extends StatetmentContext {
		public VisitBreakContext(StatetmentContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterVisitBreak(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitVisitBreak(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitVisitBreak(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatetmentContext statetment() throws RecognitionException {
		StatetmentContext _localctx = new StatetmentContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_statetment);
		int _la;
		try {
			setState(113);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
			case 1:
				_localctx = new StatVarDeclContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(79);
				varDecl();
				}
				break;
			case 2:
				_localctx = new StatReturnContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(80);
				match(T__15);
				setState(82);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (((_la) & ~0x3f) == 0 && ((1L << _la) & 7524795287552L) != 0) {
					{
					setState(81);
					expr(0);
					}
				}

				setState(84);
				match(T__3);
				}
				break;
			case 3:
				_localctx = new StateConditionContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(85);
				match(T__16);
				setState(86);
				match(T__10);
				setState(87);
				((StateConditionContext)_localctx).cond = expr(0);
				setState(88);
				match(T__11);
				setState(89);
				((StateConditionContext)_localctx).then = statetment();
				setState(92);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
				case 1:
					{
					setState(90);
					match(T__17);
					setState(91);
					((StateConditionContext)_localctx).elseDo = statetment();
					}
					break;
				}
				}
				break;
			case 4:
				_localctx = new StateWhileContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(94);
				match(T__18);
				setState(95);
				match(T__10);
				setState(96);
				((StateWhileContext)_localctx).cond = expr(0);
				setState(97);
				match(T__11);
				setState(98);
				((StateWhileContext)_localctx).then = statetment();
				}
				break;
			case 5:
				_localctx = new VisitBreakContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(100);
				match(T__19);
				setState(101);
				match(T__3);
				}
				break;
			case 6:
				_localctx = new VisitContinueContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(102);
				match(T__20);
				setState(103);
				match(T__3);
				}
				break;
			case 7:
				_localctx = new StatAssignContext(_localctx);
				enterOuterAlt(_localctx, 7);
				{
				setState(104);
				expr(0);
				setState(105);
				match(T__2);
				setState(106);
				expr(0);
				setState(107);
				match(T__3);
				}
				break;
			case 8:
				_localctx = new ExprStatContext(_localctx);
				enterOuterAlt(_localctx, 8);
				{
				setState(109);
				expr(0);
				setState(110);
				match(T__3);
				}
				break;
			case 9:
				_localctx = new StatBlockContext(_localctx);
				enterOuterAlt(_localctx, 9);
				{
				setState(112);
				block();
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
	public static class ExprContext extends ParserRuleContext {
		public ExprContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expr; }
	 
		public ExprContext() { }
		public void copyFrom(ExprContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprBinaryContext extends ExprContext {
		public Token o;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprBinaryContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprBinary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprBinary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprBinary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprLogicalAndContext extends ExprContext {
		public Token o;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprLogicalAndContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprLogicalAnd(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprLogicalAnd(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprLogicalAnd(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprGroupContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprGroupContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprGroup(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprGroup(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprGroup(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprUnaryContext extends ExprContext {
		public Token o;
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprUnaryContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprUnary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprUnary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprUnary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprArrayAccessContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprArrayAccessContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprArrayAccess(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprArrayAccess(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprArrayAccess(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprPrimaryContext extends ExprContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public ExprPrimaryContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprPrimary(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprPrimary(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class ExprFuncCallContext extends ExprContext {
		public ExprContext callFunc;
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprFuncCallContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterExprFuncCall(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitExprFuncCall(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprFuncCall(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprContext expr() throws RecognitionException {
		return expr(0);
	}

	private ExprContext expr(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExprContext _localctx = new ExprContext(_ctx, _parentState);
		ExprContext _prevctx = _localctx;
		int _startState = 16;
		enterRecursionRule(_localctx, 16, RULE_expr, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(125);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case T__21:
				{
				_localctx = new ExprUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(116);
				((ExprUnaryContext)_localctx).o = match(T__21);
				setState(117);
				expr(8);
				}
				break;
			case T__22:
				{
				_localctx = new ExprUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(118);
				((ExprUnaryContext)_localctx).o = match(T__22);
				setState(119);
				expr(7);
				}
				break;
			case ID:
			case BOOLEAN:
			case INT:
			case FLOAT:
			case CHAR:
			case STRING:
				{
				_localctx = new ExprPrimaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(120);
				primary();
				}
				break;
			case T__10:
				{
				_localctx = new ExprGroupContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(121);
				match(T__10);
				setState(122);
				expr(0);
				setState(123);
				match(T__11);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			_ctx.stop = _input.LT(-1);
			setState(159);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(157);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,15,_ctx) ) {
					case 1:
						{
						_localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(127);
						if (!(precpred(_ctx, 6))) throw new FailedPredicateException(this, "precpred(_ctx, 6)");
						setState(128);
						((ExprBinaryContext)_localctx).o = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 117440512L) != 0) ) {
							((ExprBinaryContext)_localctx).o = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(129);
						expr(7);
						}
						break;
					case 2:
						{
						_localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(130);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(131);
						((ExprBinaryContext)_localctx).o = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==T__21 || _la==T__26) ) {
							((ExprBinaryContext)_localctx).o = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(132);
						expr(6);
						}
						break;
					case 3:
						{
						_localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(133);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(134);
						((ExprBinaryContext)_localctx).o = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((_la) & ~0x3f) == 0 && ((1L << _la) & 16911433728L) != 0) ) {
							((ExprBinaryContext)_localctx).o = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(135);
						expr(5);
						}
						break;
					case 4:
						{
						_localctx = new ExprLogicalAndContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(136);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(137);
						((ExprLogicalAndContext)_localctx).o = match(T__33);
						setState(138);
						expr(4);
						}
						break;
					case 5:
						{
						_localctx = new ExprFuncCallContext(new ExprContext(_parentctx, _parentState));
						((ExprFuncCallContext)_localctx).callFunc = _prevctx;
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(139);
						if (!(precpred(_ctx, 10))) throw new FailedPredicateException(this, "precpred(_ctx, 10)");
						setState(140);
						match(T__10);
						setState(149);
						_errHandler.sync(this);
						_la = _input.LA(1);
						if (((_la) & ~0x3f) == 0 && ((1L << _la) & 7524795287552L) != 0) {
							{
							setState(141);
							expr(0);
							setState(146);
							_errHandler.sync(this);
							_la = _input.LA(1);
							while (_la==T__12) {
								{
								{
								setState(142);
								match(T__12);
								setState(143);
								expr(0);
								}
								}
								setState(148);
								_errHandler.sync(this);
								_la = _input.LA(1);
							}
							}
						}

						setState(151);
						match(T__11);
						}
						break;
					case 6:
						{
						_localctx = new ExprArrayAccessContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(152);
						if (!(precpred(_ctx, 9))) throw new FailedPredicateException(this, "precpred(_ctx, 9)");
						setState(153);
						match(T__0);
						setState(154);
						expr(0);
						setState(155);
						match(T__1);
						}
						break;
					}
					} 
				}
				setState(161);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,16,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	@SuppressWarnings("CheckReturnValue")
	public static class ArrayInitializerContext extends ParserRuleContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ArrayInitializerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_arrayInitializer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterArrayInitializer(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitArrayInitializer(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitArrayInitializer(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ArrayInitializerContext arrayInitializer() throws RecognitionException {
		ArrayInitializerContext _localctx = new ArrayInitializerContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_arrayInitializer);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(162);
			match(T__13);
			setState(163);
			expr(0);
			setState(168);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__12) {
				{
				{
				setState(164);
				match(T__12);
				setState(165);
				expr(0);
				}
				}
				setState(170);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(171);
			match(T__14);
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
	public static class PrimaryContext extends ParserRuleContext {
		public PrimaryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_primary; }
	 
		public PrimaryContext() { }
		public void copyFrom(PrimaryContext ctx) {
			super.copyFrom(ctx);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryFLOATContext extends PrimaryContext {
		public TerminalNode FLOAT() { return getToken(CymbolParser.FLOAT, 0); }
		public PrimaryFLOATContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterPrimaryFLOAT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitPrimaryFLOAT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryFLOAT(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryCHARContext extends PrimaryContext {
		public TerminalNode CHAR() { return getToken(CymbolParser.CHAR, 0); }
		public PrimaryCHARContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterPrimaryCHAR(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitPrimaryCHAR(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryCHAR(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryIDContext extends PrimaryContext {
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public PrimaryIDContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterPrimaryID(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitPrimaryID(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryID(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimarySTRINGContext extends PrimaryContext {
		public TerminalNode STRING() { return getToken(CymbolParser.STRING, 0); }
		public PrimarySTRINGContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterPrimarySTRING(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitPrimarySTRING(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimarySTRING(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryBOOLContext extends PrimaryContext {
		public TerminalNode BOOLEAN() { return getToken(CymbolParser.BOOLEAN, 0); }
		public PrimaryBOOLContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterPrimaryBOOL(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitPrimaryBOOL(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryBOOL(this);
			else return visitor.visitChildren(this);
		}
	}
	@SuppressWarnings("CheckReturnValue")
	public static class PrimaryINTContext extends PrimaryContext {
		public TerminalNode INT() { return getToken(CymbolParser.INT, 0); }
		public PrimaryINTContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).enterPrimaryINT(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof CymbolListener ) ((CymbolListener)listener).exitPrimaryINT(this);
		}
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryINT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_primary);
		try {
			setState(179);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				_localctx = new PrimaryIDContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(173);
				match(ID);
				}
				break;
			case INT:
				_localctx = new PrimaryINTContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(174);
				match(INT);
				}
				break;
			case FLOAT:
				_localctx = new PrimaryFLOATContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(175);
				match(FLOAT);
				}
				break;
			case CHAR:
				_localctx = new PrimaryCHARContext(_localctx);
				enterOuterAlt(_localctx, 4);
				{
				setState(176);
				match(CHAR);
				}
				break;
			case STRING:
				_localctx = new PrimarySTRINGContext(_localctx);
				enterOuterAlt(_localctx, 5);
				{
				setState(177);
				match(STRING);
				}
				break;
			case BOOLEAN:
				_localctx = new PrimaryBOOLContext(_localctx);
				enterOuterAlt(_localctx, 6);
				{
				setState(178);
				match(BOOLEAN);
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

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 8:
			return expr_sempred((ExprContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expr_sempred(ExprContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 6);
		case 1:
			return precpred(_ctx, 5);
		case 2:
			return precpred(_ctx, 4);
		case 3:
			return precpred(_ctx, 3);
		case 4:
			return precpred(_ctx, 10);
		case 5:
			return precpred(_ctx, 9);
		}
		return true;
	}

	public static final String _serializedATN =
		"\u0004\u0001,\u00b6\u0002\u0000\u0007\u0000\u0002\u0001\u0007\u0001\u0002"+
		"\u0002\u0007\u0002\u0002\u0003\u0007\u0003\u0002\u0004\u0007\u0004\u0002"+
		"\u0005\u0007\u0005\u0002\u0006\u0007\u0006\u0002\u0007\u0007\u0007\u0002"+
		"\b\u0007\b\u0002\t\u0007\t\u0002\n\u0007\n\u0001\u0000\u0001\u0000\u0004"+
		"\u0000\u0019\b\u0000\u000b\u0000\f\u0000\u001a\u0001\u0001\u0001\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001#\b\u0001"+
		"\u0001\u0001\u0001\u0001\u0001\u0001\u0003\u0001(\b\u0001\u0003\u0001"+
		"*\b\u0001\u0001\u0001\u0001\u0001\u0001\u0002\u0001\u0002\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0003\u0003\u00034\b\u0003\u0001\u0003"+
		"\u0001\u0003\u0001\u0003\u0001\u0004\u0001\u0004\u0001\u0004\u0005\u0004"+
		"<\b\u0004\n\u0004\f\u0004?\t\u0004\u0001\u0005\u0001\u0005\u0001\u0005"+
		"\u0001\u0005\u0003\u0005E\b\u0005\u0001\u0006\u0001\u0006\u0005\u0006"+
		"I\b\u0006\n\u0006\f\u0006L\t\u0006\u0001\u0006\u0001\u0006\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0003\u0007S\b\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0003\u0007]\b\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007\u0001\u0007"+
		"\u0001\u0007\u0001\u0007\u0001\u0007\u0003\u0007r\b\u0007\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0003"+
		"\b~\b\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001\b\u0001"+
		"\b\u0005\b\u0091\b\b\n\b\f\b\u0094\t\b\u0003\b\u0096\b\b\u0001\b\u0001"+
		"\b\u0001\b\u0001\b\u0001\b\u0001\b\u0005\b\u009e\b\b\n\b\f\b\u00a1\t\b"+
		"\u0001\t\u0001\t\u0001\t\u0001\t\u0005\t\u00a7\b\t\n\t\f\t\u00aa\t\t\u0001"+
		"\t\u0001\t\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0001\n\u0003\n\u00b4"+
		"\b\n\u0001\n\u0000\u0001\u0010\u000b\u0000\u0002\u0004\u0006\b\n\f\u000e"+
		"\u0010\u0012\u0014\u0000\u0004\u0001\u0000\u0005\n\u0001\u0000\u0018\u001a"+
		"\u0002\u0000\u0016\u0016\u001b\u001b\u0001\u0000\u001c!\u00ce\u0000\u0018"+
		"\u0001\u0000\u0000\u0000\u0002\u001c\u0001\u0000\u0000\u0000\u0004-\u0001"+
		"\u0000\u0000\u0000\u0006/\u0001\u0000\u0000\u0000\b8\u0001\u0000\u0000"+
		"\u0000\n@\u0001\u0000\u0000\u0000\fF\u0001\u0000\u0000\u0000\u000eq\u0001"+
		"\u0000\u0000\u0000\u0010}\u0001\u0000\u0000\u0000\u0012\u00a2\u0001\u0000"+
		"\u0000\u0000\u0014\u00b3\u0001\u0000\u0000\u0000\u0016\u0019\u0003\u0006"+
		"\u0003\u0000\u0017\u0019\u0003\u0002\u0001\u0000\u0018\u0016\u0001\u0000"+
		"\u0000\u0000\u0018\u0017\u0001\u0000\u0000\u0000\u0019\u001a\u0001\u0000"+
		"\u0000\u0000\u001a\u0018\u0001\u0000\u0000\u0000\u001a\u001b\u0001\u0000"+
		"\u0000\u0000\u001b\u0001\u0001\u0000\u0000\u0000\u001c\u001d\u0003\u0004"+
		"\u0002\u0000\u001d\"\u0005#\u0000\u0000\u001e\u001f\u0005\u0001\u0000"+
		"\u0000\u001f \u0003\u0010\b\u0000 !\u0005\u0002\u0000\u0000!#\u0001\u0000"+
		"\u0000\u0000\"\u001e\u0001\u0000\u0000\u0000\"#\u0001\u0000\u0000\u0000"+
		"#)\u0001\u0000\u0000\u0000$\'\u0005\u0003\u0000\u0000%(\u0003\u0010\b"+
		"\u0000&(\u0003\u0012\t\u0000\'%\u0001\u0000\u0000\u0000\'&\u0001\u0000"+
		"\u0000\u0000(*\u0001\u0000\u0000\u0000)$\u0001\u0000\u0000\u0000)*\u0001"+
		"\u0000\u0000\u0000*+\u0001\u0000\u0000\u0000+,\u0005\u0004\u0000\u0000"+
		",\u0003\u0001\u0000\u0000\u0000-.\u0007\u0000\u0000\u0000.\u0005\u0001"+
		"\u0000\u0000\u0000/0\u0003\u0004\u0002\u000001\u0005#\u0000\u000013\u0005"+
		"\u000b\u0000\u000024\u0003\b\u0004\u000032\u0001\u0000\u0000\u000034\u0001"+
		"\u0000\u0000\u000045\u0001\u0000\u0000\u000056\u0005\f\u0000\u000067\u0003"+
		"\f\u0006\u00007\u0007\u0001\u0000\u0000\u00008=\u0003\n\u0005\u00009:"+
		"\u0005\r\u0000\u0000:<\u0003\n\u0005\u0000;9\u0001\u0000\u0000\u0000<"+
		"?\u0001\u0000\u0000\u0000=;\u0001\u0000\u0000\u0000=>\u0001\u0000\u0000"+
		"\u0000>\t\u0001\u0000\u0000\u0000?=\u0001\u0000\u0000\u0000@A\u0003\u0004"+
		"\u0002\u0000AD\u0005#\u0000\u0000BC\u0005\u0001\u0000\u0000CE\u0005\u0002"+
		"\u0000\u0000DB\u0001\u0000\u0000\u0000DE\u0001\u0000\u0000\u0000E\u000b"+
		"\u0001\u0000\u0000\u0000FJ\u0005\u000e\u0000\u0000GI\u0003\u000e\u0007"+
		"\u0000HG\u0001\u0000\u0000\u0000IL\u0001\u0000\u0000\u0000JH\u0001\u0000"+
		"\u0000\u0000JK\u0001\u0000\u0000\u0000KM\u0001\u0000\u0000\u0000LJ\u0001"+
		"\u0000\u0000\u0000MN\u0005\u000f\u0000\u0000N\r\u0001\u0000\u0000\u0000"+
		"Or\u0003\u0002\u0001\u0000PR\u0005\u0010\u0000\u0000QS\u0003\u0010\b\u0000"+
		"RQ\u0001\u0000\u0000\u0000RS\u0001\u0000\u0000\u0000ST\u0001\u0000\u0000"+
		"\u0000Tr\u0005\u0004\u0000\u0000UV\u0005\u0011\u0000\u0000VW\u0005\u000b"+
		"\u0000\u0000WX\u0003\u0010\b\u0000XY\u0005\f\u0000\u0000Y\\\u0003\u000e"+
		"\u0007\u0000Z[\u0005\u0012\u0000\u0000[]\u0003\u000e\u0007\u0000\\Z\u0001"+
		"\u0000\u0000\u0000\\]\u0001\u0000\u0000\u0000]r\u0001\u0000\u0000\u0000"+
		"^_\u0005\u0013\u0000\u0000_`\u0005\u000b\u0000\u0000`a\u0003\u0010\b\u0000"+
		"ab\u0005\f\u0000\u0000bc\u0003\u000e\u0007\u0000cr\u0001\u0000\u0000\u0000"+
		"de\u0005\u0014\u0000\u0000er\u0005\u0004\u0000\u0000fg\u0005\u0015\u0000"+
		"\u0000gr\u0005\u0004\u0000\u0000hi\u0003\u0010\b\u0000ij\u0005\u0003\u0000"+
		"\u0000jk\u0003\u0010\b\u0000kl\u0005\u0004\u0000\u0000lr\u0001\u0000\u0000"+
		"\u0000mn\u0003\u0010\b\u0000no\u0005\u0004\u0000\u0000or\u0001\u0000\u0000"+
		"\u0000pr\u0003\f\u0006\u0000qO\u0001\u0000\u0000\u0000qP\u0001\u0000\u0000"+
		"\u0000qU\u0001\u0000\u0000\u0000q^\u0001\u0000\u0000\u0000qd\u0001\u0000"+
		"\u0000\u0000qf\u0001\u0000\u0000\u0000qh\u0001\u0000\u0000\u0000qm\u0001"+
		"\u0000\u0000\u0000qp\u0001\u0000\u0000\u0000r\u000f\u0001\u0000\u0000"+
		"\u0000st\u0006\b\uffff\uffff\u0000tu\u0005\u0016\u0000\u0000u~\u0003\u0010"+
		"\b\bvw\u0005\u0017\u0000\u0000w~\u0003\u0010\b\u0007x~\u0003\u0014\n\u0000"+
		"yz\u0005\u000b\u0000\u0000z{\u0003\u0010\b\u0000{|\u0005\f\u0000\u0000"+
		"|~\u0001\u0000\u0000\u0000}s\u0001\u0000\u0000\u0000}v\u0001\u0000\u0000"+
		"\u0000}x\u0001\u0000\u0000\u0000}y\u0001\u0000\u0000\u0000~\u009f\u0001"+
		"\u0000\u0000\u0000\u007f\u0080\n\u0006\u0000\u0000\u0080\u0081\u0007\u0001"+
		"\u0000\u0000\u0081\u009e\u0003\u0010\b\u0007\u0082\u0083\n\u0005\u0000"+
		"\u0000\u0083\u0084\u0007\u0002\u0000\u0000\u0084\u009e\u0003\u0010\b\u0006"+
		"\u0085\u0086\n\u0004\u0000\u0000\u0086\u0087\u0007\u0003\u0000\u0000\u0087"+
		"\u009e\u0003\u0010\b\u0005\u0088\u0089\n\u0003\u0000\u0000\u0089\u008a"+
		"\u0005\"\u0000\u0000\u008a\u009e\u0003\u0010\b\u0004\u008b\u008c\n\n\u0000"+
		"\u0000\u008c\u0095\u0005\u000b\u0000\u0000\u008d\u0092\u0003\u0010\b\u0000"+
		"\u008e\u008f\u0005\r\u0000\u0000\u008f\u0091\u0003\u0010\b\u0000\u0090"+
		"\u008e\u0001\u0000\u0000\u0000\u0091\u0094\u0001\u0000\u0000\u0000\u0092"+
		"\u0090\u0001\u0000\u0000\u0000\u0092\u0093\u0001\u0000\u0000\u0000\u0093"+
		"\u0096\u0001\u0000\u0000\u0000\u0094\u0092\u0001\u0000\u0000\u0000\u0095"+
		"\u008d\u0001\u0000\u0000\u0000\u0095\u0096\u0001\u0000\u0000\u0000\u0096"+
		"\u0097\u0001\u0000\u0000\u0000\u0097\u009e\u0005\f\u0000\u0000\u0098\u0099"+
		"\n\t\u0000\u0000\u0099\u009a\u0005\u0001\u0000\u0000\u009a\u009b\u0003"+
		"\u0010\b\u0000\u009b\u009c\u0005\u0002\u0000\u0000\u009c\u009e\u0001\u0000"+
		"\u0000\u0000\u009d\u007f\u0001\u0000\u0000\u0000\u009d\u0082\u0001\u0000"+
		"\u0000\u0000\u009d\u0085\u0001\u0000\u0000\u0000\u009d\u0088\u0001\u0000"+
		"\u0000\u0000\u009d\u008b\u0001\u0000\u0000\u0000\u009d\u0098\u0001\u0000"+
		"\u0000\u0000\u009e\u00a1\u0001\u0000\u0000\u0000\u009f\u009d\u0001\u0000"+
		"\u0000\u0000\u009f\u00a0\u0001\u0000\u0000\u0000\u00a0\u0011\u0001\u0000"+
		"\u0000\u0000\u00a1\u009f\u0001\u0000\u0000\u0000\u00a2\u00a3\u0005\u000e"+
		"\u0000\u0000\u00a3\u00a8\u0003\u0010\b\u0000\u00a4\u00a5\u0005\r\u0000"+
		"\u0000\u00a5\u00a7\u0003\u0010\b\u0000\u00a6\u00a4\u0001\u0000\u0000\u0000"+
		"\u00a7\u00aa\u0001\u0000\u0000\u0000\u00a8\u00a6\u0001\u0000\u0000\u0000"+
		"\u00a8\u00a9\u0001\u0000\u0000\u0000\u00a9\u00ab\u0001\u0000\u0000\u0000"+
		"\u00aa\u00a8\u0001\u0000\u0000\u0000\u00ab\u00ac\u0005\u000f\u0000\u0000"+
		"\u00ac\u0013\u0001\u0000\u0000\u0000\u00ad\u00b4\u0005#\u0000\u0000\u00ae"+
		"\u00b4\u0005&\u0000\u0000\u00af\u00b4\u0005\'\u0000\u0000\u00b0\u00b4"+
		"\u0005)\u0000\u0000\u00b1\u00b4\u0005*\u0000\u0000\u00b2\u00b4\u0005$"+
		"\u0000\u0000\u00b3\u00ad\u0001\u0000\u0000\u0000\u00b3\u00ae\u0001\u0000"+
		"\u0000\u0000\u00b3\u00af\u0001\u0000\u0000\u0000\u00b3\u00b0\u0001\u0000"+
		"\u0000\u0000\u00b3\u00b1\u0001\u0000\u0000\u0000\u00b3\u00b2\u0001\u0000"+
		"\u0000\u0000\u00b4\u0015\u0001\u0000\u0000\u0000\u0013\u0018\u001a\"\'"+
		")3=DJR\\q}\u0092\u0095\u009d\u009f\u00a8\u00b3";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}