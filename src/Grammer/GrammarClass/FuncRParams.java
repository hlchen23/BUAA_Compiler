package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import java.util.ArrayList;

public class FuncRParams extends Node {
    private ArrayList<Exp> exps = new ArrayList<>();

    public void analyze() throws EOF {
        Exp exp = new Exp();
        exps.add(exp);
        exp.analyze();
        Grammar.nextToken();
        while (Grammar.token.getTokenType() == TokenType.COMMA) {
            exp = new Exp();
            exps.add(exp);
            exp.analyze();
            Grammar.nextToken();
        }
        Grammar.retract();
        OutputList.addToList(GrammarType.FuncRParams);
    }

    public void makeTable() {
        for (Exp exp:exps) {
            exp.makeTable();
        }
    }

    public int getParamsNum() {
        return exps.size();
    }

    public ArrayList<Exp> getExps() {
        return exps;
    }

    public ArrayList<String> createMidCode() {
        ArrayList<String> pushVars = new ArrayList<>();
        for (Exp exp:exps) {
            // 应该先把里面的全部计算完毕再压栈
            String pushVar = MidCodeFactory.createMidCode(Opt.ASSIGN,exp.createMidCode());
            pushVars.add(pushVar);
        }
        return pushVars;
    }
}
