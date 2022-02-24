package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MyException.EOF;

public class BType extends Node {

    private Type type;

    public enum Type {
        INT
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.INTTK) {
            type = Type.INT;
        }
//        OutputList.addToList(GrammarType.BType);
    }

    public Type getType() {
        return type;
    }

    public void makeTable() {

    }
}
