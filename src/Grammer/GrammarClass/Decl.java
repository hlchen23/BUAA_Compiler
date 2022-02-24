package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;

import java.util.ArrayList;

public class Decl extends Node {


    private Type type;
    private ConstDecl constDecl;
    private VarDecl varDecl;

    private enum Type {
        CONSTDECL,
        VARDECL
    }

//    private State state = State.Start;
//    private enum State {
//        Start,
//        End
//    }

//    public void analyze() throws EOF {
//        if (state == State.Start) {
//            Grammar.nextToken();
//            if (Grammar.token.getTokenType() == TokenType.CONSTTK) {
//                Grammar.retract();
//                Grammar.stack.push(new ConstDecl());
//            }
//            else { // INTTK
//                Grammar.retract();
//                Grammar.stack.push(new VarDecl());
//            }
//            state = State.End;
//        }
//        else {
//            //OutputList.addToList(GrammarType.Decl);
//            Grammar.stack.pop();
//        }
//    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.CONSTTK) {
            Grammar.retract();
            type = Type.CONSTDECL;
            constDecl = new ConstDecl();
            constDecl.analyze();
        }
        else { // INTTK
            Grammar.retract();
            type = Type.VARDECL;
            varDecl = new VarDecl();
            varDecl.analyze();
        }
//        OutputList.addToList(GrammarType.Decl);
    }

    public ArrayList<Token> getIdentTokens() {
        if (type == Type.CONSTDECL) {
            return constDecl.getIdentTokens();
        }
        else {
            return varDecl.getIdentTokens();
        }
    }

    public void makeTable() {
        if (type == Type.CONSTDECL) {
            constDecl.makeTable();
        }
        else {
            varDecl.makeTable();
        }
    }
}
