package Grammer.GrammarClass;

import Grammer.Grammar;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;
import Error.*;

public class _Continue extends Node {
    // need nothing

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.CONTINUETK) {
            // 语义检查
            if (Grammar.loop == 0) {
                // 非循环块中使用continue 报错
                MyError.add_continue_no_loop(Grammar.token);
            }
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                // 缺少; 报错
                MyError.add_lack_semi();
            }
        }
    }

    public void makeTable() {
        createMidCode();
    }

    public void createMidCode() {
        MidCodeFactory.createMidCode(Opt.CONTINUE,MidCodeFactory.getLoopBegin());
    }
}
