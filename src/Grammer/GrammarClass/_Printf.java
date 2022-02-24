package Grammer.GrammarClass;

import Grammer.Grammar;
import Lexer.Token;
import Lexer.TokenType;
import MidCode.*;
import MyException.EOF;
import Error.*;

import java.util.ArrayList;

public class _Printf extends Node {

    private String formatString;
    private ArrayList<Exp> exps = new ArrayList<>();

    private int count; // %d的个数 即理论上exps正确的长度

    public void analyze() throws EOF {
        Token printf_token; // 记下printf对应的token 便于报错使用
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.PRINTFTK) {
            printf_token = Grammar.token;
            Grammar.nextToken();
            if (Grammar.token.getTokenType() == TokenType.LPARENT) {
                Grammar.nextToken();
                if (Grammar.token.getTokenType() == TokenType.STRCON) {
                    formatString = Grammar.token.getRawString();
                    count = Grammar.token.getCount();
                    Grammar.nextToken();
                    while (Grammar.token.getTokenType() == TokenType.COMMA) {
                        Exp exp = new Exp();
                        exps.add(exp);
                        exp.analyze();
                        Grammar.nextToken();
                    }
                    if (exps.size() != count) {
                        // %d数量和实际的exp数量不一致 报错
                        MyError.add_printf_num_mismatch(printf_token,count,exps.size());
                    }
                    if (Grammar.token.getTokenType() != TokenType.RPARENT) {
                        // 缺少) 报错
                        MyError.add_lack_rparent();
                    }
                    Grammar.nextToken();
                    if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                        // 缺少; 报错
                        MyError.add_lack_semi();
                    }
                }
            }
        }
    }

    public void makeTable() {
        for (Exp exp:exps) {
            exp.makeTable();
        }
    }

    public void createMidCode() {
        String[] subStrings = formatString.split("%d");
        int len = subStrings.length;
        if (len > 1) {
            subStrings[0] = subStrings[0] + "\"";
            subStrings[len - 1] = "\"" + subStrings[len - 1];
        }
        for (int i = 1; i <= len-2; i++) {
            subStrings[i] = "\"" + subStrings[i] + "\"";
        }
        MidCodeFactory.createMidCode(Opt.PRINT_STR,subStrings[0]);
        for (int i = 1; i < len; i++) {
            MidCodeFactory.createMidCode(Opt.PRINT_INT,exps.get(i-1).createMidCode());
            MidCodeFactory.createMidCode(Opt.PRINT_STR,subStrings[i]);
        }
    }
}
