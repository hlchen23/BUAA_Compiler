package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import Macro.Macro;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MyException.EOF;
import Error.*;
import SymTable.DataType;
import SymTable.StackTable;
import SymTable.TableItem;

import java.util.ArrayList;

public class UnaryExp extends Node {

    private Type type;

    private PrimaryExp primaryExp;

    private _Ident _ident;
    private FuncRParams funcRParams = new FuncRParams();

    private UnaryOp unaryOp;
    private UnaryExp unaryExp;

    private boolean hasFuncRParams = false;

    private enum Type {
        PRIMARYEXP,
        CALL,
        UNARYEXP
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (
                (Grammar.token.getTokenType() == TokenType.PLUS)
                || (Grammar.token.getTokenType() == TokenType.MINU)
                || (Grammar.token.getTokenType() == TokenType.NOT)
        ) {
            Grammar.retract();
            type = Type.UNARYEXP;
            unaryOp = new UnaryOp();
            unaryOp.analyze();
            unaryExp = new UnaryExp();
            unaryExp.analyze();
        }
        else if (
                (Grammar.token.getTokenType() == TokenType.LPARENT)
                || (Grammar.token.getTokenType() == TokenType.INTCON)
        ) {
            Grammar.retract();
            type = Type.PRIMARYEXP;
            primaryExp = new PrimaryExp();
            primaryExp.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.IDENFR) {
            Grammar.retract();
            _ident = new _Ident();
            _ident.analyze();
            Grammar.nextToken();
            if (Grammar.token.getTokenType() == TokenType.LPARENT) {
                type = Type.CALL; // 函数调用类型
                // 函数重命名
                _ident.getIdentToken().setRawString(Macro.FUN_PREFIX + _ident.getIdentToken().getRawString());
                Grammar.nextToken();
                // 要按照FuncRParams的FIRST集 即Exp的FIRST集
                if (
                        (Grammar.token.getTokenType() == TokenType.PLUS)
                        || (Grammar.token.getTokenType() == TokenType.MINU)
                        || (Grammar.token.getTokenType() == TokenType.NOT)
                        || (Grammar.token.getTokenType() == TokenType.IDENFR)
                        || (Grammar.token.getTokenType() == TokenType.LPARENT)
                        || (Grammar.token.getTokenType() == TokenType.INTCON)
                ) {
                    Grammar.retract();
                    hasFuncRParams = true;
                    funcRParams = new FuncRParams();
                    funcRParams.analyze();
                }
                else {
                    Grammar.retract();
                }
                Grammar.nextToken();
                if (Grammar.token.getTokenType() != TokenType.RPARENT) {
                    // 缺少) 异常
                    MyError.add_lack_rparent();
                }
            }
            else {
                Grammar.retract();
                Grammar.retract();
                type = Type.PRIMARYEXP;
                primaryExp = new PrimaryExp();
                primaryExp.analyze();
            }
        }
        OutputList.addToList(GrammarType.UnaryExp);
    }

    public void makeTable() {
        if (type == Type.PRIMARYEXP) {
            primaryExp.makeTable();
        }
        else if (type == Type.CALL) {
            // 检查是否未定义
            String name = _ident.getIdentToken().getRawString();
            TableItem func = StackTable.def(name,true);
            if (func == null) {
                MyError.add_unDef(_ident.getIdentToken());
            }
            if (hasFuncRParams) {
                funcRParams.makeTable();
            }
            if (func != null) {
                ArrayList<TableItem> params = func.getParas();
                // 检查数量
                // funcRParams可能没有 所以需要给funcRParams初始化一下
                if (params.size() != funcRParams.getParamsNum()) {
                    MyError.add_params_num_mismatch(_ident.getIdentToken(),params.size(),funcRParams.getParamsNum());
                }
                else {
                    // 数量相等的情况下检查类型匹配
                    for (int i = 0; i <params.size(); i++) {
                        DataType need = params.get(i).getDataType();
                        DataType give = funcRParams.getExps().get(i).getDataType();

                        if (!need.equals(give)) {
                            MyError.add_params_type_mismatch(_ident.getIdentToken(),need,give);
                        }
                    }
                }
            }
        }
        else if (type == Type.UNARYEXP) {
            unaryOp.makeTable();
            unaryExp.makeTable();
        }
    }

    public int eval() {
        // 常量表达式求值中使用的Ident必须是常量
        if (type == Type.PRIMARYEXP) {
            return primaryExp.eval();
        }
        else if (type == Type.UNARYEXP) {
            if (unaryOp.getOp() == UnaryOp.Op.POS) {
                return unaryExp.eval();
            }
            else if (unaryOp.getOp() == UnaryOp.Op.NEG) {
                return -1 * unaryExp.eval();
            }
            else {
                // NOT类型
                return (unaryExp.eval()==0)? 1: 0;
            }
        }
        else {
            // 常量表达式计算不能有函数调用 出错
            // 这种情况不会出现
            return 0;
        }
    }

    public DataType getDataType() {
        if (type == Type.PRIMARYEXP) {
            return primaryExp.getDataType();
        }
        else if (type == Type.CALL) {
            // 判断一下是不是void
            TableItem func = StackTable.def(_ident.getIdentToken().getRawString(),true);
            if (func != null) {
                return func.getDataType();
            }
            else {
                return DataType.INVALID_DATATYPE;
            }
        }
        else if (type == Type.UNARYEXP) {
            return unaryExp.getDataType();
        }
        else {
            return DataType.INVALID_DATATYPE;
        }
    }

    public String createMidCode() {
        if (type == Type.PRIMARYEXP) {
            return primaryExp.createMidCode();
        }
        else if (type == Type.UNARYEXP) {
            if (unaryOp.getOp() == UnaryOp.Op.POS) {
                return unaryExp.createMidCode();
            }
            else if (unaryOp.getOp() == UnaryOp.Op.NEG) {
                return MidCodeFactory.createMidCode(Opt.NEG,unaryExp.createMidCode());
            }
            else {
                // ! NOT
                return MidCodeFactory.createMidCode(Opt.NOT,unaryExp.createMidCode());
            }
        }
        else {
            // CALL_INT
            // CALL_VOID
            TableItem funItem = StackTable.def(_ident.getIdentToken().getRawString(),true);

            ArrayList<String> pushVars = new ArrayList<>();
            if (hasFuncRParams) {
                // push
                pushVars = funcRParams.createMidCode();
            }

            if (funItem.getDataType() == DataType.VOID) {
                MidCodeFactory.createMidCode(Opt.CALL_VOID,funItem.getIdentName());
            }
            else {
                // INT
                MidCodeFactory.createMidCode(Opt.CALL_INT,funItem.getIdentName());
            }
            for (String pushVar: pushVars) {
                MidCodeFactory.createMidCode(Opt.PUSH,pushVar);
            }
            if (funItem.getDataType() == DataType.VOID) {
                return MidCodeFactory.createMidCode(Opt.END_CALL_VOID,funItem.getIdentName());
            }
            else {
                // INT
                return MidCodeFactory.createMidCode(Opt.END_CALL_INT,funItem.getIdentName());
            }
        }
    }
}
