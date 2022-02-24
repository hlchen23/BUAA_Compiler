package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MyException.EOF;

public class UnaryOp extends Node {

    private Op op;

    public enum Op {
        POS, // +
        NEG, // -
        NOT  // !
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.PLUS) {
            op = Op.POS;
        }
        else if (Grammar.token.getTokenType() == TokenType.MINU) {
            op = Op.NEG;
        }
        else if (Grammar.token.getTokenType() == TokenType.NOT) {
            op = Op.NOT;
        }
        OutputList.addToList(GrammarType.UnaryOp);
    }

    public Op getOp() {
        return op;
    }

    public void makeTable() {

    }
}
