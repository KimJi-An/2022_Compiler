grammar MiniGo;

program	: decl+		{System.out.println("202002481 Rule 0");};

decl	: var_decl	{System.out.println("202002481 Rule 1-1");}
		| fun_decl	{System.out.println("202002481 Rule 1-2");};
		
var_decl: type_spec IDENT ':' 						{System.out.println("202002481 Rule 2-1");}
		| type_spec IDENT '[' ']' ';' 				{System.out.println("202002481 Rule 2-2");}
		| dec_spec IDENT  type_spec					{System.out.println("202002481 Rule 2-3");}
		| dec_spec IDENT type_spec '=' LITERAL		{System.out.println("202002481 Rule 2-4");}
		| dec_spec IDENT '[' LITERAL ']' type_spec 	{System.out.println("202002481 Rule 2-5");};
		
dec_spec	: VAR 	{System.out.println("202002481 Rule 3");};

type_spec	: 
			| INT						{System.out.println("202002481 Rule 4-1");}
			| '[' LITERAL ']'  INT	 	{System.out.println("202002481 Rule 4-2");};
		
fun_decl	: FUNC IDENT '(' params ')' type_spec compound_stmt   {System.out.println("202002481 Rule 5");};

params	: 		
		|param (',' param)*	{System.out.println("202002481 Rule 6");}; 
		
param	: IDENT  			{System.out.println("202002481 Rule 7-1");}			
		| IDENT type_spec 	{System.out.println("202002481 Rule 7-2");};  
		
stmt	: expr_stmt			{System.out.println("202002481 Rule 8-1");}
		| compound_stmt		{System.out.println("202002481 Rule 8-2");}
		| if_stmt			{System.out.println("202002481 Rule 8-3");}
		| for_stmt			{System.out.println("202002481 Rule 8-4");}
		| return_stmt		{System.out.println("202002481 Rule 8-5");};
		
expr_stmt	: expr 			{System.out.println("202002481 Rule 9");};

for_stmt	: FOR expr stmt		{System.out.println("202002481 Rule 10");};

compound_stmt: '{' local_decl* stmt* '}'	{System.out.println("202002481 Rule 11");};

local_decl	: var_decl	{System.out.println("202002481 Rule 12");}; 

if_stmt	:  IF  expr  stmt				{System.out.println("202002481 Rule 13-1");}
		| IF  expr  stmt ELSE stmt 		{System.out.println("202002481 Rule 13-2");};
		
return_stmt	: RETURN				{System.out.println("202002481 Rule 14-1");}
			| RETURN expr			{System.out.println("202002481 Rule 14-2");}
			| RETURN expr ',' expr	{System.out.println("202002481 Rule 14-3");};
			
expr :  (LITERAL|IDENT)											{System.out.println("202002481 Rule 15-1");}					
	 | '(' expr ')'												{System.out.println("202002481 Rule 15-2");}		 					
	 | IDENT '[' expr ']'										{System.out.println("202002481 Rule 15-3");}
	 | IDENT '(' args ')'										{System.out.println("202002481 Rule 15-4");}
	 | op=('-'|'+'|'--'|'++'|'!') expr							{System.out.println("202002481 Rule 15-5");}
	 | left=expr op=('*'|'/'|'%'|'+'|'-') right=expr			{System.out.println("202002481 Rule 15-6");}
	 | left=expr op=(EQ|NE|LE|'<'|GE|'>'|AND|OR) right=expr		{System.out.println("202002481 Rule 15-7");}
	 | IDENT '=' expr											{System.out.println("202002481 Rule 15-8");}						
	 | IDENT '[' expr ']' '=' expr								{System.out.println("202002481 Rule 15-9");};	

args : expr (',' expr)*			{System.out.println("202002481 Rule 16-1");}
	|					 ;

VAR: 'var';
FUNC: 'func';
INT: 'int';

FOR: 'for';
IF: 'if';
ELSE: 'else';
RETURN: 'return';
OR: 'or';
AND: 'and';
LE: '<=';
GE: '>=';
EQ: '==';
NE: '!=';

IDENT  : [a-zA-Z_]
        (   [a-zA-Z_]
        |  [0-9]
        )*;


LITERAL:   DecimalConstant     |   OctalConstant     |   HexadecimalConstant  | NilConstant | WildCardConstant   ;


DecimalConstant
    :   '0'
	|   [1-9] [0-9]*
    ;

OctalConstant
    :   '0'[0-7]*
    ;

HexadecimalConstant
    :   '0' [xX] [0-9a-fA-F] +
    ;
	
NilConstant
	: 'NIL'
	| 'nil'
	;
	
WildCardConstant
	: '_'
	;

WS  :   (   ' '
        |   '\t'
        |   '\r'
        |   '\n'
        )+
	-> channel(HIDDEN)	 
    ;
