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

statement          : '{' statement* '}' #nestedStatement
                   | 'if' '(' expression ')' statement 'else' statement #ifElseStatement
                   | 'while' '(' expression ')' statement #whileStatement
                   | 'System.out.println' '(' expression ')' ';' #printStatement
                   | identifier '=' expression ';' #varAssignStatement
                   | identifier '[' expression ']' '=' expression ';' #arrayAssignStatement;

expression         : expression ('&&' | '<' | '+' | '-' | '*') expression #arithExpression //TODO: Check types
                   | expression '[' expression ']' #arrayAccessExpression //TODO: check that lit is array and i is int
                   | expression '.' 'length' #arrayLengthExpression //TODO: check that left is array
                   | expression '.' identifier '(' (expression (',' expression)*)? ')' #methodCallExpression //done
                   | INTEGER_LITERAL #intLiteralExpression
                   | FLOAT_LITERAL #floatLiteralExpression
                   | 'true' #trueLiteralExpression
                   | 'false' #falseLiteralExpression
                   | identifier #identifierExpression
                   | 'this' #thisClassExpression
                   | 'new' 'int' '[' expression ']' #newIntegerArrayExpression //TODO: check that i is int
                   | 'new' 'float' '[' expression ']' #newFloatArrayExpression //TODO: check that i is int
                   | 'new' identifier '(' ')' #newClassExpression //done
                   | '!' expression #notExpression //TODO: make sure ex is bool
                   | '(' expression ')' #lpRpExpression;

identifier         : IDENTIFIER ;

INTEGER_LITERAL    : [0-9]+ ;
FLOAT_LITERAL      : [0-9]+ '.' [0-9]+ ;
IDENTIFIER         : [a-zA-Z_][a-zA-Z_0-9]* ;

WS                : [ \t\r\n]+ -> skip ;
COMMENT           : '//' ~[\r\n]* -> skip ;
LINE_COMMENT      : '/*' .*? '*/' -> skip ;

