package Grammer.GrammarClass;

import Grammer.Grammar;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;


public class _Ident extends Node {
    // 誊写符号表的层次
    private String name;
    private Token token;

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.IDENFR) {
            token = Grammar.token;
            name = Grammar.token.getRawString();
        }
    }

    public Token getIdentToken() {
        return token;
    }

    public void makeTable() {

    }
}
