package Optimizer.ControlFlowAnalysis;

import MidCode.MidCodeElement.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import Macro.Macro;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.LoopOptimizer.Loop;
import Optimizer.RegisterAlloc.Utils;

import MidCode.Utility;
import MidCode.MidCodeFactory;

public class Flow {
    // 方法类
    // 创建流图
    // 接收中间代码序列


    public static HashSet<String> globalVars = new HashSet<>();
    // 全局的流图入口
    private static BaseBlock entrance;

    // 每个函数对应一个子流图
    private static HashMap<String, SubFlow> func2SubFlow = new HashMap<>();

    private static ArrayList<MidCode> midCodes;

    public static HashMap<String,ArrayList<String>> renames = new HashMap<>();

    // 按照中间代码顺序记录函数流图
    public static ArrayList<SubFlow> subFlowArrayList = new ArrayList<>();

    // 把中间代码切成块 入口点 出口点
    // 把块连接起来 出口点 与 入口点 的匹配标识
    public static void createFlow(ArrayList<MidCode> midCodes) {
        // 流图分析可能被多次调用 需要初始化
        func2SubFlow = new HashMap<>();
        renames = new HashMap<>();
        subFlowArrayList = new ArrayList<>();
        globalVars = new HashSet<>();

        // 保存全局的所有的中间代码
        Flow.midCodes = midCodes;
        // 求全局变量
        calculateGlobalVars();
        // 将中间代码按照全局与函数分解 并为每个函数分配一个subFlow
        breakDown(midCodes);
        // 切割基本块
        cutDown();
        // 连接基本块
        link();
        // 指定整个程序的流图入口: main函数入口
        entrance = func2SubFlow.get(Macro.MAIN).getEntrance();
    }

    public static void calculateGlobalVars() {
        for (MidCode midCode : midCodes) {
            if (midCode.getBelong().equals(Macro.GLOBAL_MARK)) {
                if (midCode instanceof ABSTRACT_DECLARE) {
                    globalVars.add(((ABSTRACT_DECLARE) midCode).getDeclareName());
                }
            }
        }
    }

    public static void innerTempUseDefAnalyze() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).innerTempUseDefAnalyze();
        }
    }

    public static String inactive2Str() {
        String retStr = "";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).inactive2Str();
        }
        return retStr;
    }

    public static String activeTemp2Str() {
        String retStr = "";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).activeTemp2Str();
        }
        return retStr;
    }

    public static void killGenAnalyze() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).killGenAnalyze();
        }
    }

    public static void calculateAllUses() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).calculateAllUses();
        }
    }

    public static void useDefAnalyze() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).useDefAnalyze();
        }
    }

    public static void activeAnalyze() {
        // 活跃变量分析
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).activeAnalyze();
        }
    }

    public static void arriveDefAnalyze() {
        // 到达定义分析
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).arriveDefAnalyze();
        }
    }

    public static void calculateSuccessors() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).calculateSuccessors();
        }
    }

    public static void calculatePrecursors() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).calculatePrecursors();
        }
    }

    public static void defUseChainAnalyze() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).defUseChainAnalyze();
        }
    }

    public static void defUseWebAnalyze() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).defUseWebAnalyze();
        }
    }

    public static void webRename() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).webRename();
        }
        // 对变量声明的中间代码进行补充
        // 全局变量的声明不可改变
        // var var_array const_array

        // 储存额外的形参声明
        HashMap<String,ABSTRACT_DECLARE> temp = new HashMap<>();

        for (int i=0; i < midCodes.size(); i++) {
            MidCode midCode = midCodes.get(i);
            if (midCode.getBelong().equals(Macro.GLOBAL_MARK)) {
                // 全局不重命名
                continue;
            }
            if (midCode instanceof ABSTRACT_DECLARE) {
                // 不会有问题 命名完之后的再扫描时 不会与renames中原名冲掉
                String name = ((ABSTRACT_DECLARE) midCode).getDeclareName();
                if (renames.containsKey(name)) {
                    ArrayList<String> replaces = renames.get(name);
                    if (replaces.size() == 1) {
                        ((ABSTRACT_DECLARE) midCode).rename(replaces.get(0));
                        // 对单变量补一个原始
                        if (midCode instanceof VAR) {
                            ABSTRACT_DECLARE newClone = ((ABSTRACT_DECLARE) midCode).myClone();
                            newClone.rename(name);
                            midCodes.add(i, (MidCode) newClone);
                            // 不会死循环
                        }

                    } else {
                        if (midCode instanceof PARA_DECLARE || midCode instanceof PARA_ARRAY_DECLARE) {
                            // 第一个重命名
                            midCodes.remove(i--);
                            // 后面的放在所有的形参定义之后
                            for (int j = 0; j < replaces.size(); j++) {
                                // 添加新的声明语句
                                ABSTRACT_DECLARE newClone = ((ABSTRACT_DECLARE) midCode).myClone();
                                newClone.rename(replaces.get(j));
                                temp.put(replaces.get(j),newClone);
                            }
                        }
                        else {
                            // 第一个重命名
                            ((ABSTRACT_DECLARE) midCode).rename(replaces.get(0));
                            // 后面补充几个新的声明语句
                            for (int j = 1; j < replaces.size(); j++) {
                                // 添加新的声明语句
                                ABSTRACT_DECLARE newClone = ((ABSTRACT_DECLARE) midCode).myClone();
                                newClone.rename(replaces.get(j));
                                midCodes.add(i, (MidCode) newClone);
                            }
                            // 对单变量补一个原始
                            if (midCode instanceof VAR) {
                                ABSTRACT_DECLARE newClone = ((ABSTRACT_DECLARE) midCode).myClone();
                                newClone.rename(name);
                                midCodes.add(i, (MidCode) newClone);
                                // 不会死循环
                            }
                        }
                    }
                }
            }
        }

        // 再扫描一遍 没有声明的变量 要补充声明
        HashSet<String> declared = new HashSet<>();
        for (int i=0; i < midCodes.size(); i++) {
            MidCode midCode = midCodes.get(i);
            if (midCode instanceof ABSTRACT_DEF) {
                String def = ((ABSTRACT_DEF) midCode).getDef();
                if (!declared.contains(def)) {
                    midCodes.add(i,(MidCode) temp.get(def));
                    declared.add(def);
                }
            }
            if (midCode instanceof ABSTRACT_DECLARE) {
                String declare = ((ABSTRACT_DECLARE) midCode).getDeclareName();
                declared.add(declare);
            }
        }
        // System.out.println(midCodes);
    }

    public static String useDef2Str() {
        String retStr = "";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).useDef2Str();
        }
        return retStr;
    }

    public static String killGen2Str() {
        String retStr = "";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).killGen2Str();
        }
        return retStr;
    }

    public static String inOut2Str() {
        String retStr = "";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).inOut2Str();
        }
        return retStr;
    }

    public static String arriveDefInOut2Str() {
        String retStr = "";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).arriveDefInOut2Str();
        }
        return retStr;
    }

    public static String successors2Str() {
        String retStr = "===SUCCESSORS ANALYSIS===\r\n";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).successors2Str();
        }
        retStr += "======END=======\r\n";
        return retStr;
    }

    public static String precursors2Str() {
        String retStr = "===PRECURSORS ANALYSIS===\r\n";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).precursors2Str();
        }
        retStr += "======END=======\r\n";
        return retStr;
    }

    public static String chain2String() {
        String retStr = "";
        for (String funcKey:func2SubFlow.keySet()) {
            retStr += "CHAIN-----------subFlow: " + funcKey + "\r\n";
            retStr += func2SubFlow.get(funcKey).chain2String();
        }
        return retStr;
    }

    public static String web2String() {
        String retStr = "";
        for (String funcKey:func2SubFlow.keySet()) {
            retStr += "WEB-------------subFlow: " + funcKey + "\r\n";
            retStr += func2SubFlow.get(funcKey).web2String();
        }
        return retStr;
    }

    private static void breakDown(ArrayList<MidCode> midCodes) {
        for (MidCode midCode : midCodes) {
            String belong = midCode.getBelong();
            // 为函数分配子流图
            if (!func2SubFlow.containsKey(belong)) {
                SubFlow subFlow = new SubFlow();

                subFlowArrayList.add(subFlow);

                subFlow.setFuncName(belong);
                func2SubFlow.put(belong,subFlow);
            }
            func2SubFlow.get(belong).addMidCode(midCode);
            // 将子流图的函数类型信息记录下来
            if (midCode instanceof FUN) {
                if (((FUN) midCode).type == FUN.Type.VOID) {
                    func2SubFlow.get(((FUN) midCode).getFunName()).type = SubFlow.Type.VOID;
                }
                else if (((FUN) midCode).type == FUN.Type.INT) {
                    func2SubFlow.get(((FUN) midCode).getFunName()).type = SubFlow.Type.INT;
                }
                func2SubFlow.get(((FUN) midCode).getFunName()).tailNo = ((FUN) midCode).tailNo;
            }
        }
    }

    private static void cutDown() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).cutDown();
        }
    }

    private static void link() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).link();
        }
    }

    public static String flowChart2Str() {
        String retStr = "";
        for (String funcKey : func2SubFlow.keySet()) {
            retStr += func2SubFlow.get(funcKey).flowChart2Str();
        }
        return retStr;
    }

    public static void conflictAnalyze() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).conflictAnalyze();
        }
    }

    public static void initRegAllocPool() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).initRegAllocPool();
        }
    }

    public static Register applyReadReg(String name, MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            return func2SubFlow.get(belong).applyReadReg(name,midCode);
        }
        return null;
    }

    public static Register applyWriteReg(String name,MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            return func2SubFlow.get(belong).applyWriteReg(name,midCode);
        }
        return null;
    }

    public static void save(MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            func2SubFlow.get(belong).save(midCode);
        }
    }

    public static void restore(MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            func2SubFlow.get(belong).restore(midCode);
        }
    }

    public static Register borrowReg(MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            return func2SubFlow.get(belong).borrowReg(midCode);
        }
        return null;
    }

    public static void returnReg(Register reg ,MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            func2SubFlow.get(belong).returnReg(reg,midCode);
        }
    }


    public static void clearBorrow(MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            func2SubFlow.get(belong).clearBorrow();
        }
    }

    public static void saveBlockTail(MidCode midCode) {
        String belong = midCode.getBelong();
        if (func2SubFlow.containsKey(belong)) {
            func2SubFlow.get(belong).saveBlockTail(midCode);
        }
    }

    public static void adjustBaseblocks() {
        // 调整基本块

        // 第一个subFlow一定是&global
        SubFlow globalFlow = subFlowArrayList.get(0);
        for (int i=0; i< globalFlow.baseBlockArrayList.size();i++) {
            BaseBlock baseBlock = globalFlow.baseBlockArrayList.get(i);
            if (baseBlock.name.equals(Macro.GLOBAL_MARK)) {
                // 清除里面的函数声明语句
                for (int j=0; j<baseBlock.midCodes.size(); j++) {
                    MidCode midCode = baseBlock.midCodes.get(j);
                    if (midCode instanceof FUN) {
                        baseBlock.midCodes.remove(j--);
                    }
                }
            }
        }
        // 每个函数的入口基本块补充函数的声明
        for (SubFlow subFlow : subFlowArrayList) {
            if (!subFlow.funcName.equals(Macro.GLOBAL_MARK)) {
                // 第一个基本块一定是函数的入口 且一定至少有
                BaseBlock baseBlockEntrance = subFlow.baseBlockArrayList.get(0);
                MidCode midCode = new FUN(subFlow.funcName);
                midCode.setInBaseBlock(baseBlockEntrance);
                baseBlockEntrance.midCodes.add(0,midCode);
            }
        }
    }

    public static void copySpread() {

        // 弱寄存器分配下 需要考虑kill的问题 遇到新的定义会kill掉
        // 强寄存器分配 每个基本块内SSA 不需要考虑kill的问题

        // 对每个基本块内进行复制传播
        for (SubFlow subFlow : subFlowArrayList) {
            for (BaseBlock baseBlock : subFlow.baseBlockArrayList) {
                ArrayList<MidCode> midCodes = baseBlock.midCodes;

                // 先扫描一遍 把可以变成赋值语句的语句找到 常量合并
                for (int i = 0; i < midCodes.size(); i++) {
                    MidCode midCode = midCodes.get(i);
                    if (midCode instanceof ABSTRACT_USE) {
                        if (((ABSTRACT_USE) midCode).canConvert2Assign()) {
                            String rightValue = ((ABSTRACT_USE) midCode).convert2Assign();
                            String leftValue = ((ABSTRACT_USE) midCode).getCopySpreadLeftValue();
                            midCodes.remove(i);

                            MidCode midCodeNew = new ASSIGN(rightValue,leftValue);
                            // 将新添加的中间代码
                            // 确保与原来的中间代码有相同的上下文语境
                            midCodeNew.setInBaseBlock(midCode.getInBaseBlock());
                            midCodeNew.setBelong(midCode.getBelong());
                            midCodeNew.isBlockTail = midCode.isBlockTail;

                            midCodes.add(i,midCodeNew);
                        }
                    }
                }
                // 复制传播
                // 全局变量如果跨越了函数则应该停止
                for (int i = 0; i < midCodes.size(); i++) {
                    MidCode midCode = midCodes.get(i);
                    if (midCode instanceof ASSIGN) {
                        String opt = ((ASSIGN) midCode).getOpt();
                        String dst = ((ASSIGN) midCode).getDst();
                        for (int j = i + 1; j < midCodes.size(); j++) {
                            MidCode midCodeJ = midCodes.get(j);
                            if (midCodeJ instanceof ABSTRACT_USE) {
                                ((ABSTRACT_USE) midCodeJ).copySpreadRename(dst, opt);
                                if (((ABSTRACT_USE) midCodeJ).canConvert2Assign()) {
                                    String rightValue = ((ABSTRACT_USE) midCodeJ).convert2Assign();
                                    String leftValue = ((ABSTRACT_USE) midCodeJ).getCopySpreadLeftValue();
                                    midCodes.remove(j);
                                    MidCode midCodeNew = new ASSIGN(rightValue, leftValue);

                                    midCodeNew.setInBaseBlock(midCodeJ.getInBaseBlock());
                                    midCodeNew.setBelong(midCodeJ.getBelong());
                                    midCodeNew.isBlockTail = midCodeJ.isBlockTail;

                                    midCodes.add(j, midCodeNew);
                                }
                            }
                            if (midCodeJ instanceof ABSTRACT_DEF) {
                                String def = ((ABSTRACT_DEF) midCodeJ).getDef();
                                if (def.equals(dst)) {
                                    // kill掉了 停止传播
                                    break;
                                }
                            }
                            // 如果遇到了函数调用 则全局变量的传播应当停止
                            if (midCodeJ instanceof END_CALL_INT || midCodeJ instanceof END_CALL_VOID) {
                                if (Utils.isGlobalVar(dst)) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void updateMidCodes() {
        // 利用调整完的 和 复制传播后的 结果 更新程序的中间代码
        midCodes.clear();
        for (SubFlow subFlow : subFlowArrayList) {
            for (BaseBlock baseBlock : subFlow.baseBlockArrayList) {
                midCodes.addAll(baseBlock.midCodes);
            }
        }
    }

    public static void deadCodeDelete() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).deadCodeDelete();
        }
    }

    public static void setTailMidCode() {
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).setTailMidCode();
        }
    }

    public static void globalConstSpread() {
        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_USE) {
                // 全局变量除外
                for (String var : midCode.arriveDefs.keySet()) {
                    if (Utils.isGlobalVar(var)) {
                        continue;
                    }
                    if (midCode.arriveDefs.get(var).size() == 1) {
                        // 对var变量的使用只有一个定义
                        ABSTRACT_DEF defMidCode = midCode.arriveDefs.get(var).get(0);
                        if (defMidCode instanceof ASSIGN) {
                            String opt = ((ASSIGN) defMidCode).getOpt();
                            if (!Utility.isVar(opt)) {
                                // 使用常数定义的
                                // 常数传播
                                ((ABSTRACT_USE) midCode).copySpreadRename(var,opt);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void deleteMainTail() {
        // 删除主函数出口处不影响副作用的基本块
        SubFlow subFlow = func2SubFlow.get("main");
        subFlow.deleteMainTail();
    }

    private static HashSet<MidCode> conflictsMidCode = new HashSet<>();

    public static void clearSideEffect() {
        conflictsMidCode.clear();
    }


    public static HashSet<MidCode> newConflictsMidCode = new HashSet<>();

    public static void calNewConflictMidCode() {
        for (MidCode midCode : midCodes) {
            if (isSideEffectNew(midCode)) {
                newConflictsMidCode.add(midCode);
            }
        }
        boolean isVary = true;
        // 上次添加的
        HashSet<MidCode> newAdd = new HashSet<>();
        HashSet<MidCode> temp = new HashSet<>();

        newAdd.addAll(newConflictsMidCode);
        // 初始化
        while (isVary) {
            temp.clear();
            for (MidCode midCode:newAdd) {
                if (midCode instanceof ABSTRACT_USE) {

                    for (String key:midCode.arriveDefs.keySet()) {
                        ArrayList<ABSTRACT_DEF> defs = midCode.arriveDefs.get(key);
                        for (ABSTRACT_DEF def : defs) {
                            temp.add((MidCode) def);
                        }
                    }
                }
            }
            int oldLen = newConflictsMidCode.size();
            newConflictsMidCode.addAll(temp);
            int newLen = newConflictsMidCode.size();
            if (newLen == oldLen) {
                isVary = false;
            }
            else {
                isVary = true;
            }
            newAdd.addAll(temp);
        }
    }

    public static void NoSideEffectDeleteOpt() {
        // 对无副作用使用的变量的赋值可以删除
        // 对副作用通过def-use关系进行传播 迭代直至收敛
        for (MidCode midCode : midCodes) {
            if (isSideEffect(midCode)) {
                conflictsMidCode.add(midCode);
            }
        }
        boolean isVary = true;
        // 上次添加的
        HashSet<MidCode> newAdd = new HashSet<>();
        HashSet<MidCode> temp = new HashSet<>();

        newAdd.addAll(conflictsMidCode);
        // 初始化
        while (isVary) {
            temp.clear();
            for (MidCode midCode:newAdd) {
                if (midCode instanceof ABSTRACT_USE) {

                    for (String key:midCode.arriveDefs.keySet()) {
                        ArrayList<ABSTRACT_DEF> defs = midCode.arriveDefs.get(key);
                        for (ABSTRACT_DEF def : defs) {
                            temp.add((MidCode) def);
                        }
                    }
                }
            }
            int oldLen = conflictsMidCode.size();
            conflictsMidCode.addAll(temp);
            int newLen = conflictsMidCode.size();
            if (newLen == oldLen) {
                isVary = false;
            }
            else {
                isVary = true;
            }
            newAdd.addAll(temp);
        }


        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_DEF) {
                // 数组定义不能删
                // 形参数组定义不能删
                // 会导致mips异常

                if (!conflictsMidCode.contains(midCode)) {
                    midCode.isDelete = true;
                }
            }
        }
    }

    private static boolean isSideEffectNew(MidCode midCode) {
        // 是否有副作用
        if (midCode instanceof ABSTRACT_DEF) {
            String def = ((ABSTRACT_DEF) midCode).getDef();
            if (Utils.isGlobalVar(def)) {
                return true;
            }
        }
        if (midCode instanceof STORE_ARRAY) {
            return true;
        }
        if (midCode instanceof PRINT_INT) {
            return true;
        }
        if (midCode instanceof PRINT_STR) {
            return true;
        }
        if (midCode instanceof GETINT) {
            return true;
        }
        if (midCode instanceof CALL_INT) {
            return true;
        }
        if (midCode instanceof CALL_VOID) {
            return true;
        }
        if (midCode instanceof PUSH) {
            return true;
        }
        if (midCode instanceof END_CALL_INT) {
            return true;
        }
        if (midCode instanceof END_CALL_VOID) {
            return true;
        }
        // 此处不计算循环有关的内容
        if (midCode instanceof RETURN) {
            return true;
        }
        return false;
    }

    private static boolean isSideEffect(MidCode midCode) {
        // 判定一个中间代码是否是副作用代码
        // 控制流语句
        // print_int
        // getint
        // call
        // end_call
        // push
        // 对全局变量的赋值
        // 返回值语句
        // 对数组的赋值不能删
        if (midCode instanceof ABSTRACT_DEF) {
            String def = ((ABSTRACT_DEF) midCode).getDef();
            if (Utils.isGlobalVar(def)) {
                return true;
            }
        }
        if (midCode instanceof STORE_ARRAY) {
            // 保守 认为对全局或者局部变量的数组赋值均是有副作用的
            return true;
        }
        if (midCode instanceof PRINT_INT) {
            return true;
        }
        if (midCode instanceof PRINT_STR) {
            return true;
        }
        if (midCode instanceof GETINT) {
            return true;
        }
        if (midCode instanceof CALL_INT) {
            return true;
        }
        if (midCode instanceof CALL_VOID) {
            return true;
        }
        if (midCode instanceof PUSH) {
            return true;
        }
        if (midCode instanceof END_CALL_INT) {
            return true;
        }
        if (midCode instanceof END_CALL_VOID) {
            return true;
        }
        // return 已经含在JUMP接口下了
        if (midCode instanceof ABSTRACT_JUMP) {
            return true;
        }
        return false;
    }

    // 如果全局变量只在一个函数中使用 则可以按照一个局部变量来处理
    // 对全局变量单独做一个到达定义链条
    public static void global2local() {

        // 在到达定义分析之前进行这个工作

        // 只考虑全局非数组变量
        // 如果全局变量的"赋值和使用"main 在函数内部创建一个变量 用内部新变量代替 全局变量
        // 函数入口形参的后面 补一条新变量声明 然后将创建赋值语句 将全局变量赋值给局部变量
        // 全局变量如果没有赋值语句 则默认函数入口赋一个0值

        HashMap<String,HashSet<String>> var2Func = new HashMap<>();
        // 可以转化成main函数局部变量的函数
        HashSet<String> onlyInMain = new HashSet<>();

        for (MidCode midCode : midCodes) {
            if (midCode.getBelong().equals(Macro.GLOBAL_MARK)) {
                if (midCode instanceof VAR) {
                    // 全局 的 单变量 声明
                    String globalVarName = ((VAR) midCode).getDeclareName();
                    if (!var2Func.containsKey(globalVarName)) {
                        var2Func.put(globalVarName,new HashSet<>());
                    }
                }
            }
        }

        for (MidCode midCode : midCodes) {
            if (!midCode.getBelong().equals(Macro.GLOBAL_MARK)) {
                if (midCode instanceof ABSTRACT_DEF) {
                    String def = ((ABSTRACT_DEF) midCode).getDef();
                    if (var2Func.containsKey(def)) {
                        var2Func.get(def).add(midCode.getBelong());
                    }
                }
                if (midCode instanceof ABSTRACT_USE) {
                    HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                    for (String use : uses) {
                        if (var2Func.containsKey(use)) {
                            var2Func.get(use).add(midCode.getBelong());
                        }
                    }
                }
            }
        }

        for (String var : var2Func.keySet()) {
            if ((var2Func.get(var).size() == 1) && (var2Func.get(var).contains("main"))) {
                onlyInMain.add(var);
            }
        }

        // 需要新补充的变量
        ArrayList<VAR> newVars = new ArrayList<>();
        // 重命名映射表
        HashMap<String,String> renameMap = new HashMap<>();
        HashMap<String,String> reverseRenameMap = new HashMap<>();

        for (String var : onlyInMain) {
            String newName = MidCodeFactory.createAutoVar();
            VAR newVar = new VAR(newName);
            newVars.add(newVar);
            renameMap.put(var,newName);
            reverseRenameMap.put(newName,var);
        }

        for (int index = 0; index < midCodes.size(); index++) {
            MidCode midCode = midCodes.get(index);
            if (midCode.getBelong().equals("main")) {
                if (midCode instanceof ABSTRACT_USE) {
                    HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                    for (String use:uses) {
                        if (renameMap.containsKey(use)) {
                            ((ABSTRACT_USE) midCode).copySpreadRename(use,renameMap.get(use));
                        }
                    }
                }
                if (midCode instanceof ABSTRACT_DEF) {
                    String def = ((ABSTRACT_DEF) midCode).getDef();
                    if (renameMap.containsKey(def)) {
                        ((ABSTRACT_DEF) midCode).newRename(def,renameMap.get(def));
                    }
                }
            }
        }

        for (int index = 0; index < midCodes.size(); index++) {
            if (index >= 1) {
                MidCode midCode = midCodes.get(index-1);
                if (midCode instanceof FUN) {
                    if (((FUN) midCode).getFunName().equals("main")) {
                        for (VAR newVar : newVars) {
                            // 补充ASSIGN语句
                            String newName = newVar.getDeclareName();
                            ASSIGN assign = new ASSIGN(reverseRenameMap.get(newName),newName);

                            assign.setBelong("main");
                            assign.setInBaseBlock(func2SubFlow.get("main").baseBlocks.get("main"));
                            assign.isDelete = false;
                            midCodes.add(index,assign);

                            newVar.setBelong("main");
                            newVar.setInBaseBlock(func2SubFlow.get("main").baseBlocks.get("main"));
                            newVar.isDelete = false;

                            midCodes.add(index,newVar);
                        }
                    }
                }
            }
        }
    }

    public static void loopOptimize() {


        calNewConflictMidCode();

        // 构造du链
        // 存储到中间代码
        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_USE) {
                HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                for (String use : uses) {
                    if (!Utils.isGlobalVar(use)) {
                        // 不是全局变量时构造链条
                        ArrayList<ABSTRACT_DEF> defs = midCode.arriveDefs.get(use);

                        if (defs != null) {
                            for (ABSTRACT_DEF def : defs) {
                                ((MidCode) def).arriveUses.add((ABSTRACT_USE) midCode);
                            }
                        }
                    }
                }
            }
        }

        // 所有的循环
        ArrayList<Loop> allLoops = new ArrayList<>();
        // 通过循环的label的hashMap 增加查找速度
        HashMap<String,Loop> loopHash = new HashMap<>();
        // 栈
        ArrayList<Loop> loops = new ArrayList<>();

        HEAD head = new HEAD();
        TAIL tail = new TAIL();
        // 使用链表组织中间代码
        for (int i=0;i<midCodes.size();i++) {
            MidCode midCode = midCodes.get(i);
            if ((i+1) <= midCodes.size()-1) {
                midCode.nextMidCode = midCodes.get(i + 1);
            }
            if ((i-1) >= 0) {
                midCode.prevMidCode = midCodes.get(i - 1);
            }
        }
        // 设置链表头和链表尾
        midCodes.get(0).prevMidCode = head;
        head.nextMidCode = midCodes.get(0);
        midCodes.get(midCodes.size()-1).nextMidCode = tail;
        tail.prevMidCode = midCodes.get(midCodes.size()-1);

        // 记录循环 用一个栈
        for (MidCode midCode : midCodes) {
            if (midCode instanceof BEGIN_WHILE) {
                Loop loop = new Loop();
                loop.startLoop = midCode;
                loop.haveMidCodes.add(midCode);
                loops.add(loop);
            }
            else if (midCode instanceof END_WHILE) {
                Loop removeLoop = loops.remove(loops.size()-1);
                if (loops.size()-1>=0) {
                    removeLoop.prevLoop = loops.get(loops.size() - 1);
                    loops.get(loops.size() - 1).subLoops.add(removeLoop);
                }
                else {
                    removeLoop.prevLoop = null; // 表示最外层的循环
                }
                removeLoop.endLoop = midCode;
                removeLoop.haveMidCodes.add(midCode);
                allLoops.add(removeLoop);
                loopHash.put(((BEGIN_WHILE)removeLoop.startLoop).label,removeLoop);
            }
            else {
                // 其余的中间代码
                if (loops.size() > 0) {
                    // 属于栈顶的循环
                    loops.get(loops.size()-1).haveMidCodes.add(midCode);
                }
            }
        }

        boolean isVary = true;
        while (isVary) {
            isVary = false;
            for (Loop loop:allLoops) {
                if (loop.isDelete) {
                    continue;
                }
                // 当一个循环所有的子循环都被删除后 才能删除本层循环
                if (loop.subLoops.size() == 0) {
                    // 没有子循环
                    HashSet<MidCode> midCodes1 = new HashSet<>();
                    midCodes1.addAll(loop.haveMidCodes);

                    midCodes1.retainAll(newConflictsMidCode);

                    // 交集中可能存在跳转语句 如果是本loop内的跳转语句 则除了return 全部都不会跳出这个loop
//                    HashSet<MidCode> toBeDelete = new HashSet<>();
//                    for (MidCode midCode : midCodes1) {
//                        // 遍历交集
//                        if (midCode instanceof ABSTRACT_JUMP) {
//                            if (!(midCode instanceof RETURN)) {
//                                // 可以删除
//                                toBeDelete.add(midCode);
//                                HashSet<MidCode> temp = new HashSet<>();
//                                if (midCode instanceof ABSTRACT_USE) {
//                                    // 开始迭代搜索
//
//                                    HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
//                                    for (String use : uses) {
//                                        ArrayList<ABSTRACT_DEF> defs = midCode.arriveDefs.get(use);
//                                        for (ABSTRACT_DEF def : defs) {
//                                            if (((MidCode)def).arriveUses.size() == 1) {
//                                                // 只有一个使用
//                                                temp.add((MidCode) def);
//                                            }
//                                        }
//                                    }
//                                    // 迭代
//                                    boolean vary = true;
//                                    while (vary) {
//                                        int old = temp.size();
//                                        for (MidCode midCode1:temp) {
//                                            if (midCode1 instanceof ABSTRACT_USE) {
//                                                HashSet<String> uses2 = ((ABSTRACT_USE) midCode).getUse();
//                                                for (String use : uses2) {
//                                                    ArrayList<ABSTRACT_DEF> defs = midCode.arriveDefs.get(use);
//                                                    for (ABSTRACT_DEF def : defs) {
//                                                        if (((MidCode)def).arriveUses.size() == 1) {
//                                                            // 只有一个使用
//                                                            temp.add((MidCode) def);
//                                                        }
//                                                    }
//                                                }
//                                            }
//                                        }
//                                        int newSize = temp.size();
//                                        vary = (old != newSize);
//                                    }
//                                }
//                                toBeDelete.addAll(temp);
//                            }
//                        }
//                    }
//
//
//
//                    midCodes1.removeAll(toBeDelete);
//
//
//                    toBeDelete.clear();
//
//
//                    midCodes1.removeAll(toBeDelete);

                    // 且定义语句不能传播到外面的控制流语句

                    boolean dead = false;

                    HashSet<MidCode> temp = new HashSet<>();
                    for (MidCode midCode : loop.haveMidCodes) {

                        if (midCodes1.contains(midCode)) {
                            break;
                        }

                        temp.clear();
                        if (midCode instanceof ABSTRACT_DEF) {
                            temp.add(midCode);
                            for (ABSTRACT_USE use : midCode.arriveUses) {
                                temp.add((MidCode)use);
                            }
                        }
                        boolean vary = true;
                        HashSet<MidCode> t = new HashSet<>();
                        HashSet<MidCode> newT = new HashSet<>();
                        newT.addAll(temp);
                        while (vary) {
                            int oldsize = temp.size();
                            int newsize;
                            for (MidCode midCode1 : newT) {
                                if (midCode1 instanceof ABSTRACT_DEF) {
                                    for (ABSTRACT_USE use : midCode1.arriveUses) {
                                        t.add((MidCode)use);
                                    }
                                }
                            }
                            newsize = temp.size();
                            temp.addAll(t);
                            newT.clear();
                            newT.addAll(t);
                            vary = (oldsize!=newsize);
                        }
                        for (MidCode midCode1 : temp) {
                            if (midCode1 instanceof ABSTRACT_JUMP && !loop.haveMidCodes.contains(midCode1)) {
                                dead = true;
                                break;
                            }
                        }
                        if (dead) {
                            break;
                        }
                    }

                    if (midCodes1.size() == 0 && !dead) {
                        // 如果交集为空 则是安全的 可以删除
                        isVary = true;
                        String endLabel = ((END_WHILE)loop.endLoop).label;
                        MidCode gotoMidCode = new GOTO(endLabel);
                        gotoMidCode.isDelete = false;
                        gotoMidCode.setBelong(loop.startLoop.getBelong());
                        MidCode start = loop.startLoop;
                        // 将新的语句插入到中间代码中
                        gotoMidCode.nextMidCode = start;
                        gotoMidCode.prevMidCode = start.prevMidCode;
                        start.prevMidCode.nextMidCode = gotoMidCode;
                        start.prevMidCode = gotoMidCode;

                        loop.isDelete = true;
                        // 删除之后顶层不再包含这个loop
                        if (loop.prevLoop != null) {
                            loop.prevLoop.subLoops.remove(loop);
                        }
                    }
                }
            }
        }


        midCodes.clear();
        MidCode p = head;
        while (!p.nextMidCode.equals(tail)) {
            p = p.nextMidCode;
            midCodes.add(p);
        }
        for (MidCode midCode : midCodes){
            // 由于插入了跳转语句 基本块需要重新做
            midCode.isBlockTail = false;
        }
    }

    public static void checkReturnError() {
        // 检查引入了控制流之后的return类型error
        for (String funcKey : func2SubFlow.keySet()) {
            func2SubFlow.get(funcKey).checkReturnError();
        }
    }
}
