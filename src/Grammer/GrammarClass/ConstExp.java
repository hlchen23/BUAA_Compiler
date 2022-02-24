package Grammer.GrammarClass;

import Grammer.GrammarType;
import Grammer.OutputList;
import MyException.EOF;

public class ConstExp extends Node {
    // 可以求解出来进行简化
    private AddExp addExp; // 可计算

    public void analyze() throws EOF {
        addExp = new AddExp();
        addExp.analyze();
        OutputList.addToList(GrammarType.ConstExp);
    }

    public void makeTable() {
        addExp.makeTable();
    }

    public int eval() {
        // ConstExp求值方法
        return addExp.eval();
    }
}
