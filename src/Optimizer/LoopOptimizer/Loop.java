package Optimizer.LoopOptimizer;

import MidCode.MidCodeElement.MidCode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Loop {

    // 直接指向外层的循环

    // 循环开始语句
    public MidCode startLoop;
    // 循环结束语句
    public MidCode endLoop;
    // 外层循环语句
    public Loop prevLoop;
    // 其下面 直接 含有的循环
    public ArrayList<Loop> subLoops = new ArrayList<>();
    // 循环所含有的中间代码
    public HashSet<MidCode> haveMidCodes = new HashSet<>();

    public boolean isDelete = false;

    @Override
    public String toString() {
        String retStr = "";
        retStr += "\r\n";
        retStr += "========================\r\n";
        retStr += "start_loop:\r\n";
        retStr += startLoop;
        retStr += "end_loop:\r\n";
        retStr += endLoop;
        retStr += "prev_loop start:\r\n";
        if (prevLoop == null) {
            retStr += "已经是最外层循环了\r\n";
        }
        else {
            retStr += prevLoop.startLoop;
        }
        retStr += "=========================\r\n";
        retStr += "\r\n";
        return retStr;
    }

}
