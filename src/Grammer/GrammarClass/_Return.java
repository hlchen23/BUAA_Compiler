package Grammer.GrammarClass;

import Grammer.Grammar;
import Lexer.Token;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import Error.*;

public class _Return extends Node {

    private Type type;
    private Exp exp;

    private Token _returntk;

    public enum Type {
        VOID,
        INT
    }

    public Token get_returntk() {
        return _returntk;
    }

    public Type getType() {
        return type;
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.RETURNTK) {
            _returntk = Grammar.token;
            Grammar.nextToken();
            // 这里要读Exp的FIRST集
            // 不能通过判断有无;决定是否解析Exp
            // Exp->+ - ! Ident ( Number
            if (
                    (Grammar.token.getTokenType() == TokenType.PLUS)
                    || (Grammar.token.getTokenType() == TokenType.MINU)
                    || (Grammar.token.getTokenType() == TokenType.NOT)
                    || (Grammar.token.getTokenType() == TokenType.IDENFR)
                    || (Grammar.token.getTokenType() == TokenType.LPARENT)
                    || (Grammar.token.getTokenType() == TokenType.INTCON)
            ) {
                type = Type.INT;
                Grammar.retract();
                exp = new Exp();
                exp.analyze();
            }
            else {
                type = Type.VOID;
                Grammar.retract();
            }
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                // 缺少; 报错
                MyError.add_lack_semi();
            }
        }
    }

    public void makeTable() {
        if (type == Type.INT) {
            exp.makeTable();
        }
    }

    public void createMidCode() {
        // 把行号传递进去
        if (type == Type.INT) {
            MidCodeFactory.createMidCode(Opt.RETURN,exp.createMidCode(),String.valueOf(_returntk.getLineNo()));
        }
        else {
            MidCodeFactory.createMidCode(Opt.RETURN,String.valueOf(_returntk.getLineNo()));
        }
    }
}
