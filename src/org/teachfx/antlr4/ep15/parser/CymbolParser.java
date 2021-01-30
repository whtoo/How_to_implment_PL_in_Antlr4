// Generated from ./parser/Cymbol.g4 by ANTLR 4.8

package org.teachfx.antlr4.ep15.parser;

import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CymbolParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, T__15=16, T__16=17, 
		T__17=18, T__18=19, T__19=20, ID=21, INT=22, FLOAT=23, WS=24, SLCOMMENT=25;
	public static final int
		RULE_file = 0, RULE_varDecl = 1, RULE_type = 2, RULE_functionDecl = 3, 
		RULE_formalParameters = 4, RULE_formalParameter = 5, RULE_block = 6, RULE_stat = 7, 
		RULE_expr = 8, RULE_primary = 9, RULE_exprLst = 10;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "varDecl", "type", "functionDecl", "formalParameters", "formalParameter", 
			"block", "stat", "expr", "primary", "exprLst"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'='", "';'", "'float'", "'int'", "'void'", "'('", "')'", "','", 
			"'{'", "'}'", "'if'", "'then'", "'else'", "'return'", "'-'", "'!'", "'*'", 
			"'/'", "'+'", "'=='"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, null, 
			null, null, null, null, null, null, null, null, null, "ID", "INT", "FLOAT", 
			"WS", "SLCOMMENT"
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
	public String getGrammarFileName() { return "Cymbol.g4"; }

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
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4))) != 0) );
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

	public static class VarDeclContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public VarDeclContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_varDecl; }
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
			type();
			setState(29);
			match(ID);
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__0) {
				{
				setState(30);
				match(T__0);
				setState(31);
				expr(0);
				}
			}

			setState(34);
			match(T__1);
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

	public static class TypeContext extends ParserRuleContext {
		public TypeContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_type; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitType(this);
			else return visitor.visitChildren(this);
		}
	}

	public final TypeContext type() throws RecognitionException {
		TypeContext _localctx = new TypeContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_type);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(36);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4))) != 0)) ) {
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

	public static class FunctionDeclContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
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
			setState(38);
			type();
			setState(39);
			match(ID);
			setState(40);
			match(T__5);
			setState(42);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4))) != 0)) {
				{
				setState(41);
				formalParameters();
				}
			}

			setState(44);
			match(T__6);
			setState(45);
			block();
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
			setState(47);
			formalParameter();
			setState(52);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__7) {
				{
				{
				setState(48);
				match(T__7);
				setState(49);
				formalParameter();
				}
				}
				setState(54);
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

	public static class FormalParameterContext extends ParserRuleContext {
		public TypeContext type() {
			return getRuleContext(TypeContext.class,0);
		}
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public FormalParameterContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_formalParameter; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitFormalParameter(this);
			else return visitor.visitChildren(this);
		}
	}

	public final FormalParameterContext formalParameter() throws RecognitionException {
		FormalParameterContext _localctx = new FormalParameterContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_formalParameter);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(55);
			type();
			setState(56);
			match(ID);
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

	public static class BlockContext extends ParserRuleContext {
		public List<StatContext> stat() {
			return getRuleContexts(StatContext.class);
		}
		public StatContext stat(int i) {
			return getRuleContext(StatContext.class,i);
		}
		public BlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_block; }
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
			setState(58);
			match(T__8);
			setState(62);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__2) | (1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__8) | (1L << T__10) | (1L << T__13) | (1L << T__14) | (1L << T__15) | (1L << ID) | (1L << INT) | (1L << FLOAT))) != 0)) {
				{
				{
				setState(59);
				stat();
				}
				}
				setState(64);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(65);
			match(T__9);
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

	public static class StatContext extends ParserRuleContext {
		public BlockContext block() {
			return getRuleContext(BlockContext.class,0);
		}
		public VarDeclContext varDecl() {
			return getRuleContext(VarDeclContext.class,0);
		}
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public List<StatContext> stat() {
			return getRuleContexts(StatContext.class);
		}
		public StatContext stat(int i) {
			return getRuleContext(StatContext.class,i);
		}
		public StatContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stat; }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitStat(this);
			else return visitor.visitChildren(this);
		}
	}

	public final StatContext stat() throws RecognitionException {
		StatContext _localctx = new StatContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_stat);
		int _la;
		try {
			setState(90);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,8,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(67);
				block();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(68);
				varDecl();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(69);
				match(T__10);
				setState(70);
				expr(0);
				setState(71);
				match(T__11);
				setState(72);
				stat();
				setState(75);
				_errHandler.sync(this);
				switch ( getInterpreter().adaptivePredict(_input,6,_ctx) ) {
				case 1:
					{
					setState(73);
					match(T__12);
					setState(74);
					stat();
					}
					break;
				}
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(77);
				match(T__13);
				setState(79);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__14) | (1L << T__15) | (1L << ID) | (1L << INT) | (1L << FLOAT))) != 0)) {
					{
					setState(78);
					expr(0);
					}
				}

				setState(81);
				match(T__1);
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(82);
				expr(0);
				setState(83);
				match(T__0);
				setState(84);
				expr(0);
				setState(85);
				match(T__1);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(87);
				expr(0);
				setState(88);
				match(T__1);
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
	public static class ExprBinaryContext extends ExprContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprBinaryContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprBinary(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprGroupContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprGroupContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprGroup(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprUnaryContext extends ExprContext {
		public ExprContext expr() {
			return getRuleContext(ExprContext.class,0);
		}
		public ExprUnaryContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprUnary(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprPrimaryContext extends ExprContext {
		public PrimaryContext primary() {
			return getRuleContext(PrimaryContext.class,0);
		}
		public ExprPrimaryContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprPrimary(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class ExprFunCallContext extends ExprContext {
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public ExprLstContext exprLst() {
			return getRuleContext(ExprLstContext.class,0);
		}
		public ExprFunCallContext(ExprContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprFunCall(this);
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
			setState(108);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,10,_ctx) ) {
			case 1:
				{
				_localctx = new ExprFunCallContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(93);
				match(ID);
				setState(94);
				match(T__5);
				setState(96);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__5) | (1L << T__14) | (1L << T__15) | (1L << ID) | (1L << INT) | (1L << FLOAT))) != 0)) {
					{
					setState(95);
					exprLst();
					}
				}

				setState(98);
				match(T__6);
				}
				break;
			case 2:
				{
				_localctx = new ExprUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(99);
				match(T__14);
				setState(100);
				expr(7);
				}
				break;
			case 3:
				{
				_localctx = new ExprUnaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(101);
				match(T__15);
				setState(102);
				expr(6);
				}
				break;
			case 4:
				{
				_localctx = new ExprPrimaryContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(103);
				primary();
				}
				break;
			case 5:
				{
				_localctx = new ExprGroupContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(104);
				match(T__5);
				setState(105);
				expr(0);
				setState(106);
				match(T__6);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(121);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(119);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,11,_ctx) ) {
					case 1:
						{
						_localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(110);
						if (!(precpred(_ctx, 5))) throw new FailedPredicateException(this, "precpred(_ctx, 5)");
						setState(111);
						_la = _input.LA(1);
						if ( !(_la==T__16 || _la==T__17) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(112);
						expr(6);
						}
						break;
					case 2:
						{
						_localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(113);
						if (!(precpred(_ctx, 4))) throw new FailedPredicateException(this, "precpred(_ctx, 4)");
						setState(114);
						_la = _input.LA(1);
						if ( !(_la==T__14 || _la==T__18) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(115);
						expr(5);
						}
						break;
					case 3:
						{
						_localctx = new ExprBinaryContext(new ExprContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expr);
						setState(116);
						if (!(precpred(_ctx, 3))) throw new FailedPredicateException(this, "precpred(_ctx, 3)");
						setState(117);
						match(T__19);
						setState(118);
						expr(4);
						}
						break;
					}
					} 
				}
				setState(123);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,12,_ctx);
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
	public static class PrimaryFLOATContext extends PrimaryContext {
		public TerminalNode FLOAT() { return getToken(CymbolParser.FLOAT, 0); }
		public PrimaryFLOATContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryFLOAT(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrimaryIDContext extends PrimaryContext {
		public TerminalNode ID() { return getToken(CymbolParser.ID, 0); }
		public PrimaryIDContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryID(this);
			else return visitor.visitChildren(this);
		}
	}
	public static class PrimaryINTContext extends PrimaryContext {
		public TerminalNode INT() { return getToken(CymbolParser.INT, 0); }
		public PrimaryINTContext(PrimaryContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitPrimaryINT(this);
			else return visitor.visitChildren(this);
		}
	}

	public final PrimaryContext primary() throws RecognitionException {
		PrimaryContext _localctx = new PrimaryContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_primary);
		try {
			setState(127);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case ID:
				_localctx = new PrimaryIDContext(_localctx);
				enterOuterAlt(_localctx, 1);
				{
				setState(124);
				match(ID);
				}
				break;
			case INT:
				_localctx = new PrimaryINTContext(_localctx);
				enterOuterAlt(_localctx, 2);
				{
				setState(125);
				match(INT);
				}
				break;
			case FLOAT:
				_localctx = new PrimaryFLOATContext(_localctx);
				enterOuterAlt(_localctx, 3);
				{
				setState(126);
				match(FLOAT);
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

	public static class ExprLstContext extends ParserRuleContext {
		public ExprLstContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_exprLst; }
	 
		public ExprLstContext() { }
		public void copyFrom(ExprLstContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class ExprListContext extends ExprLstContext {
		public List<ExprContext> expr() {
			return getRuleContexts(ExprContext.class);
		}
		public ExprContext expr(int i) {
			return getRuleContext(ExprContext.class,i);
		}
		public ExprListContext(ExprLstContext ctx) { copyFrom(ctx); }
		@Override
		public <T> T accept(ParseTreeVisitor<? extends T> visitor) {
			if ( visitor instanceof CymbolVisitor ) return ((CymbolVisitor<? extends T>)visitor).visitExprList(this);
			else return visitor.visitChildren(this);
		}
	}

	public final ExprLstContext exprLst() throws RecognitionException {
		ExprLstContext _localctx = new ExprLstContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_exprLst);
		int _la;
		try {
			_localctx = new ExprListContext(_localctx);
			enterOuterAlt(_localctx, 1);
			{
			setState(129);
			expr(0);
			setState(134);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__7) {
				{
				{
				setState(130);
				match(T__7);
				setState(131);
				expr(0);
				}
				}
				setState(136);
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
			return precpred(_ctx, 5);
		case 1:
			return precpred(_ctx, 4);
		case 2:
			return precpred(_ctx, 3);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\33\u008c\4\2\t\2"+
		"\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\3\2\3\2\6\2\33\n\2\r\2\16\2\34\3\3\3\3\3\3\3\3\5\3#\n\3"+
		"\3\3\3\3\3\4\3\4\3\5\3\5\3\5\3\5\5\5-\n\5\3\5\3\5\3\5\3\6\3\6\3\6\7\6"+
		"\65\n\6\f\6\16\68\13\6\3\7\3\7\3\7\3\b\3\b\7\b?\n\b\f\b\16\bB\13\b\3\b"+
		"\3\b\3\t\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\tN\n\t\3\t\3\t\5\tR\n\t\3\t\3\t"+
		"\3\t\3\t\3\t\3\t\3\t\3\t\3\t\5\t]\n\t\3\n\3\n\3\n\3\n\5\nc\n\n\3\n\3\n"+
		"\3\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n\5\no\n\n\3\n\3\n\3\n\3\n\3\n\3\n\3\n"+
		"\3\n\3\n\7\nz\n\n\f\n\16\n}\13\n\3\13\3\13\3\13\5\13\u0082\n\13\3\f\3"+
		"\f\3\f\7\f\u0087\n\f\f\f\16\f\u008a\13\f\3\f\2\3\22\r\2\4\6\b\n\f\16\20"+
		"\22\24\26\2\5\3\2\5\7\3\2\23\24\4\2\21\21\25\25\2\u0098\2\32\3\2\2\2\4"+
		"\36\3\2\2\2\6&\3\2\2\2\b(\3\2\2\2\n\61\3\2\2\2\f9\3\2\2\2\16<\3\2\2\2"+
		"\20\\\3\2\2\2\22n\3\2\2\2\24\u0081\3\2\2\2\26\u0083\3\2\2\2\30\33\5\b"+
		"\5\2\31\33\5\4\3\2\32\30\3\2\2\2\32\31\3\2\2\2\33\34\3\2\2\2\34\32\3\2"+
		"\2\2\34\35\3\2\2\2\35\3\3\2\2\2\36\37\5\6\4\2\37\"\7\27\2\2 !\7\3\2\2"+
		"!#\5\22\n\2\" \3\2\2\2\"#\3\2\2\2#$\3\2\2\2$%\7\4\2\2%\5\3\2\2\2&\'\t"+
		"\2\2\2\'\7\3\2\2\2()\5\6\4\2)*\7\27\2\2*,\7\b\2\2+-\5\n\6\2,+\3\2\2\2"+
		",-\3\2\2\2-.\3\2\2\2./\7\t\2\2/\60\5\16\b\2\60\t\3\2\2\2\61\66\5\f\7\2"+
		"\62\63\7\n\2\2\63\65\5\f\7\2\64\62\3\2\2\2\658\3\2\2\2\66\64\3\2\2\2\66"+
		"\67\3\2\2\2\67\13\3\2\2\28\66\3\2\2\29:\5\6\4\2:;\7\27\2\2;\r\3\2\2\2"+
		"<@\7\13\2\2=?\5\20\t\2>=\3\2\2\2?B\3\2\2\2@>\3\2\2\2@A\3\2\2\2AC\3\2\2"+
		"\2B@\3\2\2\2CD\7\f\2\2D\17\3\2\2\2E]\5\16\b\2F]\5\4\3\2GH\7\r\2\2HI\5"+
		"\22\n\2IJ\7\16\2\2JM\5\20\t\2KL\7\17\2\2LN\5\20\t\2MK\3\2\2\2MN\3\2\2"+
		"\2N]\3\2\2\2OQ\7\20\2\2PR\5\22\n\2QP\3\2\2\2QR\3\2\2\2RS\3\2\2\2S]\7\4"+
		"\2\2TU\5\22\n\2UV\7\3\2\2VW\5\22\n\2WX\7\4\2\2X]\3\2\2\2YZ\5\22\n\2Z["+
		"\7\4\2\2[]\3\2\2\2\\E\3\2\2\2\\F\3\2\2\2\\G\3\2\2\2\\O\3\2\2\2\\T\3\2"+
		"\2\2\\Y\3\2\2\2]\21\3\2\2\2^_\b\n\1\2_`\7\27\2\2`b\7\b\2\2ac\5\26\f\2"+
		"ba\3\2\2\2bc\3\2\2\2cd\3\2\2\2do\7\t\2\2ef\7\21\2\2fo\5\22\n\tgh\7\22"+
		"\2\2ho\5\22\n\bio\5\24\13\2jk\7\b\2\2kl\5\22\n\2lm\7\t\2\2mo\3\2\2\2n"+
		"^\3\2\2\2ne\3\2\2\2ng\3\2\2\2ni\3\2\2\2nj\3\2\2\2o{\3\2\2\2pq\f\7\2\2"+
		"qr\t\3\2\2rz\5\22\n\bst\f\6\2\2tu\t\4\2\2uz\5\22\n\7vw\f\5\2\2wx\7\26"+
		"\2\2xz\5\22\n\6yp\3\2\2\2ys\3\2\2\2yv\3\2\2\2z}\3\2\2\2{y\3\2\2\2{|\3"+
		"\2\2\2|\23\3\2\2\2}{\3\2\2\2~\u0082\7\27\2\2\177\u0082\7\30\2\2\u0080"+
		"\u0082\7\31\2\2\u0081~\3\2\2\2\u0081\177\3\2\2\2\u0081\u0080\3\2\2\2\u0082"+
		"\25\3\2\2\2\u0083\u0088\5\22\n\2\u0084\u0085\7\n\2\2\u0085\u0087\5\22"+
		"\n\2\u0086\u0084\3\2\2\2\u0087\u008a\3\2\2\2\u0088\u0086\3\2\2\2\u0088"+
		"\u0089\3\2\2\2\u0089\27\3\2\2\2\u008a\u0088\3\2\2\2\21\32\34\",\66@MQ"+
		"\\bny{\u0081\u0088";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}