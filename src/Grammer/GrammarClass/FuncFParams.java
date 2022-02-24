package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;
import SymTable.TableItem;

import java.util.ArrayList;

public class FuncFParams extends Node {
    private ArrayList<FuncFParam> funcFParams = new ArrayList<>();

    public void analyze() throws EOF {
        FuncFParam funcFParam = new FuncFParam();
        funcFParams.add(funcFParam);
        funcFParam.analyze();
        Grammar.nextToken();
        while (Grammar.token.getTokenType() == TokenType.COMMA) {
            funcFParam = new FuncFParam();
            funcFParams.add(funcFParam);
            funcFParam.analyze();
            Grammar.nextToken();
        }
        Grammar.retract();
        OutputList.addToList(GrammarType.FuncFParams);
    }

    public ArrayList<Token> getIdentTokens() {
        ArrayList<Token> ret = new ArrayList<>();
        for (FuncFParam funcFParam:funcFParams) {
            ret.add(funcFParam.getIdentToken());
        }
        return ret;
    }

    public void makeTable(TableItem func) {
        for (FuncFParam funcFParam:funcFParams) {
            funcFParam.makeTable(func);
        }
    }
}
