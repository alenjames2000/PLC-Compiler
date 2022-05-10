# Assignment-1-Lexer


Lexical Structure

<token> ::= <ident> | <int_lit> | <float_lit> | <string_lit> | <reserved>  | '&' | '=' | '!' | ',' | '/'
   	| '==' | '>=' | '>' | '<<' | '<-' | '<=' | '(' | '[' | '<' | '-' | '%' | '!=' | '|' | '+' | '>>' | '->'
   	| '^' | ')' | ']' | ';' | '*'
  	 
<ident> ::= ('a'..'z'|'A'..'Z'|'_'|'$') ('a'..'z'|'A'..'Z'|'$'|'_'|'0'..'9')*  but not <reserved>

<int_lit> ::=  '0'| ( ('1'..'9') ('0'..'9')* )

<float_lit> ::=  ('0'|  ('1'..'9') ('0'..'9')* ) '.' ('0'..'9')+

<string_lit> ::=	'"' ( '\' ( 'b'|'t'|'n'|'f'|'r'|'"'|' ' '|'\')  | NOT('\'|'"') )* '"'   
(This is difficult to read.  Our language handles string literals the same way as Java)

<reserved> ::= <type> | <image_op> | <color_const> | <boolean_lit> | <other_keyword>

<type> ::= 'string' | 'int' | 'float' | 'boolean' | 'color' | 'image' | 'void'

<image_op> ::= 'getWidth' | 'getHeight'

<color_op> ::= 'getRed' | 'getGreen' | 'getBlue'

<color_const> ::= 'BLACK’ | 'BLUE' | 'CYAN' | 'DARK_GRAY' | 'GRAY’
                                      | 'GREEN' | 'LIGHT_GRAY' | 'MAGENTA' | 'ORANGE' | 'PINK'
   			  | 'RED' | 'WHITE' | 'YELLOW'
   				 
<boolean_lit> ::= 'true' | 'false'

<other_keywords> ::= 'if' | 'else' | 'fi' | 'write' | 'console'

<comment> ::= '#' NOT('\n'|'\r')* ('\r'? '\n')?  
(You may assume that a ‘\r’ without a following ‘\n’ will not occur in your input.  The rule means that a comment starts with #, and ends with either \r\n, \n, or the end of the input)

<white space> ::=  (' '|'\t'|'\r'|'\n')+

<white_space> and <comment> separate tokens, but are otherwise ignored. 
Notation
All elements of the alphabet are surrounded by quotes.  Thus '+' is a plus character while + is a metasymbol.
The only place this doesn't work is in the definition of <string_lit> where '  '  ' indicates a quote character.  
The escape sequences in our language are the same as in Java.

Metasymbols
(,) (parentheses) are used for grouping
|   alternative
..  range
* is the Kleene closure:  zero or more instances
+ is the positive closure:  one or more instances
? means 0 or 1 instance, i.e. a? = a | ε
NOT('\n'|'\r')  means any character except newline (\n) and return (\r).
