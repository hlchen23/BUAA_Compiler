package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;

import Error.*;

import java.util.ArrayList;

public class VarDecl extends Node {
    private BType bType;
    private ArrayList<VarDef> varDefs = new ArrayList<>();

    public void analyze() throws EOF {
        bType = new BType();
        bType.analyze();
        VarDef varDef = new VarDef();
        varDefs.add(varDef);
        varDef.analyze();
        Grammar.nextToken();
        while (Grammar.token.getTokenType() == TokenType.COMMA) {
            varDef = new VarDef();
            varDefs.add(varDef);
            varDef.analyze();
            Grammar.nextToken();
        }
        if (Grammar.token.getTokenType() != TokenType.SEMICN) {
            // 缺少; 报错
            MyError.add_lack_semi();
        }
        // 如果是; 已经读了
        OutputList.addToList(GrammarType.VarDecl);
    }

    public ArrayList<Token> getIdentTokens() {
        ArrayList<Token> tokens = new ArrayList<>();
        for (VarDef varDef : varDefs) {
            tokens.add(varDef.getIdentToken());
        }
        return tokens;
    }

    public void makeTable() {
        for (VarDef varDef:varDefs) {
            varDef.makeTable(bType);
        }
    }
}
