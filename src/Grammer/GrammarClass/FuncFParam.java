package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.Token;
import Lexer.TokenType;
import MidCode.MidCodeElement.MidCode;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;
import Error.*;
import SymTable.DataType;
import SymTable.IdentType;
import SymTable.StackTable;
import SymTable.TableItem;


import java.util.ArrayList;
import java.util.Arrays;

public class FuncFParam extends Node {

    private Type type;
    private BType bType;
    private _Ident _ident;
    // 数组形参 第一个为默认的[] 预先填入一个空的ConstExp对象
    private ArrayList<ConstExp> constExps =
            new ArrayList<>(Arrays.asList(new ConstExp()));

    private enum Type {
        SINGLE,
        ARRAY_1,
        ARRAY_2
    }

    public void analyze() throws EOF {
        bType = new BType();
        bType.analyze();
        _ident = new _Ident();
        _ident.analyze();
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.LBRACK) {
            type = Type.ARRAY_1;
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.RBRACK) {
                // 缺少] 报错
                MyError.add_lack_rbrack();
            }
            Grammar.nextToken();
            while (Grammar.token.getTokenType() == TokenType.LBRACK) {
                type = Type.ARRAY_2; // 语义限制 最多就是二维
                ConstExp constExp = new ConstExp();
                constExps.add(constExp);
                constExp.analyze();
                Grammar.nextToken();
                if (Grammar.token.getTokenType() != TokenType.RBRACK) {
                    MyError.add_lack_rbrack();
                }
                Grammar.nextToken();
            }
            Grammar.retract();
        }
        else {
            type = Type.SINGLE;
            Grammar.retract();
        }
        OutputList.addToList(GrammarType.FuncFParam);
    }

    public Token getIdentToken() {
        return _ident.getIdentToken();
    }

    public void makeTable(TableItem func) {
        if (bType.getType() == BType.Type.INT) {
            String name = _ident.getIdentToken().getRawString();
            if (StackTable.reDef(name,false)) {
                MyError.add_reDef(_ident.getIdentToken());
                // 如果同名直接报错并返回
                return;
            }
            // 不同名
            ArrayList<TableItem> params = func.getParas(); // 函数的形参表
            TableItem item = new TableItem();
            item.setIdentName(_ident.getIdentToken().getRawString());
            item.setRename(Grammar.rename + (Grammar.autoRenameIndex++) + "_" + _ident.getIdentToken().getRawString());
            item.setIdentType(IdentType.PARA);
            item.setBelong(Grammar.belong); // 记录形参所属的函数
            // 确定数组还是单变量
            if (type == Type.SINGLE) {
                item.setDataType(DataType.INT);
            }
            else if (type == Type.ARRAY_1) {
                item.setDataType(DataType.INT_ARRAY_1);
            }
            else if (type == Type.ARRAY_2) {
                item.setDataType(DataType.INT_ARRAY_2);
                // 形参的数组只有第二个位置有值
                item.setArray_dim_2(constExps.get(1).eval());
            }
            item.setDim(Grammar.dim);
            StackTable.push(item);
            params.add(item); // 添加对形参的引用

            createMidCode();
        }
    }

    public void createMidCode() {
        TableItem item = StackTable.def(_ident.getIdentToken().getRawString(),false);
        if (type == Type.SINGLE) {
            MidCodeFactory.createMidCode(Opt.PARA, item.getRename()); // 错误处理已经检查过了 不会空指针异常
        }
        else if (type == Type.ARRAY_1) {
            MidCodeFactory.createMidCode(Opt.PARA_ARRAY, item.getRename());
        }
        else {
            // ARRAY_2
            MidCodeFactory.createMidCode(Opt.PARA_ARRAY,
                    item.getRename(),String.valueOf(item.getArray_dim_2()));
        }
    }
}
