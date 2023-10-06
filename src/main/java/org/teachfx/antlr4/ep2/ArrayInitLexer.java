// Generated from ArrayInit.g4 by ANTLR 4.0
package org.teachfx.antlr4.ep2;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.ATNSimulator;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.atn.PredictionContextCache;
import org.antlr.v4.runtime.dfa.DFA;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ArrayInitLexer extends Lexer {
    public static final int
            T__2 = 1, T__1 = 2, T__0 = 3, INT = 4, WS = 5;
    public static final String[] tokenNames = {
            "<INVALID>",
            "'{'", "','", "'}'", "INT", "WS"
    };
    public static final String[] ruleNames = {
            "T__2", "T__1", "T__0", "INT", "WS"
    };
    public static final String _serializedATN =
            "\2\4\7\37\b\1\4\2\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\3\2\3\2\3\3\3\3" +
                    "\3\4\3\4\3\5\6\5\25\n\5\r\5\16\5\26\3\6\6\6\32\n\6\r\6\16\6\33\3\6\3\6" +
                    "\2\7\3\3\1\5\4\1\7\5\1\t\6\1\13\7\2\3\2\4\3\62;\5\13\f\17\17\"\" \2\3" +
                    "\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\3\r\3\2\2\2" +
                    "\5\17\3\2\2\2\7\21\3\2\2\2\t\24\3\2\2\2\13\31\3\2\2\2\r\16\7}\2\2\16\4" +
                    "\3\2\2\2\17\20\7.\2\2\20\6\3\2\2\2\21\22\7\177\2\2\22\b\3\2\2\2\23\25" +
                    "\t\2\2\2\24\23\3\2\2\2\25\26\3\2\2\2\26\24\3\2\2\2\26\27\3\2\2\2\27\n" +
                    "\3\2\2\2\30\32\t\3\2\2\31\30\3\2\2\2\32\33\3\2\2\2\33\31\3\2\2\2\33\34" +
                    "\3\2\2\2\34\35\3\2\2\2\35\36\b\6\2\2\36\f\3\2\2\2\5\2\26\33";
    public static final ATN _ATN =
            ATNSimulator.deserialize(_serializedATN.toCharArray());
    protected static final DFA[] _decisionToDFA;
    protected static final PredictionContextCache _sharedContextCache =
            new PredictionContextCache();
    public static String[] modeNames = {
            "DEFAULT_MODE"
    };

    static {
        _decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
    }

    public ArrayInitLexer(CharStream input) {
        super(input);
        _interp = new LexerATNSimulator(this, _ATN, _decisionToDFA, _sharedContextCache);
    }

    @Override
    public String getGrammarFileName() {
        return "ArrayInit.g4";
    }

    @Override
    public String[] getTokenNames() {
        return tokenNames;
    }

    @Override
    public String[] getRuleNames() {
        return ruleNames;
    }

    @Override
    public String[] getModeNames() {
        return modeNames;
    }

    @Override
    public ATN getATN() {
        return _ATN;
    }

    @Override
    public void action(RuleContext _localctx, int ruleIndex, int actionIndex) {
        switch (ruleIndex) {
            case 4:
                WS_action((RuleContext) _localctx, actionIndex);
                break;
        }
    }

    private void WS_action(RuleContext _localctx, int actionIndex) {
        switch (actionIndex) {
            case 0:
                skip();
                break;
        }
    }
}