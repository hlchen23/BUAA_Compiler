package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;
import Error.*;
import SymTable.StackTable;

public class MainFuncDef extends Node {
    private Block block;
    private Token RBrace;

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.INTTK) {
            Grammar.nextToken();
            if (Grammar.token.getTokenType() == TokenType.MAINTK) {
                Grammar.nextToken();
                if (Grammar.token.getTokenType() == TokenType.LPARENT) {
                    Grammar.nextToken();
                    if (Grammar.token.getTokenType() != TokenType.RPARENT) {
                        MyError.add_lack_rparent();
                    }
                    block = new Block();
                    block.analyze();
                }
            }
        }
        RBrace = Grammar.token;
        OutputList.addToList(GrammarType.MainFuncDef);
    }

//    public void checkReDef() {
//        // main函数不含有形参
//        block.checkReDef();
//    }

    public void makeTable() {
        createMidCode();
        Grammar.belong = "main";
        Grammar.dim += 1;
        block.makeTable();
        // 清空符号表之前检查一下return相关的问题
        _Return _return = block.getReturn();
        // INT
        if (_return == null) {
            // 报错
            //MyError.add_func_noReturn(RBrace);
        }
        else {
            if (_return.getType() != _Return.Type.INT) {
                // 报错
                //MyError.add_func_noReturn(RBrace);
            }
        }

        StackTable.clear();
        Grammar.dim -= 1;
        Grammar.belong = "&global";
    }

    public void createMidCode() {
        // 写死了 int
        MidCodeFactory.createMidCode(Opt.FUN,"main","int",String.valueOf(RBrace.getLineNo()));
    }
}
