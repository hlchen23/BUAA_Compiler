package Optimizer.ControlFlowAnalysis;

import MidCode.MidCodeElement.ABSTRACT_DEF;
import MidCode.MidCodeElement.ABSTRACT_USE;

import java.util.HashSet;

public class Chain {
    // 定义使用链

    private static int createNo = 0;

    private int No;

    private ABSTRACT_DEF def;
    private HashSet<ABSTRACT_USE> uses = new HashSet<>();

    public boolean isCrossBlock = false; // 标记链条是否跨越基本块

    public Chain() {
        No = createNo++;
    }

    public int getNo() {
        return No;
    }

    public String chain2String() {
        String retStr = "**********Chain***********\r\n";
        if (isCrossBlock) {
            retStr += "跨越基本块\r\n";
        }
        else {retStr += "不跨越基本块\r\n";}
        retStr += String.format("def: %s\r\n",def);
        retStr += "uses: " + uses.toString() + "\r\n";
        return retStr;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (!(object instanceof Chain)) {
            return false;
        }
        Chain chainObj = (Chain) object;
        return this.No == chainObj.getNo();
    }

    @Override
    public int hashCode() {
        return No;
    }

    public ABSTRACT_DEF getDef() {
        return def;
    }

    public HashSet<ABSTRACT_USE> getUses() {
        return uses;
    }

    public void setDef(ABSTRACT_DEF def) {
        this.def = def;
    }

    public void setUses(HashSet<ABSTRACT_USE> uses) {
        this.uses = uses;
    }

    public void addUse(ABSTRACT_USE use) {
        this.uses.add(use);
    }

    public void webRename(int no) {

    }
}
