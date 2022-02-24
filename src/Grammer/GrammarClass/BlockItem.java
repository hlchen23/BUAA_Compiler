package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;

import java.util.ArrayList;

public class BlockItem extends Node {

    private Type type;
    private Decl decl;
    private Stmt stmt;

    private enum Type {
        Decl,
        Stmt
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if ((Grammar.token.getTokenType() == TokenType.CONSTTK)
        || (Grammar.token.getTokenType() == TokenType.INTTK)) {
            type = Type.Decl;
            Grammar.retract();
            decl = new Decl();
            decl.analyze();
        }
        else {
            type = Type.Stmt;
            Grammar.retract();
            stmt = new Stmt();
            stmt.analyze();
        }
//        OutputList.addToList(GrammarType.BlockItem);
    }

    public ArrayList<Token> getIdentTokens() {
        if (type == Type.Decl) {
            return decl.getIdentTokens();
        }
        else {
            return new ArrayList<>();
        }
    }

//    public void checkReDef() {
//        if (type == Type.Stmt) {
//            stmt.checkReDef();
//        }
//    }

    public void makeTable() {
        if (type == Type.Stmt) {
            stmt.makeTable();
        }
        else if (type == Type.Decl) {
            decl.makeTable();
        }
    }

    public _Return getReturn() {
        if (type == Type.Decl) {
            return null;
        }
        else {
            return stmt.getReturn();
        }
    }

}
