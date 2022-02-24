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

public class LVal extends Node {

    private Token token;

    private Type type;
    private _Ident _ident;
    // 数组
    private ArrayList<Exp> exps = new ArrayList<>();

    public enum Type {
        SINGLE,
        ARRAY_1,
        ARRAY_2
    }

    public Type getType() {
        return type;
    }

    public _Ident get_ident() {
        return _ident;
    }

    public void analyze() throws EOF {
        _ident = new _Ident();
        _ident.analyze();
        token  = _ident.getIdentToken();
        type = Type.SINGLE;
        Grammar.nextToken();
        int array = 0;
        while (Grammar.token.getTokenType() == TokenType.LBRACK) {
            array++;
            Exp exp = new Exp();
            exps.add(exp);
            exp.analyze();
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.RBRACK) {
                // 缺少] 报错
                MyError.add_lack_rbrack();
            }
            Grammar.nextToken();
        }
        if (array == 1) {
            type = Type.ARRAY_1;
        }
        else if (array == 2) {
            type = Type.ARRAY_2;
        }
        Grammar.retract();
        OutputList.addToList(GrammarType.LVal);
    }

    public Token getToken() {
        return token;
    }

    public void makeTable() {
        String name = _ident.getIdentToken().getRawString();
        TableItem item = StackTable.def(name,false);
        if (item == null) {
            MyError.add_unDef(_ident.getIdentToken());
        }
        for (Exp exp:exps) {
            exp.makeTable();
        }
    }

    public DataType getDataType() {
        if (type == Type.SINGLE) {
            String name = _ident.getIdentToken().getRawString();
            TableItem item = StackTable.def(name,false);
            if (item!=null) {
                return item.getDataType();
            }
            else { return DataType.INVALID_DATATYPE; }
        }
        else if (type == Type.ARRAY_1) {
            String name = _ident.getIdentToken().getRawString();
            TableItem item = StackTable.def(name,false);
            if (item!=null) {
                if (item.getDataType() == DataType.INT_ARRAY_1) {
                    return DataType.INT;
                }
                else if (item.getDataType() == DataType.INT_ARRAY_2) {
                    return DataType.INT_ARRAY_1;
                }
                else { return DataType.INVALID_DATATYPE; }
            }
            else { return DataType.INVALID_DATATYPE; }
        }
        else if (type == Type.ARRAY_2) {
            String name = _ident.getIdentToken().getRawString();
            TableItem item = StackTable.def(name,false);
            if (item != null) {
                if (item.getDataType() == DataType.INT_ARRAY_2) {
                    return DataType.INT;
                }
                else {return DataType.INVALID_DATATYPE;}
            }
            else {return DataType.INVALID_DATATYPE;}
        }
        else {
            return DataType.INVALID_DATATYPE;
        }
    }

    public int eval() {
        // 左值求值必然是定义的常数
        // const int
        // const int[]
        // const int[][]
        if (type == Type.SINGLE) {
            // 查符号表得到单变量的值
            TableItem constItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (constItem != null && constItem.getIdentType() == IdentType.CONST) {
                return constItem.getValue();
            }
            else {
                // error
                // 不可能出现这种情况
                return 0;
            }
        }
        else if (type == Type.ARRAY_1) {
            TableItem constItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (constItem != null && constItem.getIdentType() == IdentType.CONST) {
                int val = exps.get(0).eval();
                return constItem.getArrayValue(val);
            }
            else {
                // error
                return 0;
            }
        }
        else {
            // ARRAY_2
            TableItem constItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (constItem != null && constItem.getIdentType() == IdentType.CONST) {
                int val_1 = exps.get(0).eval();
                int val_2 = exps.get(1).eval();
                return constItem.getArrayValue(val_1,val_2);
            }
            else {
                // error
                return 0;
            }
        }
    }

    public void writeBack(String tmp) {
        // 数组在左值时要回写
        if (type == Type.SINGLE) {
            // 只能是单变量 不用回写
        }
        else if (type == Type.ARRAY_1) {
            TableItem tableItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (tableItem != null) {
                if (tableItem.getDataType() == DataType.INT_ARRAY_1) {
                    String ind = exps.get(0).createMidCode();
                    MidCodeFactory.createMidCode(Opt.STORE_ARRAY,tmp,ind,tableItem.getRename());
                }
                else {}
            }
            else {}
        }
        else if (type == Type.ARRAY_2) {
            TableItem tableItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (tableItem != null) {
                if (tableItem.getDataType() == DataType.INT_ARRAY_2) {
                    String ind1 = exps.get(0).createMidCode();
                    String ind2 = exps.get(1).createMidCode();
                    int dim_2 = tableItem.getArray_dim_2();
                    String t = MidCodeFactory.createMidCode(Opt.MULT,ind1,String.valueOf(dim_2));
                    String ind = MidCodeFactory.createMidCode(Opt.ADD,t,ind2);
                    MidCodeFactory.createMidCode(Opt.STORE_ARRAY,tmp,ind,tableItem.getRename());
                }
                else {}
            }
            else {}
        }
    }

    public String createMidCode() {
        if (type == Type.SINGLE) {
            TableItem tableItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (tableItem != null) {
                if (tableItem.getIdentType() == IdentType.CONST) {
                    if (tableItem.getDataType() == DataType.INT) {
                        int value = tableItem.getValue();
                        return String.valueOf(value);
                    }
                    else if (tableItem.getDataType() == DataType.INT_ARRAY_1) {
                        // 取整个一维数组的地址 即指针即可
                        return tableItem.getRename();
                    }
                    else if (tableItem.getDataType() == DataType.INT_ARRAY_2) {
                        // 取整个二维数组的地址
                        return tableItem.getRename();
                    }
                    else {
                        // 不可能出现这种情况
                        return null;
                    }
                }
                else {
                    // PARA 或 VAR
                    if (tableItem.getDataType() == DataType.INT) {
                        return tableItem.getRename();
                    }
                    else if (tableItem.getDataType() == DataType.INT_ARRAY_1) {
                        // 取整个一维数组的地址
                        return tableItem.getRename();
                    }
                    else if (tableItem.getDataType() == DataType.INT_ARRAY_2) {
                        // 取整个二维数组的地址
                        return tableItem.getRename();
                    }
                    else {
                        // 不可能出现这种情况
                        return null;
                    }
                }
            }
            else {
                // 不可能出现这种情况
                return null;
            }
        }
        else if (type == Type.ARRAY_1) {
            // 应当至少是1维
            TableItem tableItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (tableItem != null) {
                if (tableItem.getDataType() == DataType.INT_ARRAY_1) {
                    // 取值
                    String tmp = exps.get(0).createMidCode();
                    return MidCodeFactory.createMidCode(Opt.LOAD_ARRAY,tableItem.getRename(),tmp);
                }
                else if (tableItem.getDataType() == DataType.INT_ARRAY_2) {
                    // 取子数组地址
                    String tmp = exps.get(0).createMidCode();
                    int dim_2 = tableItem.getArray_dim_2();
                    tmp = MidCodeFactory.createMidCode(Opt.MULT,tmp,String.valueOf(dim_2*4));
                    tmp = MidCodeFactory.createMidCode(Opt.ADD,tableItem.getRename(),tmp);
                    return tmp;
                }
                else {
                    // 不可能出现这种情况
                    return null;
                }
            }
            else {
                // 不可能出现这种情况
                return null;
            }
        }
        else if (type == Type.ARRAY_2) {
            // 应当至少是2维
            TableItem tableItem = StackTable.def(_ident.getIdentToken().getRawString(),false);
            if (tableItem != null) {
                if (tableItem.getDataType() == DataType.INT_ARRAY_2) {
                    String ind1 = exps.get(0).createMidCode();
                    String ind2 = exps.get(1).createMidCode();
                    int dim_2 = tableItem.getArray_dim_2();
                    String tmp = MidCodeFactory.createMidCode(Opt.MULT,ind1,String.valueOf(dim_2));
                    tmp = MidCodeFactory.createMidCode(Opt.ADD,tmp,ind2);
                    return MidCodeFactory.createMidCode(Opt.LOAD_ARRAY,tableItem.getRename(),tmp);
                }
                else {
                    // 不可能出现这种情况
                    return null;
                }
            }
            else {
                // 不可能出现这种情况
                return null;
            }
        }
        else {
            // 不可能出现这种情况
            return null;
        }
    }
}
