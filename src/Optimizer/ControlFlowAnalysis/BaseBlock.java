package Optimizer.ControlFlowAnalysis;

import MidCode.MidCodeElement.*;
import Optimizer.RegisterAlloc.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import MidCode.Utility;


public class BaseBlock {

    // 当前基本块内副作用不能删除的变量 如果有函数调用 则所有的全局变量都认为有副作用
    public HashSet<String> sideEffectVars = new HashSet<>();

    // 基本块内的临时变量的使用定义分析
    // 主要服务于临时寄存器分配
    public ArrayList<MidCode> midCodes = new ArrayList<>();
    // 不定数量个直接前驱基本块
    private ArrayList<BaseBlock> prevs = new ArrayList<>();
    // 最多两个直接后继基本块
    private ArrayList<BaseBlock> nexts = new ArrayList<>();

    // 便于基本块的连接 记录这个基本块的专属标签
    public String name;
    private int dstNum;
    // 跳转到的基本块
    private String dst_1;
    private String dst_2;

    // 所有前驱
    private HashMap<String,BaseBlock> precursors = new HashMap<>();
    // 所有后继
    private HashMap<String,BaseBlock> successors = new HashMap<>();

    private boolean dstIsSet = false;

    //    活跃变量分析
    private HashSet<String> use = new HashSet<>();
    private HashSet<String> def = new HashSet<>();
    private HashSet<String> in = new HashSet<>();
    private HashSet<String> out = new HashSet<>();

    // 到达定义数据流分析
    private HashSet<MidCode> kill = new HashSet<>();
    private HashSet<MidCode> gen = new HashSet<>();
    private HashSet<MidCode> arrive_def_in = new HashSet<>();
    private HashSet<MidCode> arrive_def_out = new HashSet<>();

    // 所有的使用
    private HashSet<ABSTRACT_USE> allUses = new HashSet<>();

    public void setTailMidCode() {
        if (midCodes.size()-1 >= 0) {
            MidCode midCode = midCodes.get(midCodes.size()-1);
            midCode.isBlockTail = true;
        }
    }

    public void innerTempUseDefAnalyze() {
        // 基本块内的临时变量使用定义分析

        // temp临时变量一定是先定义再使用的
        // 先知道这个 基本块内的所有的临时变量
        HashSet<String> temps = new HashSet<>();
        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_DEF) {
                String def = ((ABSTRACT_DEF) midCode).getDef();

                if (Utils.isTempVar(def)) {
                    temps.add(def);
                }
            }
        }

        ArrayList<String> useTemp = new ArrayList<>();
        for (int i = midCodes.size()-1; i>=0; i--) {
            // 逆序遍历基本块中的四元式
            MidCode midCode = midCodes.get(i);
            if (midCode instanceof ABSTRACT_USE) {
                // 只看他后面使用的 不看他自己
                HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                for (String use:uses) {
                    if (Utils.isTempVar(use)) {
                        if (useTemp.contains(use)) {
                            useTemp.remove(use);
                        }
                        // 更靠前的使用会覆盖掉靠后的使用
                        useTemp.add(use);
                    }
                }
                // arrayList中越靠后的越新
            }
            midCode.useTemp.addAll(useTemp);
        }

        ArrayList<String> defTemp = new ArrayList<>();
        for (int i = 0; i < midCodes.size(); i++) {
            MidCode midCode = midCodes.get(i);

            if (midCode instanceof ABSTRACT_DEF) {
                String def = ((ABSTRACT_DEF) midCode).getDef();
                if (Utils.isTempVar(def)) {
                    if (defTemp.contains(def)) {
                        defTemp.remove(def);
                    }
                    // 更靠后的定义会覆盖掉靠前的定义(理论上temp临时变量只会被定义一次)
                    defTemp.add(def);
                }
            }
            midCode.defTemp.addAll(defTemp);
        }

        // 计算活跃临时变量
        for (MidCode midCode : midCodes) {
            // 活跃变量按照在后面使用的顺序排序
            // 外面是useTemp
            // 里面是defTemp
            for (String temp : midCode.useTemp) {
                if (midCode.defTemp.contains(temp)) {
                    midCode.activeTemp.add(temp);
                }
            }
        }

        // 计算 inactiveTemp
        for (MidCode midCode : midCodes) {
            // 优先释放不活跃的变量 如果还是不够 释放未来最久不使用的变量
            for (String temp : temps) {
                if (!midCode.activeTemp.contains(temp)) {
                    // 不活跃
                    //if (midCode.defTemp.contains(temp)) {
                        // 只能释放前面定义过的
                        // 还未定义的 是不活跃的 但是由于未出现过 也没法释放
                    //    midCode.inactiveTemp.add(temp);
                    //}
                    midCode.inactiveTemp.add(temp);
                }
            }
            //
            // 当前向后肯定还有使用的位置
//            for (String temp:midCode.activeTemp) {
//                if (!midCode.inactiveTemp.contains(temp)) {
//                    midCode.inactiveTemp.add(temp);
//                }
//            }
            // 当前midCode会用的不能释放
//            if (midCode instanceof ABSTRACT_DEF) {
//                String def = ((ABSTRACT_DEF) midCode).getDef();
//                // 必须要是临时变量
//                if (temps.contains(def) && midCode.inactiveTemp.contains(def)) {
//                    midCode.inactiveTemp.remove(def);
//                }
//            }
//            if (midCode instanceof ABSTRACT_USE) {
//                HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
//                for (String use:uses) {
//                    if (temps.contains(use) && midCode.inactiveTemp.contains(use)) {
//                        midCode.inactiveTemp.remove(use);
//                    }
//                }
//            }
        }
    }

    public String inactive2Str() {
        String retStr = "";
        for (MidCode midCode:midCodes) {
            retStr += midCode;
            retStr += midCode.inactiveTemp.toString() + "\r\n";
        }
        return retStr;
    }

    public String activeTemp2Str() {
        String retStr = "";
        for (MidCode midCode:midCodes) {
            retStr += midCode;
            retStr += midCode.activeTemp.toString() + "\r\n";
        }
        return retStr;
    }

    public void calculateAllUses() {
        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_USE) {
                allUses.add((ABSTRACT_USE) midCode);
            }
        }
    }

    public void genAnalyze(HashMap<String,HashSet<MidCode>>
                           allMidCodesDefVar) {
        HashSet<String> alreadyGen = new HashSet<>();
        for (int i = midCodes.size()-1;i>=0;i--) {
            MidCode midCode = midCodes.get(i);
            // 一个基本块内不是所有的def都是gen
            // 单独分析gen 一个基本块对一个变量只能gen一次 gen最后一次
            // 所以倒序
            if (midCode instanceof ABSTRACT_DEF) {
                String def = ((ABSTRACT_DEF) midCode).getDef();
                if (!alreadyGen.contains(def)) {
                    alreadyGen.add(def);
                    gen.add(midCode);
                }
            }
        }
        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_DEF) {
                String varName = ((ABSTRACT_DEF) midCode).getDef();

                if (allMidCodesDefVar.containsKey(varName)) {
                    allMidCodesDefVar.get(varName).add(midCode);
                }
                else {
                    HashSet<MidCode> tmp = new HashSet<>();
                    tmp.add(midCode);
                    allMidCodesDefVar.put(varName,tmp);
                }
            }
        }
    }

    public void useDefAnalyze() {
        // 无顺序
        HashSet<String> allUse = new HashSet<>(); // 凡是使用
        HashSet<String> allDef = new HashSet<>(); // 凡是定义
        // 可能会重复做 所以要清零
        use = new HashSet<>();
        def = new HashSet<>();
        in = new HashSet<>();
        out = new HashSet<>();
        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_USE && midCode instanceof ABSTRACT_DEF) {
                // 同时满足
                // 先use后def
                HashSet<String> vars = ((ABSTRACT_USE) midCode).getUse();
                for (String var:vars) {
                    allUse.add(var);
                    if (!allDef.contains(var)) {
                        use.add(var);
                    }
                }
                String var = ((ABSTRACT_DEF) midCode).getDef();
                allDef.add(var);
                if (!allUse.contains(var)) {
                    def.add(var);
                }
            }
            else {
                // 或只是use
                if (midCode instanceof ABSTRACT_USE) {
                    HashSet<String> vars = ((ABSTRACT_USE) midCode).getUse();
                    for (String var:vars) {
                        allUse.add(var);
                        if (!allDef.contains(var)) {
                            use.add(var);
                        }
                    }
                }
                // 或只是def
                else if (midCode instanceof ABSTRACT_DEF) {
                    String var = ((ABSTRACT_DEF) midCode).getDef();
                    allDef.add(var);
                    if (!allUse.contains(var)) {
                        def.add(var);
                    }
                }
                // 或都不是
                else {

                }
            }
        }
    }

    public String successors2Str() {
        String retStr = "****PRINT SUCCESSORS****\r\n";
        retStr += "BLOCKNOW: " + name + "\r\n";
        for (String nameKey : successors.keySet()) {
            retStr += String.format("\t%s\r\n",successors.get(nameKey).getName());
        }
        retStr += "******END******\r\n";
        return retStr;
    }

    public String precursors2Str() {
        String retStr = "****PRINT PRECURSORS****\r\n";
        retStr += "BLOCKNOW: " + name + "\r\n";
        for (String nameKey : precursors.keySet()) {
            retStr += String.format("\t%s\r\n",precursors.get(nameKey).getName());
        }
        retStr += "******END******\r\n";
        return retStr;
    }

    public String useDef2Str() {
        String retStr = "\r\n";
        retStr += "BLOCKNOW: " + name + "\r\n";
        retStr += "use: " + use.toString() + "\r\n";
        retStr += "def: " + def.toString() + "\r\n";
        retStr += midCodes.toString() + "\r\n";
        retStr += "\r\n";
        return retStr;
    }

    public String killGen2Str() {
        String retStr = "\r\n";
        retStr += "BLOCKNOW: " + name + "\r\n";
        retStr += "gen: " + gen.toString() + "\r\n";
        retStr += "kill: " + kill.toString() + "\r\n";
        retStr += midCodes.toString() + "\r\n";
        retStr += "\r\n";
        return retStr;
    }

    public String inOut2Str() {
        String retStr = "\r\n";
        retStr += "BLOCKNOW: " + name + "\r\n";
        retStr += "in: " + in.toString() + "\r\n";
        retStr += "out: " + out.toString() + "\r\n";
        retStr += midCodes.toString() + "\r\n";
        retStr += "\r\n";
        return retStr;
    }

    public String arriveDefInOut2Str() {
        String retStr = "\r\n";
        retStr += "BLOCKNOW: " + name + "\r\n";
        retStr += "arrive_def_in: " + arrive_def_in.toString() + "\r\n";
        retStr += "arrive_def_out: " + arrive_def_out.toString() + "\r\n";
        retStr += midCodes.toString() + "\r\n";
        retStr += "\r\n";
        return retStr;
    }

    public boolean dstIsSet() {
        return dstIsSet;
    }

    public void addMidCode(MidCode midCode) {
        this.midCodes.add(midCode);
    }

    public void setMidCodes(ArrayList<MidCode> midCodes) {
        this.midCodes = midCodes;
    }

    public void setPrevs(ArrayList<BaseBlock> prevs) {
        this.prevs = prevs;
    }

    public void setDstNum(int dstNum) {
        this.dstNum = dstNum;
        this.dstIsSet = true;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setDst_1(String dst_1) {
        this.dst_1 = dst_1;
    }

    public void setDst_2(String dst_2) {
        this.dst_2 = dst_2;
    }

    public ArrayList<MidCode> getMidCodes() {
        return midCodes;
    }

    public ArrayList<BaseBlock> getPrevs() {
        return prevs;
    }

    public int getDstNum() {
        return dstNum;
    }


    public String getName() {
        return name;
    }

    public String getDst_1() {
        return dst_1;
    }

    public String getDst_2() {
        return dst_2;
    }

    public ArrayList<BaseBlock> getNexts() {
        return nexts;
    }

    public void setNexts(ArrayList<BaseBlock> nexts) {
        this.nexts = nexts;
    }

    @Override
    public String toString() {
        String retStr = "";
        retStr += "\r\n";
        retStr += "##########BASE_BLOCK###########\r\n";

        retStr += String.format("ENTRANCE: %s\r\n", name);
        retStr += String.format("DST_1: %s\r\n",dst_1);
        retStr += String.format("DST_2: %s\r\n",dst_2);
        retStr += String.format("Number Of Precursor: %d\r\n",prevs.size());
        retStr += String.format("Number Of Successor: %d\r\n", nexts.size());

        for (MidCode midCode:midCodes) {
            retStr += "\t" + midCode.toString();
        }
        retStr += "########END_BASE_BLOCK#########\r\n";
        retStr += "\r\n";
        return retStr;
    }

    public HashMap<String, BaseBlock> getPrecursors() {
        return precursors;
    }

    public HashMap<String, BaseBlock> getSuccessors() {
        return successors;
    }

    public void setPrecursors(HashMap<String, BaseBlock> precursors) {
        this.precursors = precursors;
    }

    public void setSuccessors(HashMap<String, BaseBlock> successors) {
        this.successors = successors;
    }

    public HashSet<String> getUse() {
        return use;
    }

    public HashSet<String> getDef() {
        return def;
    }

    public HashSet<String> getIn() {
        return in;
    }

    public HashSet<String> getOut() {
        return out;
    }

    public void setUse(HashSet<String> use) {
        this.use = use;
    }

    public void setDef(HashSet<String> def) {
        this.def = def;
    }

    public void setIn(HashSet<String> in) {
        this.in = in;
    }

    public void setOut(HashSet<String> out) {
        this.out = out;
    }

    public HashSet<MidCode> getKill() {
        return kill;
    }

    public HashSet<MidCode> getGen() {
        return gen;
    }

    public HashSet<MidCode> getArrive_def_in() {
        return arrive_def_in;
    }

    public HashSet<MidCode> getArrive_def_out() {
        return arrive_def_out;
    }

    public void setKill(HashSet<MidCode> kill) {
        this.kill = kill;
    }

    public void setGen(HashSet<MidCode> gen) {
        this.gen = gen;
    }

    public void setArrive_def_in(HashSet<MidCode> arrive_def_in) {
        this.arrive_def_in = arrive_def_in;
    }

    public void setArrive_def_out(HashSet<MidCode> arrive_def_out) {
        this.arrive_def_out = arrive_def_out;
    }

    public HashSet<ABSTRACT_USE> getAllUses() {
        return allUses;
    }

    public void deadCodeDelete() {

        if (midCodes.size() == 0) {
            // 理论上每个基本块至少有一个标签语句 不会出现空基本块
            return;
        }

        HashSet<String> usedAfterDelete = new HashSet<>();
        // 倒序遍历中间代码
        for (int i = midCodes.size()-1; i>=0; i--) {
            // 只可能删除定义语句
            // 对全局变量的任何定义均不可删除


            MidCode midCode = midCodes.get(i);
            if (midCode instanceof ABSTRACT_DEF) {
                String def = ((ABSTRACT_DEF) midCode).getDef();
                if (!Utils.isGlobalVar(def)) {

                    // 是定义语句且未定义全局变量
                    // 如果句子被删掉 其use也应该置空
                    if (!out.contains(def) && !usedAfterDelete.contains(def)) {
                        midCode.isDelete = true;
                    }
                    else {
                        // 定义语句未被删掉 则其要kill
                        if (usedAfterDelete.contains(def)) {
                            usedAfterDelete.remove(def);
                        }
                    }
                }
            }
            if (midCode instanceof  ABSTRACT_USE) {
                usedAfterDelete.addAll(((ABSTRACT_USE) midCode).getUse());
            }
        }
    }

    public void addSideEffectVars(HashSet<String> vars) {
        // 添加上一层传递来的副作用
        sideEffectVars.addAll(vars);
    }

    public void calculateSideEffectVars() {
        // 副作用变量不可删除
        // 计算副作用变量集合
        // 要从下向上遍历
        // 用来定义副作用变量而使用的变量也是副作用变量
        for (MidCode midCode : midCodes) {
            if (midCode instanceof END_CALL_VOID || midCode instanceof END_CALL_INT) {
                // 有函数调用 全局变量均有副作用
                sideEffectVars.addAll(Flow.globalVars);
            }
            // 打印的变量
            if (midCode instanceof PRINT_INT) {
                String printInt = ((PRINT_INT) midCode).getDstInt();
                if (Utility.isVar(printInt)) {
                    sideEffectVars.add(printInt);
                }
            }
            // 函数传参的变量
            if (midCode instanceof PUSH) {
                String pushVar = ((PUSH) midCode).getName();
                if (Utility.isVar(pushVar)) {
                    sideEffectVars.add(pushVar);
                }
            }
        }
    }

    public void spreadSideEffectVars() {
        // 从后向前扫描
        // 保守 用来定义副作用变量的变量也是副作用变量
        for (int index = midCodes.size()-1;index>=0;index--) {
            MidCode midCode = midCodes.get(index);
            if (midCode instanceof ABSTRACT_DEF) {
                String def = ((ABSTRACT_DEF) midCode).getDef();
                if (sideEffectVars.contains(def)) {
                    if (midCode instanceof ABSTRACT_USE) {
                        HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                        sideEffectVars.addAll(uses);
                    }
                }
            }
        }
    }

    public void deleteBlock() {
        // 删除基本块无副作用的代码
        // 强寄存器分配 基本块内SSA
        // 副作用变量的定义要保留
        for (int index=0;index<midCodes.size();index++) {
            MidCode midCode = midCodes.get(index);

        }
    }
}
