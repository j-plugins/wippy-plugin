package com.github.xepozz.wippy.lang.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.TokenType;
import com.github.xepozz.wippy.lang.psi.WippyLuaTypes;

%%

%class _WippyLuaLexer
%implements FlexLexer
%unicode
%public
%function advance
%type IElementType

%{
    private int bracketLevel = 0;
%}

WHITE_SPACE     = [ \t\f\r\n]+
ID              = [a-zA-Z_][a-zA-Z0-9_]*
DEC_NUMBER      = [0-9]+(\.[0-9]*)?([eE][+-]?[0-9]+)?
HEX_NUMBER      = 0[xX][0-9a-fA-F]+(\.[0-9a-fA-F]*)?([pP][+-]?[0-9]+)?
NUMBER          = {DEC_NUMBER} | {HEX_NUMBER}
SINGLE_STR      = '([^'\\]|\\.)*'
DOUBLE_STR      = \"([^\"\\]|\\.)*\"
SHORT_STRING    = {SINGLE_STR} | {DOUBLE_STR}

%state LONG_STRING, LONG_COMMENT

%%

<YYINITIAL> {
    {WHITE_SPACE}           { return TokenType.WHITE_SPACE; }

    "--[" "="* "["          { bracketLevel = yylength() - 4; yybegin(LONG_COMMENT); return WippyLuaTypes.BLOCK_COMMENT; }
    "--"[^\r\n]*            { return WippyLuaTypes.LINE_COMMENT; }

    "and"                   { return WippyLuaTypes.AND; }
    "break"                 { return WippyLuaTypes.BREAK; }
    "do"                    { return WippyLuaTypes.DO; }
    "else"                  { return WippyLuaTypes.ELSE; }
    "elseif"                { return WippyLuaTypes.ELSEIF; }
    "end"                   { return WippyLuaTypes.END; }
    "false"                 { return WippyLuaTypes.FALSE; }
    "for"                   { return WippyLuaTypes.FOR; }
    "function"              { return WippyLuaTypes.FUNCTION; }
    "goto"                  { return WippyLuaTypes.GOTO; }
    "if"                    { return WippyLuaTypes.IF; }
    "in"                    { return WippyLuaTypes.IN; }
    "local"                 { return WippyLuaTypes.LOCAL; }
    "nil"                   { return WippyLuaTypes.NIL; }
    "not"                   { return WippyLuaTypes.NOT; }
    "or"                    { return WippyLuaTypes.OR; }
    "return"                { return WippyLuaTypes.RETURN; }
    "repeat"                { return WippyLuaTypes.REPEAT; }
    "then"                  { return WippyLuaTypes.THEN; }
    "true"                  { return WippyLuaTypes.TRUE; }
    "until"                 { return WippyLuaTypes.UNTIL; }
    "while"                 { return WippyLuaTypes.WHILE; }
    "type"                  { return WippyLuaTypes.TYPE_KW; }
    "interface"             { return WippyLuaTypes.INTERFACE; }
    "readonly"              { return WippyLuaTypes.READONLY; }
    "as"                    { return WippyLuaTypes.AS; }
    "asserts"               { return WippyLuaTypes.ASSERTS; }
    "is"                    { return WippyLuaTypes.IS; }
    "typeof"                { return WippyLuaTypes.TYPEOF; }
    "keyof"                 { return WippyLuaTypes.KEYOF; }
    "extends"               { return WippyLuaTypes.EXTENDS; }
    "fun"                   { return WippyLuaTypes.FUN; }

    "..."                   { return WippyLuaTypes.ELLIPSIS; }
    ".."                    { return WippyLuaTypes.CONCAT; }
    "=="                    { return WippyLuaTypes.EQEQ; }
    "~="                    { return WippyLuaTypes.NEQ; }
    "<="                    { return WippyLuaTypes.LTE; }
    ">="                    { return WippyLuaTypes.GTE; }
    "<<"                    { return WippyLuaTypes.SHL; }
    ">>"                    { return WippyLuaTypes.SHR; }
    "//"                    { return WippyLuaTypes.IDIV; }
    "->"                    { return WippyLuaTypes.ARROW; }
    "?:"                    { return WippyLuaTypes.QCOLON; }
    "::"                    { return WippyLuaTypes.DCOLON; }

    "+"                     { return WippyLuaTypes.PLUS; }
    "-"                     { return WippyLuaTypes.MINUS; }
    "*"                     { return WippyLuaTypes.MULT; }
    "/"                     { return WippyLuaTypes.DIV; }
    "%"                     { return WippyLuaTypes.MOD; }
    "^"                     { return WippyLuaTypes.EXP; }
    "#"                     { return WippyLuaTypes.LEN; }
    "&"                     { return WippyLuaTypes.BAND; }
    "|"                     { return WippyLuaTypes.BOR; }
    "~"                     { return WippyLuaTypes.BNOT; }
    ">"                     { return WippyLuaTypes.GT; }
    "<"                     { return WippyLuaTypes.LT; }
    "="                     { return WippyLuaTypes.ASSIGN; }
    "("                     { return WippyLuaTypes.LPAREN; }
    ")"                     { return WippyLuaTypes.RPAREN; }
    "{"                     { return WippyLuaTypes.LBRACE; }
    "}"                     { return WippyLuaTypes.RBRACE; }
    "["                     { return WippyLuaTypes.LBRACK; }
    "]"                     { return WippyLuaTypes.RBRACK; }
    ";"                     { return WippyLuaTypes.SEMI; }
    ":"                     { return WippyLuaTypes.COLON; }
    ","                     { return WippyLuaTypes.COMMA; }
    "."                     { return WippyLuaTypes.DOT; }
    "?"                     { return WippyLuaTypes.QUESTION; }
    "!"                     { return WippyLuaTypes.BANG; }
    "@"                     { return WippyLuaTypes.AT; }

    {SHORT_STRING}          { return WippyLuaTypes.STRING; }
    {NUMBER}                { return WippyLuaTypes.NUMBER; }
    {ID}                    { return WippyLuaTypes.IDENT; }

    [^]                     { return TokenType.BAD_CHARACTER; }
}

<LONG_COMMENT> {
    "]" "="* "]"            { yybegin(YYINITIAL); return WippyLuaTypes.BLOCK_COMMENT; }
    [^]                     { return WippyLuaTypes.BLOCK_COMMENT; }
    <<EOF>>                 { yybegin(YYINITIAL); return WippyLuaTypes.BLOCK_COMMENT; }
}

<LONG_STRING> {
    "]" "="* "]"            { yybegin(YYINITIAL); return WippyLuaTypes.STRING; }
    [^]                     { return WippyLuaTypes.STRING; }
    <<EOF>>                 { yybegin(YYINITIAL); return WippyLuaTypes.STRING; }
}
