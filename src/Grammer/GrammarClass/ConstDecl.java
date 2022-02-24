package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;
import Error.*;

import java.util.ArrayList;

public class ConstDecl extends Node {
    private BType bType;
    private ArrayList<ConstDef> constDefs = new ArrayList<>();

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.CONSTTK) {
            bType = new BType();
            bType.analyze();
            ConstDef constDef = new ConstDef();
            constDefs.add(constDef);
            constDef.analyze();
            Grammar.nextToken();
            while (Grammar.token.getTokenType() == TokenType.COMMA) {
                constDef = new ConstDef();
                constDefs.add(constDef);
                constDef.analyze();
                Grammar.nextToken();
            }
            if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                MyError.add_lack_semi();
            }
            // 如果是; ;已经被读入了
        }
        OutputList.addToList(GrammarType.ConstDecl);
    }

    public ArrayList<Token> getIdentTokens() {
        ArrayList<Token> tokens = new ArrayList<>();
        for (ConstDef constDef : constDefs) {
            tokens.add(constDef.getIdentToken());
        }
        return tokens;
    }

    public void makeTable() {
        for (ConstDef constDef:constDefs) {
            constDef.makeTable(bType);
        }
    }
}
