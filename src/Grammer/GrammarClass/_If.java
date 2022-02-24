package Grammer.GrammarClass;

import Grammer.Grammar;
import Lexer.TokenType;
import MidCode.Opt;
import MidCode.MidCodeFactory;
import MyException.EOF;
import Error.*;

public class _If extends Node {

    private Type type;
    private Cond cond;
    private Stmt ifStmt;
    private Stmt elseStmt;

    private enum Type {
        ELSE,
        NO_ELSE
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.IFTK) {
            Grammar.nextToken();
            if (Grammar.token.getTokenType() == TokenType.LPARENT) {
                cond = new Cond();
                cond.analyze();
                Grammar.nextToken();
                if (Grammar.token.getTokenType() != TokenType.RPARENT) {
                    // 缺少) 报错
                    MyError.add_lack_rparent();
                }
                ifStmt = new Stmt();
                ifStmt.analyze();
                Grammar.nextToken();
                if (Grammar.token.getTokenType() == TokenType.ELSETK) {
                    type = Type.ELSE;
                    elseStmt = new Stmt();
                    elseStmt.analyze();
                }
                else {
                    type = Type.NO_ELSE;
                    Grammar.retract();
                }
            }
        }
    }

    public void makeTable() {
        if (type == Type.ELSE) {
            // ELSE
            cond.makeTable();
            String labelIf = MidCodeFactory.createAutoLabel();
            String labelElse = MidCodeFactory.createAutoLabel();
            String labelEnd = MidCodeFactory.createAutoLabel();
            cond.createMidCode(labelIf);
            MidCodeFactory.createMidCode(Opt.GOTO,labelElse);
            MidCodeFactory.createMidCode(Opt.IF,labelIf);
            ifStmt.makeTable();
            MidCodeFactory.createMidCode(Opt.GOTO,labelEnd);
            MidCodeFactory.createMidCode(Opt.ELSE,labelElse);
            elseStmt.makeTable();
            MidCodeFactory.createMidCode(Opt.END_IF,labelEnd);
        }
        else {
            // NO_ELSE
            cond.makeTable();
            String labelIf = MidCodeFactory.createAutoLabel();
            String labelEnd = MidCodeFactory.createAutoLabel();
            cond.createMidCode(labelIf);
            MidCodeFactory.createMidCode(Opt.GOTO,labelEnd);
            MidCodeFactory.createMidCode(Opt.IF,labelIf);
            ifStmt.makeTable();
            MidCodeFactory.createMidCode(Opt.END_IF,labelEnd);
        }
    }
}
