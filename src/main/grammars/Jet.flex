package com.github.xepozz.wippy.jet.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.github.xepozz.wippy.jet.psi.WippyJetTypes;

%%

%class _WippyJetLexer
%implements FlexLexer
%unicode
%public
%function advance
%type IElementType

WHITE_SPACE = [ \t\f\r\n]+
ID          = [a-zA-Z_][a-zA-Z0-9_]*
FIELD       = \.([a-zA-Z_][a-zA-Z0-9_]*)?
NUMBER      = [0-9]+(\.[0-9]+)?([eE][+-]?[0-9]+)?
DQ_STRING   = \"([^\"\\]|\\.)*\"
RAW_STRING  = `[^`]*`

%state IN_ACTION, IN_COMMENT

%%

<YYINITIAL> {
    "{*"            { yybegin(IN_COMMENT); return WippyJetTypes.JET_COMMENT; }
    "{{" "-"?       { yybegin(IN_ACTION); return WippyJetTypes.JET_OPEN; }
    [^{]+           { return WippyJetTypes.JET_CONTENT; }
    "{"             { return WippyJetTypes.JET_CONTENT; }
}

<IN_COMMENT> {
    "*}"            { yybegin(YYINITIAL); return WippyJetTypes.JET_COMMENT; }
    [^*]+           { return WippyJetTypes.JET_COMMENT; }
    "*"             { return WippyJetTypes.JET_COMMENT; }
}

<IN_ACTION> {
    {WHITE_SPACE}   { return TokenType.WHITE_SPACE; }
    "-"? "}}"       { yybegin(YYINITIAL); return WippyJetTypes.JET_CLOSE; }

    "extends"       { return WippyJetTypes.KW_EXTENDS; }
    "import"        { return WippyJetTypes.KW_IMPORT; }
    "include"       { return WippyJetTypes.KW_INCLUDE; }
    "block"         { return WippyJetTypes.KW_BLOCK; }
    "end"           { return WippyJetTypes.KW_END; }
    "yield"         { return WippyJetTypes.KW_YIELD; }
    "content"       { return WippyJetTypes.KW_CONTENT; }
    "if"            { return WippyJetTypes.KW_IF; }
    "else"          { return WippyJetTypes.KW_ELSE; }
    "range"         { return WippyJetTypes.KW_RANGE; }
    "try"           { return WippyJetTypes.KW_TRY; }
    "catch"         { return WippyJetTypes.KW_CATCH; }
    "return"        { return WippyJetTypes.KW_RETURN; }
    "and"           { return WippyJetTypes.KW_AND; }
    "or"            { return WippyJetTypes.KW_OR; }
    "not"           { return WippyJetTypes.KW_NOT; }
    "nil"           { return WippyJetTypes.KW_NIL; }
    "msg"           { return WippyJetTypes.KW_MSG; }
    "trans"         { return WippyJetTypes.KW_TRANS; }
    "true"          { return WippyJetTypes.BOOL; }
    "false"         { return WippyJetTypes.BOOL; }

    ":="            { return WippyJetTypes.DECL; }
    "=="            { return WippyJetTypes.EQ; }
    "!="            { return WippyJetTypes.NEQ; }
    ">="            { return WippyJetTypes.GE; }
    "<="            { return WippyJetTypes.LE; }
    "="             { return WippyJetTypes.ASSIGN; }
    ">"             { return WippyJetTypes.GT; }
    "<"             { return WippyJetTypes.LT; }
    "+"             { return WippyJetTypes.PLUS; }
    "-"             { return WippyJetTypes.MINUS; }
    "*"             { return WippyJetTypes.MUL; }
    "/"             { return WippyJetTypes.DIV; }
    "%"             { return WippyJetTypes.MOD; }
    "|"             { return WippyJetTypes.PIPE; }
    "?"             { return WippyJetTypes.QUESTION; }
    ":"             { return WippyJetTypes.COLON; }
    "("             { return WippyJetTypes.LPAREN; }
    ")"             { return WippyJetTypes.RPAREN; }
    "["             { return WippyJetTypes.LBRACK; }
    "]"             { return WippyJetTypes.RBRACK; }
    ","             { return WippyJetTypes.COMMA; }
    ";"             { return WippyJetTypes.SEMI; }
    "_"             { return WippyJetTypes.UNDERSCORE; }

    {FIELD}         { return WippyJetTypes.FIELD; }
    {NUMBER}        { return WippyJetTypes.NUMBER; }
    {DQ_STRING}     { return WippyJetTypes.STRING; }
    {RAW_STRING}    { return WippyJetTypes.RAW_STRING; }
    {ID}            { return WippyJetTypes.IDENT; }

    [^]             { return TokenType.BAD_CHARACTER; }
}

[^]                 { return TokenType.BAD_CHARACTER; }
