/*
 * oxTrust is available under the MIT License (2008). See http://opensource.org/licenses/MIT for full text.
 *
 * Copyright (c) 2017, Gluu
 */
// Generated from ScimFilter.g4 by ANTLR 4.5.3
package org.gluu.oxtrust.service.antlr.scimFilter.antlr4;

import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class ScimFilterLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.5.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9,
		T__9=10, T__10=11, T__11=12, T__12=13, T__13=14, T__14=15, WHITESPACE=16,
		ALPHA=17, NUMBER=18, BOOLEAN=19, NULL=20, NAMECHAR=21, URI=22, ATTRNAME=23,
		SUBATTR=24, STRING=25;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8",
		"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "DIGIT", "LOWERCASE",
		"UPPERCASE", "WHITESPACE", "ALPHA", "NUMBER", "BOOLEAN", "NULL", "NAMECHAR",
		"URI", "ATTRNAME", "SUBATTR", "STRING"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'eq'", "'ne'", "'co'", "'sw'", "'ew'", "'gt'", "'lt'", "'ge'",
		"'le'", "'pr'", "'not'", "'('", "')'", "'and'", "'or'", null, null, null,
		null, "'null'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null,
		null, null, null, null, "WHITESPACE", "ALPHA", "NUMBER", "BOOLEAN", "NULL",
		"NAMECHAR", "URI", "ATTRNAME", "SUBATTR", "STRING"
	};
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


	public ScimFilterLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ScimFilter.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\33\u00bf\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\3\2\3\2\3\2\3\3\3\3\3\3"+
		"\3\4\3\4\3\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3"+
		"\t\3\n\3\n\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3"+
		"\17\3\17\3\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\6\24p"+
		"\n\24\r\24\16\24q\3\24\3\24\3\25\3\25\5\25x\n\25\3\26\5\26{\n\26\3\26"+
		"\6\26~\n\26\r\26\16\26\177\3\26\3\26\6\26\u0084\n\26\r\26\16\26\u0085"+
		"\5\26\u0088\n\26\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\3\27\5\27\u0093"+
		"\n\27\3\30\3\30\3\30\3\30\3\30\3\31\3\31\3\31\5\31\u009d\n\31\3\32\3\32"+
		"\3\32\7\32\u00a2\n\32\f\32\16\32\u00a5\13\32\3\32\3\32\3\32\3\33\5\33"+
		"\u00ab\n\33\3\33\3\33\7\33\u00af\n\33\f\33\16\33\u00b2\13\33\3\34\3\34"+
		"\3\34\3\35\3\35\7\35\u00b9\n\35\f\35\16\35\u00bc\13\35\3\35\3\35\3\u00ba"+
		"\2\36\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\21!\2#\2%\2\'\22)\23+\24-\25/\26\61\27\63\30\65\31\67\329\33\3"+
		"\2\t\3\2\62;\3\2c|\3\2C\\\4\2\13\13\"\"\4\2\60\60^^\4\2//aa\4\2\60\60"+
		"<<\u00c9\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2"+
		"\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2\'\3\2\2"+
		"\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2"+
		"\2\2\65\3\2\2\2\2\67\3\2\2\2\29\3\2\2\2\3;\3\2\2\2\5>\3\2\2\2\7A\3\2\2"+
		"\2\tD\3\2\2\2\13G\3\2\2\2\rJ\3\2\2\2\17M\3\2\2\2\21P\3\2\2\2\23S\3\2\2"+
		"\2\25V\3\2\2\2\27Y\3\2\2\2\31]\3\2\2\2\33_\3\2\2\2\35a\3\2\2\2\37e\3\2"+
		"\2\2!h\3\2\2\2#j\3\2\2\2%l\3\2\2\2\'o\3\2\2\2)w\3\2\2\2+z\3\2\2\2-\u0092"+
		"\3\2\2\2/\u0094\3\2\2\2\61\u009c\3\2\2\2\63\u009e\3\2\2\2\65\u00aa\3\2"+
		"\2\2\67\u00b3\3\2\2\29\u00b6\3\2\2\2;<\7g\2\2<=\7s\2\2=\4\3\2\2\2>?\7"+
		"p\2\2?@\7g\2\2@\6\3\2\2\2AB\7e\2\2BC\7q\2\2C\b\3\2\2\2DE\7u\2\2EF\7y\2"+
		"\2F\n\3\2\2\2GH\7g\2\2HI\7y\2\2I\f\3\2\2\2JK\7i\2\2KL\7v\2\2L\16\3\2\2"+
		"\2MN\7n\2\2NO\7v\2\2O\20\3\2\2\2PQ\7i\2\2QR\7g\2\2R\22\3\2\2\2ST\7n\2"+
		"\2TU\7g\2\2U\24\3\2\2\2VW\7r\2\2WX\7t\2\2X\26\3\2\2\2YZ\7p\2\2Z[\7q\2"+
		"\2[\\\7v\2\2\\\30\3\2\2\2]^\7*\2\2^\32\3\2\2\2_`\7+\2\2`\34\3\2\2\2ab"+
		"\7c\2\2bc\7p\2\2cd\7f\2\2d\36\3\2\2\2ef\7q\2\2fg\7t\2\2g \3\2\2\2hi\t"+
		"\2\2\2i\"\3\2\2\2jk\t\3\2\2k$\3\2\2\2lm\t\4\2\2m&\3\2\2\2np\t\5\2\2on"+
		"\3\2\2\2pq\3\2\2\2qo\3\2\2\2qr\3\2\2\2rs\3\2\2\2st\b\24\2\2t(\3\2\2\2"+
		"ux\5#\22\2vx\5%\23\2wu\3\2\2\2wv\3\2\2\2x*\3\2\2\2y{\7/\2\2zy\3\2\2\2"+
		"z{\3\2\2\2{}\3\2\2\2|~\5!\21\2}|\3\2\2\2~\177\3\2\2\2\177}\3\2\2\2\177"+
		"\u0080\3\2\2\2\u0080\u0087\3\2\2\2\u0081\u0083\t\6\2\2\u0082\u0084\5!"+
		"\21\2\u0083\u0082\3\2\2\2\u0084\u0085\3\2\2\2\u0085\u0083\3\2\2\2\u0085"+
		"\u0086\3\2\2\2\u0086\u0088\3\2\2\2\u0087\u0081\3\2\2\2\u0087\u0088\3\2"+
		"\2\2\u0088,\3\2\2\2\u0089\u008a\7h\2\2\u008a\u008b\7c\2\2\u008b\u008c"+
		"\7n\2\2\u008c\u008d\7u\2\2\u008d\u0093\7g\2\2\u008e\u008f\7v\2\2\u008f"+
		"\u0090\7t\2\2\u0090\u0091\7w\2\2\u0091\u0093\7g\2\2\u0092\u0089\3\2\2"+
		"\2\u0092\u008e\3\2\2\2\u0093.\3\2\2\2\u0094\u0095\7p\2\2\u0095\u0096\7"+
		"w\2\2\u0096\u0097\7n\2\2\u0097\u0098\7n\2\2\u0098\60\3\2\2\2\u0099\u009d"+
		"\t\7\2\2\u009a\u009d\5!\21\2\u009b\u009d\5)\25\2\u009c\u0099\3\2\2\2\u009c"+
		"\u009a\3\2\2\2\u009c\u009b\3\2\2\2\u009d\62\3\2\2\2\u009e\u00a3\5)\25"+
		"\2\u009f\u00a2\5\61\31\2\u00a0\u00a2\t\b\2\2\u00a1\u009f\3\2\2\2\u00a1"+
		"\u00a0\3\2\2\2\u00a2\u00a5\3\2\2\2\u00a3\u00a1\3\2\2\2\u00a3\u00a4\3\2"+
		"\2\2\u00a4\u00a6\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a6\u00a7\5)\25\2\u00a7"+
		"\u00a8\7<\2\2\u00a8\64\3\2\2\2\u00a9\u00ab\5\63\32\2\u00aa\u00a9\3\2\2"+
		"\2\u00aa\u00ab\3\2\2\2\u00ab\u00ac\3\2\2\2\u00ac\u00b0\5)\25\2\u00ad\u00af"+
		"\5\61\31\2\u00ae\u00ad\3\2\2\2\u00af\u00b2\3\2\2\2\u00b0\u00ae\3\2\2\2"+
		"\u00b0\u00b1\3\2\2\2\u00b1\66\3\2\2\2\u00b2\u00b0\3\2\2\2\u00b3\u00b4"+
		"\7\60\2\2\u00b4\u00b5\5\65\33\2\u00b58\3\2\2\2\u00b6\u00ba\7$\2\2\u00b7"+
		"\u00b9\13\2\2\2\u00b8\u00b7\3\2\2\2\u00b9\u00bc\3\2\2\2\u00ba\u00bb\3"+
		"\2\2\2\u00ba\u00b8\3\2\2\2\u00bb\u00bd\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bd"+
		"\u00be\7$\2\2\u00be:\3\2\2\2\20\2qwz\177\u0085\u0087\u0092\u009c\u00a1"+
		"\u00a3\u00aa\u00b0\u00ba\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}