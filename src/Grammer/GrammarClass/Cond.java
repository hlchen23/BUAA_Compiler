package Grammer.GrammarClass;

import Grammer.GrammarType;
import Grammer.OutputList;
import MyException.EOF;

public class Cond extends Node {
    private LOrExp lOrExp;

    public void analyze() throws EOF {
        lOrExp = new LOrExp();
        lOrExp.analyze();
        OutputList.addToList(GrammarType.Cond);
    }

    public void makeTable() {
        lOrExp.makeTable();
    }

    public void createMidCode(String label) {
        lOrExp.createMidCode(label);
    }
}
