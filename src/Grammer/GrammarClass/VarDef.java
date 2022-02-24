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

import Error.*;
import SymTable.DataType;
import SymTable.IdentType;
import SymTable.StackTable;
import SymTable.TableItem;

import java.util.ArrayList;

public class VarDef extends Node {

    private Type type;
    private _Ident _ident;
    private ArrayList<ConstExp> constExps = new ArrayList<>(); // 维度信息
    private InitVal initVal;

    private int dim_1;
    private int dim_2;

    private enum Type {
        SINGLE,
        ARRAY_1,
        ARRAY_2,
    }

    public void analyze() throws EOF {
        _ident = new _Ident();
        _ident.analyze();
        Grammar.nextToken();
        while (Grammar.token.getTokenType() == TokenType.LBRACK) {
            ConstExp constExp = new ConstExp();
            constExps.add(constExp);
            constExp.analyze();
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.RBRACK) {
                // 缺少] 报错
                MyError.add_lack_rbrack();
            }
            // ]已经读入
            Grammar.nextToken();
        }
        if (Grammar.token.getTokenType() == TokenType.ASSIGN) {
            initVal = new InitVal();
            initVal.analyze();
        }
        else {
            Grammar.retract(); }

        if (constExps.size() == 0) {
            type = Type.SINGLE;
        }
        else if (constExps.size() == 1) {
            type = Type.ARRAY_1;
        }
        else if (constExps.size() == 2) {
            type = Type.ARRAY_2;
        }
        OutputList.addToList(GrammarType.VarDef);
    }

    public Token getIdentToken() {
        return _ident.getIdentToken();
    }

    public void makeTable(Node node) {
        if (node instanceof BType) {
            BType bType = (BType) node;
            TableItem tableItem = new TableItem();
            if (bType.getType() == BType.Type.INT) {

                // 查找是否有重名
                String name = _ident.getIdentToken().getRawString();
                if (StackTable.reDef(name,false)) {
                    MyError.add_reDef(_ident.getIdentToken());
                }
                else {
                    // 没有重定义插入符号表
                    tableItem.setIdentName(_ident.getIdentToken().getRawString());
                    tableItem.setRename(Grammar.rename + (Grammar.autoRenameIndex++) + "_" + _ident.getIdentToken().getRawString());
                    tableItem.setIdentType(IdentType.VAR);
                    tableItem.setBelong(Grammar.belong);
                    // 确定数组的类型 并确定维度信息
                    if (constExps.size() == 0) {
                        tableItem.setDataType(DataType.INT);
                    }
                    else if (constExps.size() == 1) {
                        tableItem.setDataType(DataType.INT_ARRAY_1);
                        dim_1 = constExps.get(0).eval();
                        tableItem.setArray_dim_1(dim_1);
                    }
                    else if (constExps.size() == 2) {
                        tableItem.setDataType(DataType.INT_ARRAY_2);
                        dim_1 = constExps.get(0).eval();
                        dim_2 = constExps.get(1).eval();
                        tableItem.setArray_dim_1(dim_1);
                        tableItem.setArray_dim_2(dim_2);
                    }
                    tableItem.setDim(Grammar.dim);

                    // 先生成右边初始值的中间代码 再把定义的var写入符号表
                    createMidCode();

                    StackTable.push(tableItem);
                }
            }
        }
    }

    public void createMidCode() {
        String name = Grammar.rename + (Grammar.autoRenameIndex - 1) + "_" + _ident.getIdentToken().getRawString();
        if (Grammar.belong.equals(Macro.GLOBAL_MARK)) {
            name += Macro.GLOBAL_VAR_CONST_MARK;
        }
        if (type == Type.SINGLE) {
            // 单变量
            // 此情形是先use后def int a = 2; {int a = a + 1;}
            if (initVal != null) {
                String tmp = initVal.createMidCode();
                MidCodeFactory.createMidCode(Opt.VAR, name);
                MidCodeFactory.createMidCode(Opt.ASSIGN, tmp, name);
            } else {
                MidCodeFactory.createMidCode(Opt.VAR, name);
            }
        }
        else if (type == Type.ARRAY_1) {
            if (initVal != null) {
                ArrayList<String> init = initVal.arrCreateMidCode();
                MidCodeFactory.createMidCode(Opt.VAR_ARRAY,
                        name,
                        String.valueOf(dim_1));
                MidCodeFactory.createMidCode(Opt.ARRAY_ADDR_INIT,name);
                int len = dim_1;
                for (int i = 0; i < len; i++) {
                    MidCodeFactory.createMidCode(Opt.STORE_ARRAY,
                            init.get(i),String.valueOf(i),name);
                }
            } else {
                MidCodeFactory.createMidCode(Opt.VAR_ARRAY,
                        name,
                        String.valueOf(dim_1));
                MidCodeFactory.createMidCode(Opt.ARRAY_ADDR_INIT,name);
            }
        }
        else if (type == Type.ARRAY_2) {
            if (initVal != null) {
                ArrayList<String> init = initVal.arrCreateMidCode();
                MidCodeFactory.createMidCode(Opt.VAR_ARRAY,
                        name,
                        String.valueOf(dim_1),String.valueOf(dim_2));
                MidCodeFactory.createMidCode(Opt.ARRAY_ADDR_INIT,name);
                int len = dim_1 * dim_2;
                for (int i = 0; i < len; i++) {
                    MidCodeFactory.createMidCode(Opt.STORE_ARRAY,
                            init.get(i),String.valueOf(i),name);
                }
            } else {
                MidCodeFactory.createMidCode(Opt.VAR_ARRAY,
                        name,
                        String.valueOf(dim_1),String.valueOf(dim_2));
                MidCodeFactory.createMidCode(Opt.ARRAY_ADDR_INIT,name);
            }
        }
    }
}
