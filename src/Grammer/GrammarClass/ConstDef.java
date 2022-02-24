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
import Optimizer.OptimizerSwitch;
import SymTable.DataType;
import SymTable.IdentType;
import SymTable.StackTable;
import SymTable.TableItem;

import java.util.ArrayList;

public class ConstDef extends Node {

    private Type type;
    private _Ident _ident;
    private ArrayList<ConstExp> constExps = new ArrayList<>(); // 常量数组的维度信息
    private ConstInitVal constInitVal;
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
                // 缺少 ] 报错
                MyError.add_lack_rbrack();
            }
            Grammar.nextToken();
        }
        if (Grammar.token.getTokenType() == TokenType.ASSIGN) {
            constInitVal = new ConstInitVal();
            constInitVal.analyze();
        }

        if (constExps.size() == 0) {
            type = Type.SINGLE;
        }
        else if (constExps.size() == 1) {
            type = Type.ARRAY_1;
        }
        else if (constExps.size() == 2) {
            type = Type.ARRAY_2;
        }
        OutputList.addToList(GrammarType.ConstDef);
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
                    tableItem.setIdentType(IdentType.CONST);
                    // 确定数组的类型 并确定维度信息
                    if (constExps.size() == 0) {
                        tableItem.setDataType(DataType.INT);
                        // 向符号表中存储常量的值
                        tableItem.setValue(constInitVal.eval());
                    }
                    else if (constExps.size() == 1) {
                        tableItem.setDataType(DataType.INT_ARRAY_1);
                        // 记录常量数组的初始值信息与维度信息
                        tableItem.setArrays(constInitVal.arrEval());
                        dim_1 = constExps.get(0).eval();
                        tableItem.setArray_dim_1(dim_1);
                    }
                    else if (constExps.size() == 2) {
                        tableItem.setDataType(DataType.INT_ARRAY_2);
                        // 记录常量数组的初始值信息与维度信息
                        tableItem.setArrays(constInitVal.arrEval());
                        dim_1 = constExps.get(0).eval();
                        dim_2 = constExps.get(1).eval();
                        tableItem.setArray_dim_1(dim_1);
                        tableItem.setArray_dim_2(dim_2);
                    }
                    tableItem.setDim(Grammar.dim);

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
            MidCodeFactory.createMidCode(Opt.CONST, name);
        }
        else if (type == Type.ARRAY_1) {
            // 常量数组一定有初始化 初始化之后一定数常数
            ArrayList<Integer> init = constInitVal.arrEval();
            MidCodeFactory.createMidCode(Opt.CONST_ARRAY,
                    name,
                    String.valueOf(dim_1));

            if (OptimizerSwitch.CONST_ARRAY_INTO_DATA) {
                MidCodeFactory.createMidCode(Opt.CONST_ARRAY_ADDR_INIT, name);
            }
            else {
                MidCodeFactory.createMidCode(Opt.ARRAY_ADDR_INIT, name);
                int len = dim_1;
                for (int i = 0; i < len; i++) {
                    MidCodeFactory.createMidCode(Opt.STORE_ARRAY,
                            String.valueOf(init.get(i)),String.valueOf(i),name);
                }
            }

        }
        else if (type == Type.ARRAY_2) {
            // 常量数组一定有初始化
            ArrayList<Integer> init = constInitVal.arrEval();

            MidCodeFactory.createMidCode(Opt.CONST_ARRAY,
                    name,
                    String.valueOf(dim_1),String.valueOf(dim_2));
            MidCodeFactory.createMidCode(Opt.ARRAY_ADDR_INIT, name);
            int len = dim_1 * dim_2;
            for (int i = 0; i < len; i++) {
                MidCodeFactory.createMidCode(Opt.STORE_ARRAY,
                        String.valueOf(init.get(i)),String.valueOf(i),name);
            }
        }
    }
}
