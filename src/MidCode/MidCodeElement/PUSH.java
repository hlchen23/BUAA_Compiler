package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class PUSH extends MidCode
        implements ABSTRACT_USE {
    private String name;

    public PUSH(String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("push %s\r\n",name);
    }


    @Override
    public void createMipsOpt() {
        Register useReg = Flow.applyReadReg(name,this);
        MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                useReg,
                // 相加 记得加括号 否则是整数拼接
                (Utility.getPushOffset()-Utility.getPushAlign()),
                Register.sp);
    }

    public void createMipsOpt_Old() {
        Register useReg = RegAlloc.applyReg(name,this);
        MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                useReg,
                // 相加 记得加括号 否则是整数拼接
                (Utility.getPushOffset()-Utility.getPushAlign()),
                Register.sp);
    }

    @Override
    public String createMips() {
        if (Utility.isVar(name)) {
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(name,belong), // 校准
                    Utility.getPointerReg(name,belong));
            // 压入栈帧
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    // 相加 记得加括号 否则是整数拼接
                    (Utility.getPushOffset()-Utility.getPushAlign()),
                    Register.sp);
            return  retStr;
        }
        else {
            // 常数
            String retStr = "";
            retStr += String.format("li %s, %s\r\n",
                    Register.t0,
                    name);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    (Utility.getPushOffset()-Utility.getPushAlign()),
                    Register.sp);
            return retStr;
        }
    }

    public String getName() {
        return name;
    }

    @Override
    public HashSet<String> getUse() {
        HashSet<String> retSet = new HashSet<>();
        if (Utility.isVar(name)) {
            retSet.add(name);
        }
        return retSet;
    }

    @Override
    public void renameUse(String src, String suffix) {
        if (name.equals(src)) {
            if (arriveDefs.containsKey(name)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(name);
                arriveDefs.remove(name);
                arriveDefs.put(name + suffix,defs);
            }
            name = name + suffix;
        }
    }

    @Override
    public boolean canConvert2Assign() {
        return false;
    }

    @Override
    public String convert2Assign() {
        return null;
    }

    @Override
    public void copySpreadRename(String src, String dst) {
        if (name.equals(src)) {
            name = dst;
        }
    }

    @Override
    public String getCopySpreadLeftValue() {
        return null;
    }
}
