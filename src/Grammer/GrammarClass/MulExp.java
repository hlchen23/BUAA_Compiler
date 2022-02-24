package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;
import SymTable.DataType;

import java.util.ArrayList;

public class MulExp extends Node {

    private ArrayList<Op> ops = new ArrayList<>();
    private ArrayList<UnaryExp> unaryExps = new ArrayList<>();

    private enum Op {
        MULT,
        DIV,
        MOD
    }

    public void analyze() throws EOF {
        UnaryExp unaryExp = new UnaryExp();
        unaryExps.add(unaryExp);
        unaryExp.analyze();
        OutputList.addToList(GrammarType.MulExp);
        Grammar.nextToken();
        while (
                (Grammar.token.getTokenType() == TokenType.MULT)
                || (Grammar.token.getTokenType() == TokenType.DIV)
                || (Grammar.token.getTokenType() == TokenType.MOD)
        ) {
            if (Grammar.token.getTokenType() == TokenType.MULT) {
                ops.add(Op.MULT);
            }
            else if (Grammar.token.getTokenType() == TokenType.DIV) {
                ops.add(Op.DIV);
            }
            else { // MOD
                ops.add(Op.MOD);
            }
            unaryExp = new UnaryExp();
            unaryExps.add(unaryExp);
            unaryExp.analyze();
            OutputList.addToList(GrammarType.MulExp);
            Grammar.nextToken();
        }
        Grammar.retract();
    }

    public void makeTable() {
        for (UnaryExp unaryExp : unaryExps) {
            unaryExp.makeTable();
        }
    }

    public int eval() {
        int index = 0;
        UnaryExp unaryExp = unaryExps.get(index++);
        int val = unaryExp.eval();
        for (Op op : ops) {
            if (op == Op.MULT) {
                val *= unaryExps.get(index++).eval();
            }
            else if (op == Op.DIV){
                val /= unaryExps.get(index++).eval();
            }
            else { // MOD
                val %= unaryExps.get(index++).eval();
            }
        }
        return val;
    }

    public DataType getDataType() {
        return unaryExps.get(0).getDataType();
    }

    public String createMidCode() {
        int index = 0;
        String tmp = unaryExps.get(index++).createMidCode();
        for (Op op : ops) {
            if (op == Op.MULT) {
                tmp = MidCodeFactory.createMidCode(Opt.MULT,tmp,unaryExps.get(index++).createMidCode());
            }
            else if (op == Op.DIV) {
                tmp = MidCodeFactory.createMidCode(Opt.DIV,tmp,unaryExps.get(index++).createMidCode());
            }
            else { // MOD
                tmp = MidCodeFactory.createMidCode(Opt.MOD,tmp,unaryExps.get(index++).createMidCode());
            }
        }
        return tmp;
    }
}
