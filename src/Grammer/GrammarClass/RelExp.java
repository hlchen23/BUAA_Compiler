package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import java.util.ArrayList;

public class RelExp extends Node {

    private ArrayList<Op> ops = new ArrayList<>();
    private ArrayList<AddExp> addExps = new ArrayList<>();

    private enum Op {
        LSS, // <
        GRE, // >
        LEQ, // <=
        GEQ  // >=
    }

    public void analyze() throws EOF {
        AddExp addExp = new AddExp();
        addExps.add(addExp);
        addExp.analyze();
        OutputList.addToList(GrammarType.RelExp);
        Grammar.nextToken();
        while (
                (Grammar.token.getTokenType() == TokenType.LSS)
                || (Grammar.token.getTokenType() == TokenType.LEQ)
                || (Grammar.token.getTokenType() == TokenType.GRE)
                || (Grammar.token.getTokenType() == TokenType.GEQ)
        ) {
            if (Grammar.token.getTokenType() == TokenType.LSS) {
                ops.add(Op.LSS);
            }
            else if (Grammar.token.getTokenType() == TokenType.LEQ) {
                ops.add(Op.LEQ);
            }
            else if (Grammar.token.getTokenType() == TokenType.GRE) {
                ops.add(Op.GRE);
            }
            else { // GEQ
                ops.add(Op.GEQ);
            }
            addExp = new AddExp();
            addExps.add(addExp);
            addExp.analyze();
            OutputList.addToList(GrammarType.RelExp);
            Grammar.nextToken();
        }
        Grammar.retract();
    }

    public void makeTable() {
        for (AddExp addExp:addExps) {
            addExp.makeTable();
        }
    }

    public String createMidCode() {
        int index = 0;
        String tmp = addExps.get(index++).createMidCode();
        for (Op op : ops) {
            if (op == Op.LSS) {
                tmp = MidCodeFactory.createMidCode(Opt.LSS,tmp,addExps.get(index++).createMidCode());
            }
            else if (op == Op.GRE) {
                tmp = MidCodeFactory.createMidCode(Opt.GRE,tmp,addExps.get(index++).createMidCode());
            }
            else if (op == Op.LEQ) {
                tmp = MidCodeFactory.createMidCode(Opt.LEQ,tmp,addExps.get(index++).createMidCode());
            }
            else { // GEQ
                tmp = MidCodeFactory.createMidCode(Opt.GEQ,tmp,addExps.get(index++).createMidCode());
            }
        }
        return tmp;
    }
}
