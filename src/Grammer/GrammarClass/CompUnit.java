package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import java.util.ArrayList;
import java.util.HashSet;

import Error.*;

public class CompUnit extends Node {

    private ArrayList<Decl> decls = new ArrayList<>();
    private ArrayList<FuncDef> funcDefs = new ArrayList<>();
    private MainFuncDef mainFuncDef;

//    private State state = State.Decl;

    public CompUnit() {
    }

//    private enum State {
//        Decl,
//        FuncDef,
//        MainFuncDef,
//        End
//    }

//    public void analyze() throws EOF {
//        if (state == State.Decl) {
//            Grammar.nextToken();
//            if (Grammar.token.getTokenType() == TokenType.CONSTTK) {
//                Grammar.retract();
//                Grammar.stack.push(new Decl());
//            }
//            else if (Grammar.token.getTokenType() == TokenType.INTTK) {
//                Grammar.nextToken();
//                Grammar.nextToken();
//                if ((Grammar.token.getTokenType() == TokenType.SEMICN)
//                ||(Grammar.token.getTokenType() == TokenType.LBRACK)
//                ||(Grammar.token.getTokenType() == TokenType.ASSIGN)
//                ||(Grammar.token.getTokenType() == TokenType.COMMA)) {
//                    Grammar.retract();
//                    Grammar.retract();
//                    Grammar.retract();
//                    Grammar.stack.push(new Decl());
//                } else {
//                    state = State.FuncDef; }
//            } else {
//                state = State.FuncDef; }
//        }
//        else if (state == State.FuncDef) {
//            Grammar.nextToken();
//            if (Grammar.token.getTokenType() == TokenType.VOIDTK) {
//                Grammar.retract();
//                Grammar.stack.push(new FuncDef());
//            }
//            else if (Grammar.token.getTokenType() == TokenType.INTTK) {
//                Grammar.nextToken();
//                if (Grammar.token.getTokenType() == TokenType.IDENFR) {
//                    Grammar.retract();
//                    Grammar.retract();
//                    Grammar.stack.push(new FuncDef());
//                } else {
//                    state = State.MainFuncDef; }
//            } else {
//                state = State.MainFuncDef; }
//        }
//        else if (state == State.MainFuncDef) {
//            Grammar.stack.push(new MainFuncDef());
//            state = State.End;
//        }
//        else {
//            OutputList.addToList(GrammarType.CompUnit);
//            Grammar.stack.pop();
//        }
//    }

    public void analyze() throws EOF {

        while (true) {
            Grammar.mark();
            Grammar.nextToken();
            if (Grammar.token.getTokenType() == TokenType.CONSTTK) {
                Decl decl = new Decl();
                decls.add(decl);
                Grammar.retract();
                decl.analyze();
            }
            else if (Grammar.token.getTokenType() == TokenType.INTTK) {
                Grammar.nextToken();
                Grammar.nextToken();
                if (
                        (Grammar.token.getTokenType() == TokenType.SEMICN)
                        || (Grammar.token.getTokenType() == TokenType.LBRACK)
                        || (Grammar.token.getTokenType() == TokenType.ASSIGN)
                        || (Grammar.token.getTokenType() == TokenType.COMMA)
                ) {
                    Decl decl = new Decl();
                    decls.add(decl);
                    Grammar.retract();
                    Grammar.retract();
                    Grammar.retract();
                    decl.analyze();
                }
                else {
                    Grammar.restore();
                    break;
                }
            }
            else {
                Grammar.restore();
                break;
            }
        }

        while (true) {
            Grammar.mark();
            Grammar.nextToken();
            if (Grammar.token.getTokenType() == TokenType.VOIDTK) {
                FuncDef funcDef = new FuncDef();
                funcDefs.add(funcDef);
                Grammar.retract();
                funcDef.analyze();
            }
            else if (Grammar.token.getTokenType() == TokenType.INTTK) {
                Grammar.nextToken();
                if (Grammar.token.getTokenType() == TokenType.IDENFR) {
                    FuncDef funcDef = new FuncDef();
                    funcDefs.add(funcDef);
                    Grammar.retract();
                    Grammar.retract();
                    funcDef.analyze();
                }
                else {
                    Grammar.restore();
                    break;
                }
            }
            else {
                Grammar.restore();
                break;
            }
        }
        mainFuncDef = new MainFuncDef();
        mainFuncDef.analyze();
        OutputList.addToList(GrammarType.CompUnit);
    }

//    public void semanticCheck() {
//        HashSet<String> names = new HashSet<>();
//        // 判断重名的decl和funcDefs
//        for (Decl decl : decls) {
//            ArrayList<Token> tokens = decl.getIdentTokens();
//            for (Token token:tokens) {
//                String name = token.getRawString();
//                if (names.contains(name)) {
//                    // 报错
//                    MyError error = new MyError();
//                    error.setErrorType(ErrorType.NAME_REDIFNED);
//                    error.setToken(token);
//                    error.setMsg(String.format("name %s redefined!",name));
//                    error.setLineNo(token.getLineNo());
//                    MyError.addErrors(error);
//                }
//                else {
//                    names.add(name);
//                }
//            }
//        }
//        for (FuncDef funcDef : funcDefs) {
//            Token token = funcDef.getIdentToken();
//            String name = token.getRawString();
//            if (names.contains(name)) {
//                // 报错
//                MyError error = new MyError();
//                error.setErrorType(ErrorType.NAME_REDIFNED);
//                error.setToken(token);
//                error.setMsg(String.format("name %s redefined!",name));
//                error.setLineNo(token.getLineNo());
//                MyError.addErrors(error);
//            } else {
//                names.add(name);
//            }
//        }
//        // 检查每个函数内部的重名问题 注意block
//        for (FuncDef funcDef : funcDefs) {
//            funcDef.checkReDef();
//        }
//        // 记得检查main函数
//        mainFuncDef.checkReDef();
//    }

    public void makeTable() {
        Grammar.dim = 0;
        // 遍历树 建立符号表
        // 将语法和语义解耦
        for (Decl decl : decls) {
            decl.makeTable();
        }
        // 程序入口
        MidCodeFactory.createMidCode(Opt.START_PROGRAM);
        // 程序出口
        MidCodeFactory.createMidCode(Opt.END_PROGRAM);
        for (FuncDef funcDef : funcDefs) {
            funcDef.makeTable();
        }
        mainFuncDef.makeTable();
    }
}
