package Optimizer.ControlFlowAnalysis;

import Macro.Macro;
import MidCode.MidCodeElement.ABSTRACT_DEF;
import MidCode.MidCodeElement.ABSTRACT_USE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Web {
    // 定义使用网

    private static int createNo = 0;

    private int No;

    private String var=null;

    // 网管理的链
    private HashMap<ABSTRACT_DEF,Chain> chains = new HashMap<>();

    public boolean canGetSReg; // 标记是否这个网能够参与全局寄存器分配 否则是临时
    public ArrayList<Web> conflictWebs = new ArrayList<>(); // 冲突的网 参与全局寄存器分配的有

    public Web() {
        No = createNo++;
    }

    public void setCanGetSReg() {
        canGetSReg = false;
        for (Chain chain:chains.values()) {
            if (chain.isCrossBlock) {
                canGetSReg =true;
            }
        }
    }

    public int getNo() {
        return No;
    }

    public String getVar() {
        return var;
    }

    public void addChain(Chain chain) {
        chains.put(chain.getDef(),chain);
        if (var==null) {
            var = chain.getDef().getDef();
        }
    }

    public void addChains(ArrayList<Chain> chains) {
        for (Chain chain:chains) {
            addChain(chain);
        }
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof Web)) {
            return false;
        }
        Web webObj = (Web) object;
        return this.No == webObj.getNo();
    }

    @Override
    public int hashCode() {
        return No;
    }

    public HashMap<ABSTRACT_DEF, Chain> getChains() {
        return chains;
    }

    public void setChains(HashMap<ABSTRACT_DEF, Chain> chains) {
        this.chains = chains;
    }

    public String web2String() {
        String retStr = "***********Web***********\r\n";
        if (canGetSReg) {
            retStr += "参与s寄存器分配\r\n";
        }
        else {
            retStr += "不参与s寄存器分配\r\n";
        }
        for (Chain chain:chains.values()) {
            retStr += chain.chain2String();
        }
        return retStr;
    }

    public void webRename() {
        if (!Flow.renames.containsKey(var)) {
            Flow.renames.put(var,new ArrayList<>());
        }
        // 一个web一个变量
        String suffix = Macro.WEB_RENAME_SUFFIX + No;
        // 网中所有的使用与定义
        HashSet<ABSTRACT_DEF> defs = new HashSet<>();
        HashSet<ABSTRACT_USE> uses = new HashSet<>();
        for (Chain chain : chains.values()) {
            defs.add(chain.getDef());
            uses.addAll(chain.getUses());
        }
        for (ABSTRACT_DEF def:defs) {
            def.renameDef(var,suffix);
        }
        for (ABSTRACT_USE use:uses) {
            use.renameUse(var,suffix);
        }
        Flow.renames.get(var).add(var+suffix);
        var = var + suffix;
    }
}
