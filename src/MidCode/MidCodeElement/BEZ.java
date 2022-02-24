package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class BEZ extends MidCode
        implements ABSTRACT_JUMP, ABSTRACT_USE {

    private String cond;
    private String label;

    public BEZ(String cond, String label) {
        super();
        this.cond = cond;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("if %s is 0 goto %s\r\n",cond,label);
    }

    @Override
    public String createMips() {
        if (Utility.isVar(cond)) {
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(cond,belong),
                    Utility.getPointerReg(cond,belong));
            retStr += String.format("beq %s, %s, %s\r\n",
                    Register.t0,
                    Register.zero,
                    label);
            return retStr;
        }
        else {
            // 常数
            String retStr = "";
            retStr += String.format("li %s, %s\r\n",
                    Register.t0,
                    cond);
            retStr += String.format("beq %s, %s, %s\r\n",
                    Register.t0,
                    Register.zero,
                    label);
            return retStr;
        }
    }

    @Override
    public void createMipsOpt() {
        // 不关心是否是常数
        Register r = Flow.applyReadReg(cond, this);

        Flow.saveBlockTail(this);

        MipsFactory.mipsCode += String.format("beq %s, %s, %s\r\n",
                r,
                Register.zero,
                label);
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        // 不关心是否是常数
        Register r = RegAlloc.applyReg(cond,this);
        MipsFactory.mipsCode += String.format("beq %s, %s, %s\r\n",
                r,
                Register.zero,
                label);
    }

    private String dst_1;
    @Override
    public int getDstNum() {
        return 2;
    }

    @Override
    public String getDst_1() {
        return dst_1;
    }

    @Override
    public String getDst_2() {
        return label;
    }

    @Override
    public void setDst_1(String dst_1) {
        this.dst_1 = dst_1;
    }

    @Override
    public void setDst_2(String dst_2) {

    }

    @Override
    public HashSet<String> getUse() {
        HashSet<String> retSet = new HashSet<>();
        if (Utility.isVar(cond)) {
            retSet.add(cond);
        }
        return retSet;
    }

    @Override
    public void renameUse(String src, String suffix) {
        if (cond.equals(src)) {
            if (arriveDefs.containsKey(cond)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(cond);
                arriveDefs.remove(cond);
                arriveDefs.put(cond + suffix,defs);
            }
            cond  = cond + suffix;
        }
    }

    @Override
    public boolean canConvert2Assign() {
        // 跳转语句 不能转为赋值
        return false;
    }

    @Override
    public String convert2Assign() {
        return null;
    }

    @Override
    public void copySpreadRename(String src, String dst) {
        if (cond.equals(src)) {
            cond = dst;
        }
    }

    @Override
    public String getCopySpreadLeftValue() {
        return null;
    }
}
