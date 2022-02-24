package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;

import Error.*;
import SymTable.StackTable;
import SymTable.Table;

public class Stmt extends Node {

    private Type type;

    private _Assign _assign;
    private Exp exp;
    private Block block;
    private _If _if;
    private _While _while;
    private _Break _break;
    private _Continue _continue;
    private _Return _return;
    private _Getint _getint;
    private _Printf _printf;

    private Token lValToken;

    private enum Type {
        ASSIGN,
        EXP,NULL, // [Exp] ';'
        BLOCK,
        IF,
        WHILE,
        BREAK,
        CONTINUE,
        RETURN,
        GETINT,
        PRINTF
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.LBRACE) {
            type = Type.BLOCK;
            Grammar.retract();
            block = new Block();
            block.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.IFTK) {
            type = Type.IF;
            Grammar.retract();
            _if = new _If();
            _if.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.WHILETK) {
            type = Type.WHILE;
            Grammar.retract();
            _while = new _While();
            _while.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.BREAKTK) {
            type = Type.BREAK;
            Grammar.retract();
            _break = new _Break();
            _break.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.CONTINUETK) {
            type = Type.CONTINUE;
            Grammar.retract();
            _continue = new _Continue();
            _continue.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.RETURNTK) {
            type = Type.RETURN;
            Grammar.retract();
            _return = new _Return();
            _return.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.PRINTFTK) {
            type = Type.PRINTF;
            Grammar.retract();
            _printf = new _Printf();
            _printf.analyze();
        }
        else {
            if (
                    (Grammar.token.getTokenType() == TokenType.PLUS)
                    || (Grammar.token.getTokenType() == TokenType.MINU)
                    || (Grammar.token.getTokenType() == TokenType.NOT)
                    || (Grammar.token.getTokenType() == TokenType.LPARENT)
                    || (Grammar.token.getTokenType() == TokenType.INTCON)
            ) {
                Grammar.retract();
                type = Type.EXP;
                exp = new Exp();
                exp.analyze();
                // 判断结尾的分号
                Grammar.nextToken();
                if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                    MyError.add_lack_semi();
                }
            }
            else if (Grammar.token.getTokenType() == TokenType.IDENFR) {
                Grammar.nextToken();
                if (Grammar.token.getTokenType() == TokenType.LPARENT) {
                    Grammar.retract();
                    Grammar.retract();
                    // Exp 里面的UnaryExp中的函数调用
                    type = Type.EXP;
                    exp = new Exp();
                    exp.analyze();
                    Grammar.nextToken();
                    if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                        MyError.add_lack_semi();
                    }
                }
                else { // 如果不是( 那ident属于左值
                    Grammar.retract();
                    Grammar.retract();
                    Grammar.mark();
                    LVal lVal = new LVal();
                    lVal.analyze();
                    lValToken = lVal.getToken(); // 记录下左值的token 便于报错
                    Grammar.nextToken();
                    if (Grammar.token.getTokenType() == TokenType.ASSIGN) {
                        Grammar.nextToken();
                        if (Grammar.token.getTokenType() == TokenType.GETINTTK) {
                            type = Type.GETINT;
                            _getint = new _Getint();
                            _getint.setlVal(lVal);
                            Grammar.nextToken();
                            if (Grammar.token.getTokenType() == TokenType.LPARENT) {
                                Grammar.nextToken();
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
                        else { // 不是getint
                            type = Type.ASSIGN;
                            Grammar.retract();
                            _assign = new _Assign();
                            _assign.setlVal(lVal);
                            Exp exp = new Exp();
                            exp.analyze();
                            _assign.setExp(exp);
                            Grammar.nextToken();
                            if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                                // 缺少; 报错
                                MyError.add_lack_semi();
                            }
                        }
                    } // 不是assign 则左值应当是Exp中的PrimaryExp中的左值
                    else {
                        Grammar.restore();
                        type = Type.EXP;
                        exp = new Exp();
                        exp.analyze();
                        Grammar.nextToken();
                        if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                            // 缺少; 报错
                            MyError.add_lack_semi();
                        }
                    }
                }
            }
            else {
                type = Type.NULL;
                // 必须是; 了否则报错
                if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                    MyError.add_lack_semi();
                }
            }
        }
        OutputList.addToList(GrammarType.Stmt);
    }

//    public void checkReDef() {
//        if (type == Type.BLOCK) {
//            block.checkReDef();
//        }
//    }

    public void makeTable() {
        if (type == Type.ASSIGN) {
            _assign.makeTable();
            _assign.createMidCode();
        }
        else if (type == Type.EXP) {
            exp.makeTable();
            exp.createMidCode();
        }
        else if (type == Type.NULL) {
            // nothing to do
        }
        else if (type == Type.BLOCK) {
            // 维护域
            Grammar.dim += 1;
            block.makeTable();
            StackTable.clear();
            Grammar.dim -= 1;
        }
        else if (type == Type.IF) {
            _if.makeTable();
        }
        else if (type == Type.WHILE) {
            _while.makeTable();
        }
        else if (type == Type.BREAK) {
            _break.makeTable();
        }
        else if (type == Type.CONTINUE) {
            _continue.makeTable();
        }
        else if (type == Type.RETURN) {
            _return.makeTable();
            _return.createMidCode();
        }
        else if (type == Type.GETINT) {
            _getint.makeTable();
            _getint.createMidCode();
        }
        else if (type == Type.PRINTF) {
            _printf.makeTable();
            _printf.createMidCode();
        }
    }

    public _Return getReturn() {
        if (type == Type.RETURN) {
            return _return;
        }
        else {
            return null;
        }
    }
}
