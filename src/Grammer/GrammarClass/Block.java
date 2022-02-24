package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MyException.EOF;

import java.util.ArrayList;
import java.util.HashSet;

import Error.*;

public class Block extends Node {
    private ArrayList<BlockItem> blockItems = new ArrayList<>();

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.LBRACE) {
            Grammar.nextToken();
            while (Grammar.token.getTokenType() != TokenType.RBRACE) {
                Grammar.retract();
                BlockItem blockItem = new BlockItem();
                blockItems.add(blockItem);
                blockItem.analyze();
                Grammar.nextToken();
            }
        }
        OutputList.addToList(GrammarType.Block);
    }

//    public void checkReDef() {
//        // 不需要检查形参
//        HashSet<String> names = new HashSet<>();
//        // 检查本层block的Decl
//        for (BlockItem blockItem:blockItems) {
//            ArrayList<Token> tokens = blockItem.getIdentTokens();
//            for (Token token :tokens) {
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
//        for (BlockItem blockItem :blockItems) {
//            // 继续向下查
//            blockItem.checkReDef();
//        }
//    }

//    public void checkReDef(ArrayList<Token> params) {
//        HashSet<String> names = new HashSet<>();
//        // 检查形参部分
//        for (Token token:params) {
//            String name = token.getRawString();
//            if (names.contains(name)) {
//                // 报错
//                MyError error = new MyError();
//                error.setErrorType(ErrorType.NAME_REDIFNED);
//                error.setToken(token);
//                error.setMsg(String.format("name %s redefined!",name));
//                error.setLineNo(token.getLineNo());
//                MyError.addErrors(error);
//            }
//            else {
//                names.add(name);
//            }
//        }
//        // 检查本层block的Decl
//        for (BlockItem blockItem:blockItems) {
//            ArrayList<Token> tokens = blockItem.getIdentTokens();
//            for (Token token :tokens) {
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
//        for (BlockItem blockItem :blockItems) {
//            // 继续向下查
//            blockItem.checkReDef();
//        }
//    }

    public void makeTable() {
        for (BlockItem blockItem : blockItems) {
            blockItem.makeTable();
        }
    }

    public _Return getReturn() {
        if (blockItems.size() == 0) {
            return null;
        } else {
            return blockItems.get(blockItems.size() - 1).getReturn();
        }
    }
}
