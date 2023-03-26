// Generated from CSV.g4 by ANTLR 4.8
package org.teachfx.antlr4.ep10;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNDeserializer;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class CSVParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.8", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, TEXT=4, STRING=5;
	public static final int
		RULE_file = 0, RULE_hdr = 1, RULE_row = 2, RULE_field = 3;
	private static String[] makeRuleNames() {
		return new String[] {
			"file", "hdr", "row", "field"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "','", "'\r'", "'\n'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "TEXT", "STRING"
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
	public String getGrammarFileName() { return "CSV.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public CSVParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class FileContext extends ParserRuleContext {
		public int i = 0;
		public HdrContext hdr;
		public RowContext row;
		public List<RowContext> rows = new ArrayList<RowContext>();
		public HdrContext hdr() {
			return getRuleContext(HdrContext.class,0);
		}
		public List<RowContext> row() {
			return getRuleContexts(RowContext.class);
		}
		public RowContext row(int i) {
			return getRuleContext(RowContext.class,i);
		}
		public FileContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_file; }
	}

	public final FileContext file() throws RecognitionException {
		FileContext _localctx = new FileContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_file);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(8);
			((FileContext)_localctx).hdr = hdr();
			setState(12); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(9);
				((FileContext)_localctx).row = row((((FileContext)_localctx).hdr!=null?_input.getText(((FileContext)_localctx).hdr.start,((FileContext)_localctx).hdr.stop):null).split(","));
				((FileContext)_localctx).rows.add(((FileContext)_localctx).row);
				_localctx.i++;
				}
				}
				setState(14); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__1) | (1L << T__2) | (1L << TEXT) | (1L << STRING))) != 0) );

			            System.out.println(_localctx.i+" rows");
			            for(RowContext r : ((FileContext)_localctx).rows) {
			                System.out.println("row token interval: " + r.getSourceInterval());
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

	public static class HdrContext extends ParserRuleContext {
		public RowContext row() {
			return getRuleContext(RowContext.class,0);
		}
		public HdrContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_hdr; }
	}

	public final HdrContext hdr() throws RecognitionException {
		HdrContext _localctx = new HdrContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_hdr);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(18);
			row(null);
			 System.out.println("header: '"+_input.getText(_localctx.start, _input.LT(-1)).trim()+"'"); 
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

	public static class RowContext extends ParserRuleContext {
		public String[] columns;
		public Map<String,String> values;
		public int col = 0;
		public FieldContext field;
		public List<FieldContext> field() {
			return getRuleContexts(FieldContext.class);
		}
		public FieldContext field(int i) {
			return getRuleContext(FieldContext.class,i);
		}
		public RowContext(ParserRuleContext parent, int invokingState) { super(parent, invokingState); }
		public RowContext(ParserRuleContext parent, int invokingState, String[] columns) {
			super(parent, invokingState);
			this.columns = columns;
		}
		@Override public int getRuleIndex() { return RULE_row; }
	}

	public final RowContext row(String[] columns) throws RecognitionException {
		RowContext _localctx = new RowContext(_ctx, getState(), columns);
		enterRule(_localctx, 4, RULE_row);
		 
		    ((RowContext)_localctx).values =  new HashMap<String,String>();

		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(21);
			((RowContext)_localctx).field = field();

			        if (_localctx.columns!=null) {
			            _localctx.values.put(_localctx.columns[_localctx.col++].trim(), (((RowContext)_localctx).field!=null?_input.getText(((RowContext)_localctx).field.start,((RowContext)_localctx).field.stop):null).trim());
			        }
			        
			setState(29);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0) {
				{
				{
				setState(23);
				match(T__0);
				setState(24);
				((RowContext)_localctx).field = field();

				            if (_localctx.columns!=null) {
				                _localctx.values.put(_localctx.columns[_localctx.col++].trim(), (((RowContext)_localctx).field!=null?_input.getText(((RowContext)_localctx).field.start,((RowContext)_localctx).field.stop):null).trim());
				            }
				            
				}
				}
				setState(31);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(33);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==T__1) {
				{
				setState(32);
				match(T__1);
				}
			}

			setState(35);
			match(T__2);
			}
			_ctx.stop = _input.LT(-1);

			    if(_localctx.values!=null && _localctx.values.size() > 0) {
			        System.out.println("values = "+_localctx.values);
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

	public static class FieldContext extends ParserRuleContext {
		public TerminalNode TEXT() { return getToken(CSVParser.TEXT, 0); }
		public TerminalNode STRING() { return getToken(CSVParser.STRING, 0); }
		public FieldContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_field; }
	}

	public final FieldContext field() throws RecognitionException {
		FieldContext _localctx = new FieldContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_field);
		try {
			setState(40);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case TEXT:
				enterOuterAlt(_localctx, 1);
				{
				setState(37);
				match(TEXT);
				}
				break;
			case STRING:
				enterOuterAlt(_localctx, 2);
				{
				setState(38);
				match(STRING);
				}
				break;
			case T__0:
			case T__1:
			case T__2:
				enterOuterAlt(_localctx, 3);
				{
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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\7-\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\3\2\3\2\3\2\3\2\6\2\17\n\2\r\2\16\2\20\3\2\3\2\3\3"+
		"\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\7\4\36\n\4\f\4\16\4!\13\4\3\4\5\4$\n"+
		"\4\3\4\3\4\3\5\3\5\3\5\5\5+\n\5\3\5\2\2\6\2\4\6\b\2\2\2-\2\n\3\2\2\2\4"+
		"\24\3\2\2\2\6\27\3\2\2\2\b*\3\2\2\2\n\16\5\4\3\2\13\f\5\6\4\2\f\r\b\2"+
		"\1\2\r\17\3\2\2\2\16\13\3\2\2\2\17\20\3\2\2\2\20\16\3\2\2\2\20\21\3\2"+
		"\2\2\21\22\3\2\2\2\22\23\b\2\1\2\23\3\3\2\2\2\24\25\5\6\4\2\25\26\b\3"+
		"\1\2\26\5\3\2\2\2\27\30\5\b\5\2\30\37\b\4\1\2\31\32\7\3\2\2\32\33\5\b"+
		"\5\2\33\34\b\4\1\2\34\36\3\2\2\2\35\31\3\2\2\2\36!\3\2\2\2\37\35\3\2\2"+
		"\2\37 \3\2\2\2 #\3\2\2\2!\37\3\2\2\2\"$\7\4\2\2#\"\3\2\2\2#$\3\2\2\2$"+
		"%\3\2\2\2%&\7\5\2\2&\7\3\2\2\2\'+\7\6\2\2(+\7\7\2\2)+\3\2\2\2*\'\3\2\2"+
		"\2*(\3\2\2\2*)\3\2\2\2+\t\3\2\2\2\6\20\37#*";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}