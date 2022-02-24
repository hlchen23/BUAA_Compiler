package Optimizer.ControlFlowAnalysis;

import Macro.Macro;
import MidCode.MidCodeElement.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import MidCode.Utility;
import Mips.Register;
import Optimizer.RegisterAlloc.ConflictGraph;
import Optimizer.RegisterAlloc.NewRegAlloc;
import Error.MyError;

public class SubFlow {

    public Type type;
    // 记录函数的类型
    public enum Type {
        INT,
        VOID
    }

    // 函数末尾的}的行号 rbrace
    public int tailNo;

    // 每个函数对应一个子流图

    private ConflictGraph conflictGraph;

    private NewRegAlloc regAlloc;

    // s寄存器分配表 冲突分析得出
    public HashMap<String, Register> sRegAllocTable = new HashMap<>();

    public String funcName;

    // 入口基本块
    private BaseBlock entrance;

    // 函数拥有的中间代码
    // 对基本块中的midCode调整后 这个可能不对
    public ArrayList<MidCode> midCodes = new ArrayList<>();

    // 函数拥有的基本块
    public HashMap<String,BaseBlock> baseBlocks = new HashMap<>();

    // 按照顺序管理函数拥有的基本块
    public ArrayList<BaseBlock> baseBlockArrayList = new ArrayList<>();

    // 流图的使用定义链分析 变量名-->链条集合
    // 定义语句-->一条链
    public HashMap<String,HashMap<ABSTRACT_DEF,Chain>> var2Chains = new HashMap<>();
    // 流图的使用定义网分析 变量名-->网络集合
    public HashMap<String,HashSet<Web>> var2Webs = new HashMap<>();

    // 建立映射
    // varName -> 所有基本块中所有定义了varName的语句(跨基本块)
    private HashMap<String, HashSet<MidCode>> allMidCodesDefVar = new HashMap<>();

    public void addMidCode(MidCode midCode) {
        this.midCodes.add(midCode);
    }

    private BaseBlock now;
    // 切割基本块
    public void cutDown() {
        now = new BaseBlock();

        baseBlockArrayList.add(now);

        now.setName(funcName);
        entrance = now;
        baseBlocks.put(now.getName(),now);
        // 分配出口基本块
        BaseBlock exit = new BaseBlock();
        exit.setName(Macro.EXIT_BLOCK_NAME);
        exit.setDstNum(0);
        baseBlocks.put(exit.getName(),exit);
        for (MidCode midCode : midCodes) {
            if (midCode instanceof ABSTRACT_JUMP) {
                midCode.setInBaseBlock(now);
                now.addMidCode(midCode);
                now.setDstNum(((ABSTRACT_JUMP) midCode).getDstNum());
                now.setDst_1(((ABSTRACT_JUMP) midCode).getDst_1());
                now.setDst_2(((ABSTRACT_JUMP) midCode).getDst_2());
            }
            else if (midCode instanceof ABSTRACT_LABEL) {
                BaseBlock prev = now;
                if (!prev.dstIsSet()) {
                    // 前面没有跳转
                    // 应顺序连接
                    prev.setDstNum(1);
                    prev.setDst_1(((ABSTRACT_LABEL) midCode).getEntrance());
                }
                now = new BaseBlock();

                baseBlockArrayList.add(now);

                midCode.setInBaseBlock(now);
                now.addMidCode(midCode);
                now.setName(((ABSTRACT_LABEL) midCode).getEntrance());
                baseBlocks.put(now.getName(),now);
            }
            else {
                midCode.setInBaseBlock(now);
                now.addMidCode(midCode);
            }
        }

        baseBlockArrayList.add(exit);

        // 设置基本块末尾的中间代码
        for (BaseBlock baseBlock:baseBlocks.values()) {
            baseBlock.setTailMidCode();
        }
    }

    public void setTailMidCode() {
        for (BaseBlock baseBlock:baseBlocks.values()) {
            baseBlock.setTailMidCode();
        }
    }

    public void link() {
        for (String nameKey : baseBlocks.keySet()) {
            BaseBlock baseBlock = baseBlocks.get(nameKey);
            int dstNum = baseBlock.getDstNum();
            if (dstNum == 1) {
                String dst_1 = baseBlock.getDst_1();
                BaseBlock dst_1_bb = baseBlocks.get(dst_1);
                link2Block(baseBlock,dst_1_bb);
            }
            else if (dstNum == 2) {
                String dst_1 = baseBlock.getDst_1();
                String dst_2 = baseBlock.getDst_2();
                BaseBlock dst_1_bb = baseBlocks.get(dst_1);
                BaseBlock dst_2_bb = baseBlocks.get(dst_2);
                link2Block(baseBlock,dst_1_bb);
                link2Block(baseBlock,dst_2_bb);
            }
        }
    }

    private void link2Block(BaseBlock src, BaseBlock dst) {
        src.getNexts().add(dst);
        dst.getPrevs().add(src);
    }

    public void innerTempUseDefAnalyze() {
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        for (BaseBlock baseBlock:baseBlocks.values()) {
            baseBlock.innerTempUseDefAnalyze();
        }
    }

    public String inactive2Str() {
        String retStr = "";
        retStr += "\r\n";
        for (BaseBlock baseBlock:baseBlocks.values()) {
            retStr += baseBlock.inactive2Str();
        }
        retStr += "\r\n";
        return retStr;
    }

    public String activeTemp2Str() {
        String retStr = "";
        retStr += "\r\n";
        for (BaseBlock baseBlock:baseBlocks.values()) {
            retStr += baseBlock.activeTemp2Str();
        }
        retStr += "\r\n";
        return retStr;
    }

    public void calculateAllUses() {
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        for (BaseBlock baseBlock:baseBlocks.values()) {
            baseBlock.calculateAllUses();
        }
    }

    public void killGenAnalyze() {
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        // 分析gen
        for (String blockName:baseBlocks.keySet()) {
            baseBlocks.get(blockName).genAnalyze(allMidCodesDefVar);
        }
        // 分析kill
        for (BaseBlock baseBlock:baseBlocks.values()) {
            HashSet<MidCode> gen = baseBlock.getGen();
            HashSet<MidCode> kill = baseBlock.getKill();
            for (MidCode midCode : gen) {
                String varName = ((ABSTRACT_DEF) midCode).getDef();
                HashSet<MidCode> defVar = allMidCodesDefVar.get(varName);
                kill.addAll(defVar);
            }
            // 不用把自己定义的除掉
            // 从数据流方程上看不需要
            // kill.removeAll(gen);
        }
    }

    public void useDefAnalyze() {
        //if (funcName.equals(Macro.GLOBAL_MARK)) {
        //    return;
        //}
        for (String blockName:baseBlocks.keySet()) {
            baseBlocks.get(blockName).useDefAnalyze();
        }
    }

    public void activeAnalyze() {
        // global不分析了
        //if (funcName.equals(Macro.GLOBAL_MARK)) {
        //    return;
        //}
        boolean isVary;
        do {
            isVary = false;
            for (BaseBlock baseBlock:baseBlocks.values()) {
                HashSet<String> preIn = baseBlock.getIn();
                HashSet<String> newOut = new HashSet<>();
                HashSet<String> newIn = new HashSet<>();
                for (BaseBlock next:baseBlock.getNexts()) {
                    newOut.addAll(next.getIn());
                }
                baseBlock.setOut(newOut);
                newIn.addAll(baseBlock.getUse());
                HashSet<String> tmp = new HashSet<>();
                tmp.addAll(baseBlock.getOut());
                tmp.removeAll(baseBlock.getDef());
                newIn.addAll(tmp);
                baseBlock.setIn(newIn);
                // in[B]是不减少的
                if (newIn.size() > preIn.size()) {
                    isVary = true;
                }
            }
        } while (isVary);
    }

    public void arriveDefAnalyze() {
        // global不分析了
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        boolean isVary;
        do {
            isVary = false;
            for (BaseBlock baseBlock:baseBlocks.values()) {
                HashSet<MidCode> preArriveDefOut = baseBlock.getArrive_def_out();
                HashSet<MidCode> newArriveDefIn = new HashSet<>();
                HashSet<MidCode> newArriveDefOut = new HashSet<>();
                for (BaseBlock prev : baseBlock.getPrevs()) {
                    newArriveDefIn.addAll(prev.getArrive_def_out());
                }
                baseBlock.setArrive_def_in(newArriveDefIn);
                newArriveDefOut.addAll(baseBlock.getGen());
                HashSet<MidCode> tmp = new HashSet<>();
                tmp.addAll(baseBlock.getArrive_def_in());
                tmp.removeAll(baseBlock.getKill());

                newArriveDefOut.addAll(tmp);
                baseBlock.setArrive_def_out(newArriveDefOut);
                // out[B]是不减少的
                if (newArriveDefOut.size() > preArriveDefOut.size()) {
                    isVary = true;
                }
            }
        } while (isVary);
    }

    public void defUseChainAnalyze() {
        // 全局不分析
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        // 根据定义点确定分配
        for (BaseBlock baseBlock:baseBlocks.values()) {
            // 不是用gen 而是用这个基本块所有的定义
            for (MidCode midCode:baseBlock.getMidCodes()) {
                if (midCode instanceof ABSTRACT_DEF) {
                    ABSTRACT_DEF def = (ABSTRACT_DEF) midCode;
                    String var = def.getDef();
                    if (!var2Chains.containsKey(var)) {
                        var2Chains.put(var,new HashMap<>());
                    }
                    HashMap<ABSTRACT_DEF,Chain> chains = var2Chains.get(var);
                    Chain chain = new Chain();
                    chain.setDef(def);
                    chains.put(def,chain);
                }
            }
        }
        // 找到每一个链条的使用点
        for (BaseBlock baseBlock:baseBlocks.values()) {
            HashSet<MidCode> arriveDefIn = baseBlock.getArrive_def_in();
            // 建立一个方便根据变量名查找定义语句的数据结构
            HashMap<String,HashSet<ABSTRACT_DEF>> arriveDefInMap = new HashMap<>();
            for (MidCode midCode:arriveDefIn) {
                ABSTRACT_DEF def = (ABSTRACT_DEF) midCode;
                String defVar = def.getDef();
                if (!arriveDefInMap.containsKey(defVar)) {
                    arriveDefInMap.put(defVar,new HashSet<>());
                }
                arriveDefInMap.get(defVar).add(def);
            }

            // 被第一次定义
            HashSet<String> firstDef = new HashSet<>();
            // 应是先使用后定义才放入定义使用链 定义后的使用属于块内
            for (int i = 0; i < baseBlock.getMidCodes().size(); i++) {
                MidCode midCode = baseBlock.getMidCodes().get(i);
                if (midCode instanceof ABSTRACT_USE) {
                    HashSet<String> useVars = ((ABSTRACT_USE) midCode).getUse();
                    for (String useVar:useVars) {
                        if (!firstDef.contains(useVar)) {
                            if (arriveDefInMap.containsKey(useVar)) {
                                HashSet<ABSTRACT_DEF> defs = arriveDefInMap.get(useVar);
                                for (ABSTRACT_DEF def : defs) {
                                    var2Chains.get(useVar).get(def).addUse((ABSTRACT_USE) midCode);
                                    var2Chains.get(useVar).get(def).isCrossBlock = true;
                                    // 存储每个使用的中间代码可能拥有的定义来源
                                    if (!midCode.arriveDefs.containsKey(useVar)) {
                                        midCode.arriveDefs.put(useVar,new ArrayList<>());
                                    }
                                    midCode.arriveDefs.get(useVar).add(def);
                                }
                            }
                        }
                    }
                }
                if (midCode instanceof ABSTRACT_DEF) {
                    String def = ((ABSTRACT_DEF) midCode).getDef();
                    if (!firstDef.contains(def)) {
                        firstDef.add(def);
                    }
                }
            }
        }
        for (BaseBlock baseBlock:baseBlocks.values()) {
            // 补充基本块内的数据流
            // 线性扫描
            // 找到一个定义语句 把这个定义语句后面直到下次定义的使用放入链条
            for (int i = 0;i < baseBlock.getMidCodes().size(); i++) {
                MidCode midCode = baseBlock.getMidCodes().get(i);
                if (midCode instanceof ABSTRACT_DEF) {
                    String def = ((ABSTRACT_DEF) midCode).getDef();
                    for (int j = i+1; j < baseBlock.getMidCodes().size(); j++) {
                        if (baseBlock.getMidCodes().get(j) instanceof ABSTRACT_USE) {
                            HashSet<String> uses =  ((ABSTRACT_USE) baseBlock.getMidCodes().get(j)).getUse();
                            if (uses.contains(def)) {
                                // 放入链条
                                var2Chains.get(def).get(midCode).addUse((ABSTRACT_USE) baseBlock.getMidCodes().get(j));
                                // 存储每个使用的中间代码可能拥有的定义来源
                                if (!baseBlock.getMidCodes().get(j).arriveDefs.containsKey(def)) {
                                    baseBlock.getMidCodes().get(j).arriveDefs.put(def,new ArrayList<>());
                                }
                                baseBlock.getMidCodes().get(j).arriveDefs.get(def).add((ABSTRACT_DEF) midCode);
                            }
                        }
                        if (baseBlock.getMidCodes().get(j) instanceof ABSTRACT_DEF) {
                            String newDef = ((ABSTRACT_DEF) baseBlock.getMidCodes().get(j)).getDef();
                            if (def.equals(newDef)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    public String chain2String() {
        String retStr = "";
        for (String varName : var2Chains.keySet()) {
            retStr += "\r\n";
            retStr += "=========================\r\n";
            retStr += "var: " + varName + "\r\n";
            for (Chain chain:var2Chains.get(varName).values()) {
                retStr += chain.chain2String();
            }
            retStr += "=========================\r\n";
            retStr += "\r\n";
        }
        return retStr;
    }

    public void defUseWebAnalyze() {
        // 全局不分析
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        for (String var:var2Chains.keySet()) {
            var2Webs.put(var,WebMaker.createWebs(var2Chains.get(var)));
        }
    }

    public void webRename() {
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        HashSet<Web> websTemp = new HashSet<>();
        // rename是复用前面活跃变量分析的最好方法
        for (String var : var2Webs.keySet()) {
            if (Utility.isGlobalVar(var)) {
                // 全局变量不能重命名
                continue;
            }
            HashSet<Web> webs = var2Webs.get(var);
            for (Web web:webs) {
                websTemp.add(web);
                web.webRename();
            }
        }
        var2Webs = new HashMap<>();
        for (Web web:websTemp) {
            HashSet<Web> temp = new HashSet<>();
            temp.add(web);
            var2Webs.put(web.getVar(),temp);
        }
    }

    public String web2String() {
        String retStr = "";
        for (String varName : var2Webs.keySet()) {
            retStr += "\r\n";
            retStr += "------------------------\r\n";
            retStr += "var: " + varName + "\r\n";
            for (Web web:var2Webs.get(varName)) {
                retStr += web.web2String();
            }
            retStr += "------------------------\r\n";
            retStr += "\r\n";
        }
        return retStr;
    }

    public void calculateSuccessors() {
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        // 初始化每个基本块的后继为直接后继
        for (String blockName:baseBlocks.keySet()) {
            BaseBlock baseBlock = baseBlocks.get(blockName);
            for (BaseBlock next : baseBlock.getNexts()) {
                baseBlock.getSuccessors().put(next.getName(),next);
            }
        }
        // 迭代法求解后继
        boolean isVary;
        do {
            isVary = false;
            for (String blockName:baseBlocks.keySet()) {
                BaseBlock baseBlock = baseBlocks.get(blockName);
                int preLength = baseBlock.getSuccessors().size();
                HashMap<String,BaseBlock> temp = new HashMap<>();
                for (String key : baseBlock.getSuccessors().keySet()) {
                    for (String subKey : baseBlock.getSuccessors().get(key).getSuccessors().keySet()) {
                        temp.put(
                                subKey,baseBlock.getSuccessors().get(key).getSuccessors().get(subKey));
                    }
                }
                for (String key : temp.keySet()) {
                    baseBlock.getSuccessors().put(key,temp.get(key));
                }
                int nowLength = baseBlock.getSuccessors().size();
                if (nowLength > preLength) {
                    isVary = true;
                }
            }
        } while (isVary);
        // 去掉自身
        for (String blockName:baseBlocks.keySet()) {
            if (baseBlocks.get(blockName).getSuccessors().containsKey(blockName)) {
                baseBlocks.get(blockName).getSuccessors().remove(blockName);
            }
        }
    }

    public void calculatePrecursors() {
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        // 初始化每个基本块的前驱为直接前驱
        for (String blockName:baseBlocks.keySet()) {
            BaseBlock baseBlock = baseBlocks.get(blockName);
            for (BaseBlock prev : baseBlock.getPrevs()) {
                baseBlock.getPrecursors().put(prev.getName(),prev);
            }
        }
        // 迭代法求解前驱
        boolean isVary;
        do {
            isVary = false;
            for (String blockName:baseBlocks.keySet()) {
                BaseBlock baseBlock = baseBlocks.get(blockName);
                int preLength = baseBlock.getPrecursors().size();
                HashMap<String,BaseBlock> temp = new HashMap<>();
                for (String key : baseBlock.getPrecursors().keySet()) {
                    for (String subKey : baseBlock.getPrecursors().get(key).getPrecursors().keySet()) {
                        temp.put(
                                subKey,baseBlock.getPrecursors().get(key).getPrecursors().get(subKey));
                    }
                }
                for (String key : temp.keySet()) {
                    baseBlock.getPrecursors().put(key,temp.get(key));
                }
                int nowLength = baseBlock.getPrecursors().size();
                if (nowLength > preLength) {
                    isVary = true;
                }
            }
        } while (isVary);
        // 去掉自身
        for (String blockName:baseBlocks.keySet()) {
            if (baseBlocks.get(blockName).getPrecursors().containsKey(blockName)) {
                baseBlocks.get(blockName).getPrecursors().remove(blockName);
            }
        }
    }

    public String useDef2Str() {
        String retStr = "";
        for (String blockName : baseBlocks.keySet()) {
            retStr += baseBlocks.get(blockName).useDef2Str();
        }
        return retStr;
    }

    public String killGen2Str() {
        String retStr = "";
        for (String blockName : baseBlocks.keySet()) {
            retStr += baseBlocks.get(blockName).killGen2Str();
        }
        return retStr;
    }

    public String inOut2Str() {
        String retStr = "";
        for (String blockName:baseBlocks.keySet()) {
            retStr += baseBlocks.get(blockName).inOut2Str();
        }
        return retStr;
    }

    public String arriveDefInOut2Str() {
        String retStr = "";
        for (String blockName:baseBlocks.keySet()) {
            retStr += baseBlocks.get(blockName).arriveDefInOut2Str();
        }
        return retStr;
    }

    public String successors2Str() {
        String retStr = "";
        for (String blockName : baseBlocks.keySet()) {
            retStr += baseBlocks.get(blockName).successors2Str();
        }
        return retStr;
    }

    public String precursors2Str() {
        String retStr = "";
        for (String blockName : baseBlocks.keySet()) {
            retStr += baseBlocks.get(blockName).precursors2Str();
        }
        return retStr;
    }

    public String flowChart2Str() {
        String retStr = "\r\n";
        retStr += String.format("*************************FUN: %s***************************\r\n",funcName);
        for (String blockName : baseBlocks.keySet()) {
            retStr += baseBlocks.get(blockName).toString();
        }
        retStr += "*************************END_FUN***************************\r\n";
        retStr += "\r\n";
        return retStr;
    }

    public BaseBlock getEntrance() {
        return this.entrance;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public void conflictAnalyze() {
        //if (funcName.equals(Macro.GLOBAL_MARK)) {
        //    return;
        //}
        conflictGraph = new ConflictGraph(this);
        conflictGraph.analyze();
        this.sRegAllocTable = conflictGraph.allocTable;
    }

    public void initRegAllocPool() {
        regAlloc = new NewRegAlloc(this);
    }

    public Register applyReadReg(String name,MidCode midCode) {
        return regAlloc.applyReadReg(name,midCode);
    }

    public Register applyWriteReg(String name,MidCode midCode) {
        return regAlloc.applyWriteReg(name,midCode);
    }

    public void save(MidCode midCode) {
        regAlloc.save(midCode);
    }

    public void restore(MidCode midCode) {
        regAlloc.restore(midCode);
    }

    public Register borrowReg(MidCode midCode) {
        return regAlloc.borrowReg(midCode);
    }

    public void returnReg(Register reg, MidCode midCode) {
        regAlloc.returnReg(reg,midCode);
    }

    public void clearBorrow() {
        regAlloc.clearBorrow();
    }

    public void saveBlockTail(MidCode midCode) {
        regAlloc.saveBlockTail(midCode);
    }

    public void deadCodeDelete() {
        for (BaseBlock baseBlock : baseBlocks.values()) {
            baseBlock.deadCodeDelete();
        }
    }

    public void deleteMainTail() {
        if (!funcName.equals("main")) {
            return;
        }
        // 图算法
        BaseBlock exitBlock = baseBlocks.get(Macro.EXIT_BLOCK_NAME);
        // BFS
        int layers = 0;

    }

    public void checkReturnError() {
        if (funcName.equals(Macro.GLOBAL_MARK)) {
            return;
        }
        if (type == Type.VOID) {
            BaseBlock exitBlock = baseBlocks.get(Macro.EXIT_BLOCK_NAME);
            ArrayList<BaseBlock> prevs = exitBlock.getPrevs();
            // void类型的函数
            // 检查连接exit的基本块是否有return int

            for (BaseBlock prev : prevs) {

                // 只有return的语句才会连接到exit
                MidCode last = prev.midCodes.get(prev.midCodes.size() - 1);
                RETURN retLast = (RETURN) last;
                if (retLast.getType() == RETURN.Type.INT) {
                    MyError.add_voidFunc_return(retLast.lineNo);
                }
            }
        }
        else {
            // INT类型的函数
            // 检查是否各个分支都有返回语句
            // 从入口开始广搜 如果所有的路径都能走到exit则是合理的
            // 如果发现无路可走了 只能是exit
            ArrayList<BaseBlock> paths = new ArrayList<>();
            HashSet<BaseBlock> pathsSet = new HashSet<>();
            paths.add(entrance);
            pathsSet.add(entrance);// 记录走过的

            while (paths.size() > 0) {
                BaseBlock first = paths.remove(0);
                if (first.getNexts().size() == 0 && !first.getName().equals(Macro.EXIT_BLOCK_NAME)) {
                    MyError.add_func_noReturn(tailNo);
                    return;
                }
                for (BaseBlock baseBlock : first.getNexts()) {
                    if (pathsSet.contains(baseBlock)) {

                    }
                    else {
                        pathsSet.add(baseBlock);
                        paths.add(baseBlock);
                    }
                }
            }
        }
    }
}
