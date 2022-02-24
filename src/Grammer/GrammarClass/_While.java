package Grammer.GrammarClass;

import Grammer.Grammar;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;
import Error.*;

public class _While extends Node {
    private Cond cond;
    private Stmt stmt;

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.WHILETK) {
            Grammar.nextToken();
            if (Grammar.token.getTokenType() == TokenType.LPARENT) {
                cond = new Cond();
                cond.analyze();
                Grammar.nextToken();
                if (Grammar.token.getTokenType() != TokenType.RPARENT) {
                    // 缺少) 报错
                    MyError.add_lack_rparent();
                }
                stmt = new Stmt();
                Grammar.loop += 1;
                stmt.analyze();
                Grammar.loop -= 1;
            }
        }
    }

    public void makeTable() {
        String label_begin_while = MidCodeFactory.createAutoLabel();
        String label_end_while = MidCodeFactory.createAutoLabel();

        MidCodeFactory.enterLoop(label_begin_while,label_end_while);

        MidCodeFactory.createMidCode(Opt.BEGIN_WHILE,label_begin_while);
        cond.makeTable();
        String labelIf = MidCodeFactory.createAutoLabel();
        cond.createMidCode(labelIf);
        MidCodeFactory.createMidCode(Opt.GOTO,label_end_while);
        MidCodeFactory.createMidCode(Opt.IF,labelIf);
        stmt.makeTable();
        MidCodeFactory.createMidCode(Opt.GOTO,label_begin_while);
        MidCodeFactory.createMidCode(Opt.END_WHILE,label_end_while);

        MidCodeFactory.leaveLoop();
    }
}
