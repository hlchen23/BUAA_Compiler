package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import java.util.ArrayList;

public class LOrExp extends Node {

    private ArrayList<LAndExp> lAndExps = new ArrayList<>();

    public void analyze() throws EOF {
        LAndExp lAndExp = new LAndExp();
        lAndExps.add(lAndExp);
        lAndExp.analyze();
        OutputList.addToList(GrammarType.LOrExp);
        Grammar.nextToken();
        while (Grammar.token.getTokenType() == TokenType.OR) {
            lAndExp = new LAndExp();
            lAndExps.add(lAndExp);
            lAndExp.analyze();
            OutputList.addToList(GrammarType.LOrExp);
            Grammar.nextToken();
        }
        Grammar.retract();
    }

    public void makeTable() {
        for (LAndExp lAndExp :lAndExps) {
            lAndExp.makeTable();
        }
    }

    public void createMidCode(String label) {
        // nextLabel是这个lOrExp判断为假时要继续判断的部分的入口标签
        for (LAndExp lAndExp : lAndExps) {
            String nextLabel = MidCodeFactory.createAutoLabel();
            lAndExp.createMidCode(nextLabel);
            // 传入的label是条件主体的入口标签
            MidCodeFactory.createMidCode(Opt.GOTO,label);
            MidCodeFactory.createMidCode(Opt.LABEL,nextLabel);
        }
    }
}
