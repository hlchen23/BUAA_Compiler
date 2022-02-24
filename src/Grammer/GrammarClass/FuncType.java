package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MyException.EOF;

public class FuncType extends Node {

    private Type type;

    public enum Type {
        VOID,
        INT
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.VOIDTK) {
            type = Type.VOID;
        }
        else { // INTTK
            type = Type.INT;
        }
        OutputList.addToList(GrammarType.FuncType);
    }

    public Type getType() {
        return type;
    }

    public void makeTable() {

    }
}
