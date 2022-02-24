package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MyException.EOF;

public class Number extends Node {

    private int number;

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.INTCON) {
            // 记录数值信息
            number = Grammar.token.getValue();
        }
        OutputList.addToList(GrammarType.Number);
    }

    public void makeTable() {

    }

    public int eval() {
        return number;
    }

    public String getNumStr() {
        return String.valueOf(number);
    }
}
