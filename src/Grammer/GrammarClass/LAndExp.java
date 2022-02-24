package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import java.util.ArrayList;

public class LAndExp extends Node {

    private ArrayList<EqExp> eqExps = new ArrayList<>();

    public void analyze() throws EOF {
        EqExp eqExp = new EqExp();
        eqExps.add(eqExp);
        eqExp.analyze();
        OutputList.addToList(GrammarType.LAndExp);
        Grammar.nextToken();
        while (Grammar.token.getTokenType() == TokenType.AND) {
            eqExp = new EqExp();
            eqExps.add(eqExp);
            eqExp.analyze();
            OutputList.addToList(GrammarType.LAndExp);
            Grammar.nextToken();
        }
        Grammar.retract();
    }

    public void makeTable() {
        for (EqExp eqExp:eqExps){
            eqExp.makeTable();
        }
    }

    public void createMidCode(String label) {
        // 有条件转移 也要有求值
        String tmp;
        for (EqExp eqExp : eqExps) {
            tmp = eqExp.createMidCode();
            MidCodeFactory.createMidCode(Opt.BEZ,tmp,label);
        }
    }
}
