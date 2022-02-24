package Grammer.GrammarClass;

import Grammer.Grammar;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;
import Error.*;
import SymTable.IdentType;
import SymTable.StackTable;
import SymTable.TableItem;

public class _Assign extends Node {
    private LVal lVal;
    private Exp exp;

    public void setlVal(LVal lVal) {
        this.lVal = lVal;
    }

    public void setExp(Exp exp) {
        this.exp = exp;
    }

    public void analyze() throws EOF {
        lVal = new LVal();
        lVal.analyze();
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.ASSIGN) {
            exp = new Exp();
            exp.analyze();
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.SEMICN) {
                // 缺少; 报错
                MyError.add_lack_semi();
            }
        }
    }

    public void makeTable() {
        // 检查是否定义
        String name = lVal.get_ident().getIdentToken().getRawString();
        TableItem item = StackTable.def(name,false);
        if (item != null) {
            // 检查是否更改常量的值
            if (item.getIdentType() == IdentType.CONST) {
                // 修改常量的值 报错
                MyError.add_const_varied(lVal.get_ident().getIdentToken());
            }
        }
        lVal.makeTable();
        exp.makeTable();
    }

    public void createMidCode() {
        String src = exp.createMidCode();
        String dst = lVal.createMidCode();
        MidCodeFactory.createMidCode(
                Opt.ASSIGN,src,dst);
        // 数组元素被赋值要回写
        lVal.writeBack(dst);
    }
}
