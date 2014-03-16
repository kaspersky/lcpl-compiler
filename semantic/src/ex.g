grammar ex;

program: clasa*;

clasa: 'class' symbol ('inherits' symbol)? feature* 'end' SEP;

feature: classattr | metoda;

classattr: 'var' atribut* 'end' SEP;

localattr: 'local' atribut* 'end';

atribut: type (DouBrOp)? symbol (AttrOp expr)? SEP;

metoda: symbol (formalparam (',' formalparam)*)? ('->' type)? ':' block 'end' SEP;

/* Orice declarare de variabile locale e urmata de un block,
care e format de oricate instructiuni, care pot fi la
randul lor declarari de variabile sau blocuri. Aceasta 
formulare mi-a simplificat crearea scope-urilor */
instruction: (localattr SEP) block | (expr | whileins) SEP;
block: instruction*;

formalparam: type symbol;

/* dispatch */
apel: '[' (obj=expr DotOp)? symbol (expr (',' expr)*)? ']';

/* static dispatch */
stdisp: '[' expr DotOp type DotOp symbol (expr (',' expr)*)? ']';

//vectacces: expr '[' expr ']';

cast: '{' type expr '}';

ifexpr: 'if' expr 'then' block ('else' block)? 'end';

/* Nu consider while expresie, pentru ca nu returneaza nimic. */
whileins: 'while' expr 'loop' block 'end';

/* Tipurile de expresii si reguli de asocieri */
expr:
    expr DotOp symbol |
    Vectaccess=expr '[' expr ']' |
    ifexpr | stdisp | apel | expr OpBrOp expr ',' expr ']' | cast |
    MinusOp expr |
    expr (MultOp | DivOp) expr |
    expr (AddOp | MinusOp) expr |
    expr (LessOp | LessEqOp | EqOp) expr |
    LogNotOp expr |
    NewOp type ('[' expr ClBrOp)? |
    expr (AttrOp<assoc=right>) expr |
    OpParOp expr ')' |
    atom;

atom: symbol | integer | string | voidd;

/* Operatori folositi */
DotOp: '.';
MultOp: '*';
MinusOp: '-';
AddOp: '+';
DivOp: '/';
NewOp: 'new';
LessOp: '<';
LessEqOp: '<=';
EqOp: '==';
AttrOp: '=';
LogNotOp: '!';
OpParOp: '(';
OpBrOp: '[';
DouBrOp: '[]';
ClBrOp: ']';

/* Tipuri de baza */
type: 'Int' | 'String' | symbol;

/* Constante (literali) */
integer: Integer;
string: String;
voidd: Void;
symbol: /* Cuvinte cheie */
    {!(
        _input.LT(1).getText().equals("class") ||
        _input.LT(1).getText().equals("inherits") ||
        _input.LT(1).getText().equals("end") ||
        _input.LT(1).getText().equals("var") ||
        _input.LT(1).getText().equals("local") ||
        _input.LT(1).getText().equals("void") ||
        _input.LT(1).getText().equals("new") ||
        _input.LT(1).getText().equals("if") ||
        _input.LT(1).getText().equals("then") ||
        _input.LT(1).getText().equals("else") ||
        _input.LT(1).getText().equals("while") ||
        _input.LT(1).getText().equals("loop")
    )}? Identifier;

/* Tokeni de baza */
Void: 'void';
Integer: DIGIT+;
Identifier: LETTER ('_' | LETTER | DIGIT)*;
String : '"' ( '\\"' | . )*? '"' {setText(getText().substring(1, getText().length()-1));};

fragment DIGIT: '0'..'9';
fragment LETTER: 'a'..'z'|'A'..'Z';
WS: (' '|'\t'|'\n'|'\r') -> skip;
COMMENT: '#' (~('\n'|'\r'))* -> skip;
SEP: ';';
