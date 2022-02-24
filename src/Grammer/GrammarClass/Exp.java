package Grammer.GrammarClass;

import Grammer.GrammarType;
import Grammer.OutputList;
import MidCode.MidCodeFactory;
import MyException.EOF;
import SymTable.DataType;

public class Exp extends Node {
    private AddExp addExp;

    public void analyze() throws EOF {
        addExp = new AddExp();
        addExp.analyze();
        OutputList.addToList(GrammarType.Exp);
    }

    public void makeTable() {
        addExp.makeTable();
    }

    public int eval() {
        // 只用于常量表达式求值
        return addExp.eval();
    }

    public DataType getDataType() {
        return addExp.getDataType();
    }

    public String createMidCode() {
        return addExp.createMidCode();
    }
}
