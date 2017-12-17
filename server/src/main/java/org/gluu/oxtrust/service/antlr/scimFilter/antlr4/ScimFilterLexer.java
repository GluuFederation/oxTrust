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
		ALPHA=17, NUMBER=18, BOOLEAN=19, NULL=20, NAMECHAR=21, ATTRNAME=22, SUBATTR=23, 
		STRING=24;
	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	public static final String[] ruleNames = {
		"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
		"T__9", "T__10", "T__11", "T__12", "T__13", "T__14", "DIGIT", "LOWERCASE", 
		"UPPERCASE", "WHITESPACE", "ALPHA", "NUMBER", "BOOLEAN", "NULL", "NAMECHAR", 
		"ATTRNAME", "SUBATTR", "STRING"
	};

	private static final String[] _LITERAL_NAMES = {
		null, "'eq'", "'ne'", "'co'", "'sw'", "'ew'", "'gt'", "'lt'", "'ge'", 
		"'le'", "'pr'", "'not'", "'('", "')'", "'and'", "'or'", null, null, null, 
		null, "'null'"
	};
	private static final String[] _SYMBOLIC_NAMES = {
		null, null, null, null, null, null, null, null, null, null, null, null, 
		null, null, null, null, "WHITESPACE", "ALPHA", "NUMBER", "BOOLEAN", "NULL", 
		"NAMECHAR", "ATTRNAME", "SUBATTR", "STRING"
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
		"\3\u0430\ud6d1\u8206\uad2d\u4417\uaef1\u8d80\uaadd\2\32\u00ac\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\3\2\3\2\3\2\3\3\3\3\3\3\3\4\3\4\3"+
		"\4\3\5\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\t\3\n\3\n"+
		"\3\n\3\13\3\13\3\13\3\f\3\f\3\f\3\f\3\r\3\r\3\16\3\16\3\17\3\17\3\17\3"+
		"\17\3\20\3\20\3\20\3\21\3\21\3\22\3\22\3\23\3\23\3\24\6\24n\n\24\r\24"+
		"\16\24o\3\24\3\24\3\25\3\25\5\25v\n\25\3\26\6\26y\n\26\r\26\16\26z\3\26"+
		"\3\26\6\26\177\n\26\r\26\16\26\u0080\5\26\u0083\n\26\3\27\3\27\3\27\3"+
		"\27\3\27\3\27\3\27\3\27\3\27\5\27\u008e\n\27\3\30\3\30\3\30\3\30\3\30"+
		"\3\31\3\31\3\31\5\31\u0098\n\31\3\32\3\32\7\32\u009c\n\32\f\32\16\32\u009f"+
		"\13\32\3\33\3\33\3\33\3\34\3\34\7\34\u00a6\n\34\f\34\16\34\u00a9\13\34"+
		"\3\34\3\34\3\u00a7\2\35\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f"+
		"\27\r\31\16\33\17\35\20\37\21!\2#\2%\2\'\22)\23+\24-\25/\26\61\27\63\30"+
		"\65\31\67\32\3\2\b\3\2\62;\3\2c|\3\2C\\\4\2\13\13\"\"\4\2\60\60^^\4\2"+
		"//aa\u00b2\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2"+
		"\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27"+
		"\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2\2\2\2\'\3\2\2"+
		"\2\2)\3\2\2\2\2+\3\2\2\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2"+
		"\2\2\65\3\2\2\2\2\67\3\2\2\2\39\3\2\2\2\5<\3\2\2\2\7?\3\2\2\2\tB\3\2\2"+
		"\2\13E\3\2\2\2\rH\3\2\2\2\17K\3\2\2\2\21N\3\2\2\2\23Q\3\2\2\2\25T\3\2"+
		"\2\2\27W\3\2\2\2\31[\3\2\2\2\33]\3\2\2\2\35_\3\2\2\2\37c\3\2\2\2!f\3\2"+
		"\2\2#h\3\2\2\2%j\3\2\2\2\'m\3\2\2\2)u\3\2\2\2+x\3\2\2\2-\u008d\3\2\2\2"+
		"/\u008f\3\2\2\2\61\u0097\3\2\2\2\63\u0099\3\2\2\2\65\u00a0\3\2\2\2\67"+
		"\u00a3\3\2\2\29:\7g\2\2:;\7s\2\2;\4\3\2\2\2<=\7p\2\2=>\7g\2\2>\6\3\2\2"+
		"\2?@\7e\2\2@A\7q\2\2A\b\3\2\2\2BC\7u\2\2CD\7y\2\2D\n\3\2\2\2EF\7g\2\2"+
		"FG\7y\2\2G\f\3\2\2\2HI\7i\2\2IJ\7v\2\2J\16\3\2\2\2KL\7n\2\2LM\7v\2\2M"+
		"\20\3\2\2\2NO\7i\2\2OP\7g\2\2P\22\3\2\2\2QR\7n\2\2RS\7g\2\2S\24\3\2\2"+
		"\2TU\7r\2\2UV\7t\2\2V\26\3\2\2\2WX\7p\2\2XY\7q\2\2YZ\7v\2\2Z\30\3\2\2"+
		"\2[\\\7*\2\2\\\32\3\2\2\2]^\7+\2\2^\34\3\2\2\2_`\7c\2\2`a\7p\2\2ab\7f"+
		"\2\2b\36\3\2\2\2cd\7q\2\2de\7t\2\2e \3\2\2\2fg\t\2\2\2g\"\3\2\2\2hi\t"+
		"\3\2\2i$\3\2\2\2jk\t\4\2\2k&\3\2\2\2ln\t\5\2\2ml\3\2\2\2no\3\2\2\2om\3"+
		"\2\2\2op\3\2\2\2pq\3\2\2\2qr\b\24\2\2r(\3\2\2\2sv\5#\22\2tv\5%\23\2us"+
		"\3\2\2\2ut\3\2\2\2v*\3\2\2\2wy\5!\21\2xw\3\2\2\2yz\3\2\2\2zx\3\2\2\2z"+
		"{\3\2\2\2{\u0082\3\2\2\2|~\t\6\2\2}\177\5!\21\2~}\3\2\2\2\177\u0080\3"+
		"\2\2\2\u0080~\3\2\2\2\u0080\u0081\3\2\2\2\u0081\u0083\3\2\2\2\u0082|\3"+
		"\2\2\2\u0082\u0083\3\2\2\2\u0083,\3\2\2\2\u0084\u0085\7h\2\2\u0085\u0086"+
		"\7c\2\2\u0086\u0087\7n\2\2\u0087\u0088\7u\2\2\u0088\u008e\7g\2\2\u0089"+
		"\u008a\7v\2\2\u008a\u008b\7t\2\2\u008b\u008c\7w\2\2\u008c\u008e\7g\2\2"+
		"\u008d\u0084\3\2\2\2\u008d\u0089\3\2\2\2\u008e.\3\2\2\2\u008f\u0090\7"+
		"p\2\2\u0090\u0091\7w\2\2\u0091\u0092\7n\2\2\u0092\u0093\7n\2\2\u0093\60"+
		"\3\2\2\2\u0094\u0098\t\7\2\2\u0095\u0098\5!\21\2\u0096\u0098\5)\25\2\u0097"+
		"\u0094\3\2\2\2\u0097\u0095\3\2\2\2\u0097\u0096\3\2\2\2\u0098\62\3\2\2"+
		"\2\u0099\u009d\5)\25\2\u009a\u009c\5\61\31\2\u009b\u009a\3\2\2\2\u009c"+
		"\u009f\3\2\2\2\u009d\u009b\3\2\2\2\u009d\u009e\3\2\2\2\u009e\64\3\2\2"+
		"\2\u009f\u009d\3\2\2\2\u00a0\u00a1\7\60\2\2\u00a1\u00a2\5\63\32\2\u00a2"+
		"\66\3\2\2\2\u00a3\u00a7\7$\2\2\u00a4\u00a6\13\2\2\2\u00a5\u00a4\3\2\2"+
		"\2\u00a6\u00a9\3\2\2\2\u00a7\u00a8\3\2\2\2\u00a7\u00a5\3\2\2\2\u00a8\u00aa"+
		"\3\2\2\2\u00a9\u00a7\3\2\2\2\u00aa\u00ab\7$\2\2\u00ab8\3\2\2\2\f\2ouz"+
		"\u0080\u0082\u008d\u0097\u009d\u00a7\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}