package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import java.util.ArrayList;

public class EqExp extends Node {

    // \ensures ops.length + 1 == relExps.length;
    private ArrayList<Op> ops = new ArrayList<>();
    private ArrayList<RelExp> relExps = new ArrayList<>();

    private enum Op{
        EQL, // ==
        NEQ  // !=
    }

    public void analyze() throws EOF {
        RelExp relExp = new RelExp();
        relExps.add(relExp);
        relExp.analyze();
        OutputList.addToList(GrammarType.EqExp);
        Grammar.nextToken();
        while ((Grammar.token.getTokenType() == TokenType.EQL)
        || (Grammar.token.getTokenType() == TokenType.NEQ)) {
            if (Grammar.token.getTokenType() == TokenType.EQL) {
                ops.add(Op.EQL);
            }
            else {ops.add(Op.NEQ);}
            relExp = new RelExp();
            relExps.add(relExp);
            relExp.analyze();
            OutputList.addToList(GrammarType.EqExp);
            Grammar.nextToken();
        }
        Grammar.retract();
    }

    public void makeTable() {
        for (RelExp relExp:relExps) {
            relExp.makeTable();
        }
    }

    public String createMidCode() {
        int index = 0;
        String tmp = relExps.get(index++).createMidCode();
        for (Op op : ops) {
            if (op == Op.EQL) {
                tmp = MidCodeFactory.createMidCode(Opt.EQL,tmp,relExps.get(index++).createMidCode());
            }
            else { // NEQ
                tmp = MidCodeFactory.createMidCode(Opt.NEQ,tmp,relExps.get(index++).createMidCode());
            }
        }
        return tmp;
    }
}
