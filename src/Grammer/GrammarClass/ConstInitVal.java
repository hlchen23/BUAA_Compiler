package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MyException.EOF;

import java.util.ArrayList;

public class ConstInitVal extends Node {

    private Type type;
    private ConstExp constExp; // 简单变量
    private ArrayList<ConstInitVal> constInitVals = new ArrayList<>(); // 数组

    private enum Type {
        SINGLE,
        ARRAY
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.LBRACE) {
            type = Type.ARRAY;
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.RBRACE) {
                Grammar.retract();
                ConstInitVal constInitVal = new ConstInitVal();
                constInitVals.add(constInitVal);
                constInitVal.analyze();
                Grammar.nextToken();
                while (Grammar.token.getTokenType() == TokenType.COMMA) {
                    constInitVal = new ConstInitVal();
                    constInitVals.add(constInitVal);
                    constInitVal.analyze();
                    Grammar.nextToken();
                }
                if (Grammar.token.getTokenType() == TokenType.RBRACE) {;}
            }
        }
        else {
            type = Type.SINGLE;
            Grammar.retract();
            constExp = new ConstExp();
            constExp.analyze();
        }
        OutputList.addToList(GrammarType.ConstInitVal);
    }

    public void makeTable() {
        if (type == Type.SINGLE) {
            constExp.makeTable();
        }
        else if (type == Type.ARRAY) {
            for (ConstInitVal constInitVal : constInitVals) {
                constInitVal.makeTable();
            }
        }
    }

    public int eval() {
        return constExp.eval();
    }

    public ArrayList<Integer> arrEval() {
        if (type == Type.ARRAY) {
            ArrayList<Integer> ret = new ArrayList<>();
            for (ConstInitVal constInitVal:constInitVals) {
                ret.addAll(constInitVal.arrEval());
            }
            return ret;
        }
        else {
            ArrayList<Integer> ret = new ArrayList<>();
            int val = constExp.eval();
            ret.add(val);
            return ret;
        }
    }
}
