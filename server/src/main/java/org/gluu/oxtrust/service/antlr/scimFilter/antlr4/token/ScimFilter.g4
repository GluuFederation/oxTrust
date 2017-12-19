/** ScimFilter.g4 */

grammar ScimFilter;

/*
 * Parser Rules. Edit only if you really know what you are doing 8-|
 */

attrpath : ATTRNAME SUBATTR? ;

compareop : 'eq' | 'ne' | 'co' | 'sw' | 'ew' | 'gt' | 'lt' | 'ge' | 'le' ;

compvalue : BOOLEAN | NUMBER | STRING | NULL ;
       
attrexp : attrpath 'pr'
        | attrpath compareop compvalue ;

filter : 'not'? '(' filter ')'	#negatedFilter
       | filter 'and' filter 	#andFilter
       | filter 'or' filter	#orFilter
       | attrexp 		#simpleExpr
       ;

 
/*
 * Lexer Rules. Edit only if you really know what you are doing 8-|
 */
fragment DIGIT : [0-9] ;
fragment LOWERCASE : [a-z] ;
fragment UPPERCASE : [A-Z] ;
		
WHITESPACE : [\t ]+ -> skip ;

ALPHA : LOWERCASE | UPPERCASE ;

NUMBER : '-'? DIGIT+ ([\.] DIGIT+)? ;

BOOLEAN : 'false' | 'true' ;

NULL : 'null' ;

NAMECHAR : '-' | '_' | DIGIT | ALPHA ;

URI : ALPHA (NAMECHAR | ':' | '.')* ALPHA ':' ;

ATTRNAME : URI? ALPHA NAMECHAR* ;

SUBATTR : '.' ATTRNAME ;

STRING : '"' .*? '"' ;
