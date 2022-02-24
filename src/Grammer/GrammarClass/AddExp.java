package Grammer.GrammarClass;

import Grammer.Grammar;
import Grammer.GrammarType;
import Grammer.OutputList;
import Lexer.TokenType;
import MidCode.MidCodeFactory;
import MidCode.Opt;
import MidCode.Utility;
import MyException.EOF;
import Optimizer.OptimizerSwitch;
import SymTable.DataType;

import java.util.ArrayList;

public class AddExp extends Node {

    private ArrayList<Op> ops = new ArrayList<>();
    private ArrayList<MulExp> mulExps = new ArrayList<>();

    private enum Op {
        PLUS,
        MINU
    }

    public void analyze() throws EOF {
        MulExp mulExp = new MulExp();
        mulExps.add(mulExp);
        mulExp.analyze();
        OutputList.addToList(GrammarType.AddExp);
        Grammar.nextToken();
        while (
                (Grammar.token.getTokenType() == TokenType.PLUS)
                || (Grammar.token.getTokenType() == TokenType.MINU)
        ) {
            if (Grammar.token.getTokenType() == TokenType.PLUS) {
                ops.add(Op.PLUS);
            }
            else { ops.add(Op.MINU); }
            mulExp = new MulExp();
            mulExps.add(mulExp);
            mulExp.analyze();
            OutputList.addToList(GrammarType.AddExp);
            Grammar.nextToken();
        }
        Grammar.retract();
    }

    public void makeTable() {
        for (MulExp mulExp : mulExps) {
            mulExp.makeTable();
        }
    }

    public int eval() {
        // 至少有一个 且ops一定比mulExps长度大1
        int index = 0;
        MulExp mulExp = mulExps.get(index++);
        int val = mulExp.eval();
        for (Op op : ops) {
            if (op == Op.PLUS) {
                val += mulExps.get(index++).eval();
            }
            else { // MINU
                val -= mulExps.get(index++).eval();
            }
        }
        return val;
    }

    public DataType getDataType() {
        return mulExps.get(0).getDataType();
    }

    public String createMidCode() {
        // 要注意可能没有返回值
        // 如调用void函数
        if (OptimizerSwitch.ADD_SUB_MERGE_OPT) {
            // 加法交换律与加法结合律
            ArrayList<String> name = new ArrayList<>();
            ArrayList<String> var = new ArrayList<>();
            // 先从左到右计算乘法
            for (int index = 0; index < mulExps.size(); index++) {
                String temp = mulExps.get(index).createMidCode();
                name.add(temp);

                if (temp == null) {
                    // void类型函数调用 void无法参与加减法运算
                    return null;
                }

                if (Utility.isVar(temp)) {
                    var.add(temp);
                }
            }
            // 至少有一个MulExp
            int sum = 0;
            String name0 = name.get(0);
            if (!Utility.isVar(name0)) {
                sum += Integer.valueOf(name0);
            }
            // 把内部的常量计算出来
            for (int index = 0; index < ops.size(); index++) {
                String nameTemp = name.get(index+1);
                if (!Utility.isVar(nameTemp)) {
                    int num = Integer.valueOf(nameTemp);
                    Op op = ops.get(index);
                    if (op == Op.PLUS) {
                        sum += num;
                    }
                    else {
                        // MINU
                        sum -= num;
                    }
                }
            }
            String start = String.valueOf(sum);
            // 补上第一个
            String nameTemp = name.get(0);
            if (Utility.isVar(nameTemp)) {
                start = MidCodeFactory.createMidCode(Opt.ADD,start,nameTemp);
            }

            for (int index = 0; index < ops.size(); index++) {
                nameTemp = name.get(index+1);
                if (Utility.isVar(nameTemp)) {
                    Op op = ops.get(index);
                    if (op == Op.PLUS) {
                        start = MidCodeFactory.createMidCode(Opt.ADD,start,nameTemp);
                    }
                    else {
                        // MINU
                        start = MidCodeFactory.createMidCode(Opt.SUB,start,nameTemp);
                    }
                }
            }
            return start;
        }
        else {
            int index = 0;
            String tmp = mulExps.get(index++).createMidCode();
            for (Op op : ops) {
                if (op == Op.PLUS) {
                    tmp = MidCodeFactory.createMidCode(Opt.ADD,tmp,mulExps.get(index++).createMidCode());
                }
                else { // MINU
                    tmp = MidCodeFactory.createMidCode(Opt.SUB,tmp,mulExps.get(index++).createMidCode());
                }
            }
            return tmp;
        }
    }
}
