package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MyException.EOF;
import Error.*;
import SymTable.DataType;

public class PrimaryExp extends Node {

    private Type type;

    private Exp exp;
    private LVal lVal;
    private Number number;

    private enum Type {
        EXP,
        LVAL,
        NUMBER
    }

    public void analyze() throws EOF {
        Grammar.nextToken();
        if (Grammar.token.getTokenType() == TokenType.LPARENT) {
            type = Type.EXP;
            exp = new Exp();
            exp.analyze();
            Grammar.nextToken();
            if (Grammar.token.getTokenType() != TokenType.RPARENT) {
                // 缺少) 报错
                MyError.add_lack_rparent();
            }
        }
        else if (Grammar.token.getTokenType() == TokenType.IDENFR) {
            Grammar.retract();
            type = Type.LVAL;
            lVal = new LVal();
            lVal.analyze();
        }
        else if (Grammar.token.getTokenType() == TokenType.INTCON) {
            Grammar.retract();
            type = Type.NUMBER;
            number = new Number();
            number.analyze();
        }
        OutputList.addToList(GrammarType.PrimaryExp);
    }

    public void makeTable() {
        if (type == Type.EXP) {
            exp.makeTable();
        }
        else if (type == Type.LVAL) {
            lVal.makeTable();
        }
        else if (type == Type.NUMBER) {
            number.makeTable();
        }
    }

    public DataType getDataType() {
        if (type == Type.EXP) {
            return exp.getDataType();
        }
        else if (type == Type.LVAL) {
            return lVal.getDataType();
        }
        else if (type == Type.NUMBER) {
            return DataType.INT;
        }
        else {
            return DataType.INVALID_DATATYPE;
        }
    }

    public int eval() {
        if (type == Type.EXP) {
            return exp.eval();
        }
        else if (type == Type.NUMBER) {
            return number.eval();
        }
        else {
            // LVAL
            return lVal.eval();
        }
    }

    public String createMidCode() {
        if (type == Type.EXP) {
            return exp.createMidCode();
        }
        else if (type == Type.NUMBER) {
            return number.getNumStr();
        }
        else {
            // LVAL
            return lVal.createMidCode();
        }
    }
}
