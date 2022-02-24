package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MyException.EOF;

import java.util.ArrayList;

public class InitVal extends Node {

    private Type type;
    private Exp exp; // 简单变量
    private ArrayList<InitVal> initVals = new ArrayList<>(); // 数组

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
                InitVal initVal = new InitVal();
                initVals.add(initVal);
                initVal.analyze();
                Grammar.nextToken();
                while (Grammar.token.getTokenType() == TokenType.COMMA) {
                    initVal = new InitVal();
                    initVals.add(initVal);
                    initVal.analyze();
                    Grammar.nextToken();
                }
                if (Grammar.token.getTokenType() == TokenType.RBRACE) {;}
            }
        }
        else {
            type = Type.SINGLE;
            Grammar.retract();
            exp = new Exp();
            exp.analyze();
        }
        OutputList.addToList(GrammarType.InitVal);
    }

    public void makeTable() {
        if (type == Type.SINGLE) {
            exp.makeTable();
        }
        else if (type == Type.ARRAY) {
            for (InitVal initVal:initVals) {
                initVal.makeTable();
            }
        }
    }

    public String createMidCode() {
        return exp.createMidCode();
    }

    public ArrayList<String> arrCreateMidCode() {
        if (type == Type.ARRAY) {
            ArrayList<String> ret = new ArrayList<>();
            for (InitVal initVal : initVals) {
                ret.addAll(initVal.arrCreateMidCode());
            }
            return ret;
        }
        else {
            // SINGLE
            ArrayList<String> ret = new ArrayList<>();
            String tmp = exp.createMidCode();
            ret.add(tmp);
            return ret;
        }
    }
}
