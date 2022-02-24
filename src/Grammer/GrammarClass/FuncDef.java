package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import Macro.Macro;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;

import Error.MyError;
import Error.ErrorType;
import SymTable.DataType;
import SymTable.IdentType;
import SymTable.StackTable;
import SymTable.TableItem;

import java.util.ArrayList;

public class FuncDef extends Node {
    // 信息填充符号表
    private FuncType funcType;
    private _Ident _ident;
    private FuncFParams funcFParams;
    private Block block;

    // 报错所需 记录末尾的}
    private Token RBrace;

    private boolean hasFuncRParams = false;

    public void analyze() throws EOF {
        funcType = new FuncType();
        funcType.analyze();
        _ident = new _Ident();
        _ident.analyze();
        // 函数重命名
        _ident.getIdentToken().setRawString(Macro.FUN_PREFIX + _ident.getIdentToken().getRawString());

        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.LPARENT) {
            Grammar.nextToken();
            // 这里应该用FuncFParams的FIRST集判断 INT
            if (Grammar.token.getTokenType() == TokenType.INTTK) {
                Grammar.retract();
                hasFuncRParams = true;
                funcFParams = new FuncFParams();
                funcFParams.analyze();
            }
            else {
                Grammar.retract();
            }
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.RPARENT) {
                // 缺少右括号报错
                MyError.add_lack_rparent();
            }
            block = new Block();
            block.analyze();
        }
        RBrace = Grammar.token; // 记录末尾的token '}'
        OutputList.addToList(GrammarType.FuncDef);
    }

    public Token getIdentToken() {
        return _ident.getIdentToken();
    }

//    public void checkReDef() {
//        // 形参属于第一个block的作用域
//        ArrayList<Token> tokens = funcFParams.getIdentTokens();
//        block.checkReDef(tokens);
//    }

    public void makeTable() {
        // 如果函数名重复了要把第二个的函数名也弹栈
        // 否则栈保留函数名
        String name = _ident.getIdentToken().getRawString();
        if (StackTable.reDef(name,true)) {
            MyError.add_reDef(_ident.getIdentToken());
        }
        // 无论是否重定义 因为要判断这个函数里面的内容也需要构造一个域 也要压栈
        TableItem tableItem = new TableItem();
        tableItem.setIdentName(name);
        tableItem.setIdentType(IdentType.FUNC);
        tableItem.setDataType(funcType.getType() == FuncType.Type.INT? DataType.INT: DataType.VOID);
        tableItem.setDim(Grammar.dim);
        StackTable.push(tableItem);
        createMidCode();

        // 记录当前所属的函数
        Grammar.belong = name;
        // 进入形参即作用域要+1
        Grammar.dim += 1;
        if (hasFuncRParams) {
            funcFParams.makeTable(tableItem); // 把函数的传进去
        }
        // block
        block.makeTable();

        // 释放符号表之前需要查一遍return相关的错误
        _Return _return = block.getReturn();
        if (funcType.getType() == FuncType.Type.VOID) {
            if (_return != null) {
                if (_return.getType() != _Return.Type.VOID) {
                    // 报错
                    // MyError.add_voidFunc_return(_return.get_returntk());
                }
            }
        }
        else { // INT
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
        }
        // void类型要函数结束补充一个return确保安全
        if (funcType.getType() == FuncType.Type.VOID) {
            createReturn();
        }
        // 退出函数前把当前作用域内的符号清空
        StackTable.clear();
        // 函数名重复 第二个函数不能留在栈
        if (StackTable.reDef(name,true)) {
            StackTable.pop();
        }
        Grammar.dim -= 1;
        Grammar.belong = "&global";
    }

    public void createMidCode() {
        // 把函数的类型 函数末尾的}记录下来传递到中间代码 便于数据流报错
        if (funcType.getType() == FuncType.Type.VOID) {
            MidCodeFactory.createMidCode(Opt.FUN, _ident.getIdentToken().getRawString(), "void",String.valueOf(RBrace.getLineNo()));
        }
        else if (funcType.getType() == FuncType.Type.INT) {
            MidCodeFactory.createMidCode(Opt.FUN, _ident.getIdentToken().getRawString(), "int",String.valueOf(RBrace.getLineNo()));
        }
    }

    // void类型的函数需要额外补充一个return,防止可能不出现return
    public void createReturn() {
        MidCodeFactory.createMidCode(Opt.RETURN);
    }
}
