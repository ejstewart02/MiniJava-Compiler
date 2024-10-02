grammar MiniJava;

goal               : mainClass classDeclaration* EOF ;

mainClass          : 'class' identifier '{' 'public' 'static' 'void' 'main' '(' 'String' '[' ']' identifier ')' '{' statement '}' '}' ;

classDeclaration   : 'class' identifier ('extends' identifier)? '{' varDeclaration* methodDeclaration* '}' ;

varDeclaration     : type identifier ';' ;

methodDeclaration  : 'public' type identifier '(' (type identifier (',' type identifier)*)? ')' '{' varDeclaration* statement* 'return' expression ';' '}' ;

type               : 'int' '[' ']'
                   | 'float' '[' ']'
                   | 'boolean'
                   | 'int'
                   | 'float'
                   | identifier ;

statement          : '{' statement* '}'
                   | 'if' '(' expression ')' statement 'else' statement
                   | 'while' '(' expression ')' statement
                   | 'System.out.println' '(' expression ')' ';'
                   | identifier '=' expression ';'
                   | identifier '[' expression ']' '=' expression ';' ;

expression         : expression ('&&' | '<' | '+' | '-' | '*') expression
                   | expression '[' expression ']'
                   | expression '.' 'length'
                   | expression '.' identifier '(' (expression (',' expression)*)? ')'
                   | INTEGER_LITERAL
                   | FLOAT_LITERAL
                   | 'true'
                   | 'false'
                   | identifier
                   | 'this'
                   | 'new' 'int' '[' expression ']'
                   | 'new' 'float' '[' expression ']'
                   | 'new' identifier '(' ')'
                   | '!' expression
                   | '(' expression ')' ;

identifier         : IDENTIFIER ;

INTEGER_LITERAL    : [0-9]+ ;
FLOAT_LITERAL      : [0-9]+ '.' [0-9]+ ;
IDENTIFIER         : [a-zA-Z_][a-zA-Z_0-9]* ;

WS                : [ \t\r\n]+ -> skip ;
COMMENT           : '//' ~[\r\n]* -> skip ;
LINE_COMMENT      : '/*' .*? '*/' -> skip ;

