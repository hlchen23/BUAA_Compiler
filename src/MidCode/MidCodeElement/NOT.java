package MidCode.MidCodeElement;

import Macro.Macro;
import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class NOT extends MidCode
        implements ABSTRACT_DEF, ABSTRACT_USE {
    private String opt;
    private String dst;

    public NOT(String opt, String dst) {
        super();
        this.opt = opt;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = ! %s\r\n",dst, opt);
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            Register optReg = Flow.applyReadReg(opt,this);
            Register dstReg = Flow.applyWriteReg(dst,this);
            MipsFactory.mipsCode += String.format("seq %s, %s, %s\r\n",
                    dstReg,optReg,Register.zero);
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        Register optReg = RegAlloc.applyReg(opt,this);
        Register dstReg = RegAlloc.writeReg(dst,this);
        MipsFactory.mipsCode += String.format("seq %s, %s, %s\r\n",
                dstReg,optReg,Register.zero);
        if (!Utility.isTemp(dst)) {
            GlobalRegAlloc.writeBack(dst,this);
        }
    }

    @Override
    public String createMips() {
        if (Utility.isVar(opt)) {
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt,belong),
                    Utility.getPointerReg(opt,belong));
            retStr += String.format("seq %s, %s, %s\r\n",
                    Register.t1,
                    Register.t0,
                    Register.zero);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else {
            // 常数
            String retStr = "";
            String belong = super.getBelong();
            int num = Integer.valueOf(opt);
            int out = (num == 0)? 1: 0;
            retStr += String.format("li %s, %s\r\n", Register.t0,String.valueOf(out));
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
    }

    @Override
    public String getDef() {
        return dst;
    }

    @Override
    public HashSet<String> getUse() {
        if (isDelete) {
            return new HashSet<>();
        }
        else {
            HashSet<String> retSet = new HashSet<>();
            if (Utility.isVar(opt)) {
                retSet.add(opt);
            }
            return retSet;
        }
    }

    @Override
    public void renameDef(String src, String suffix) {
        if (dst.equals(src)) {
            dst = dst + suffix;
        }
    }

    @Override
    public void renameUse(String src, String suffix) {
        if (opt.equals(src)) {
            if (arriveDefs.containsKey(opt)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(opt);
                arriveDefs.remove(opt);
                arriveDefs.put(opt + suffix,defs);
            }
            opt = opt + suffix;
        }
    }

    @Override
    public void behaviorAfterDelete() {
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public boolean canConvert2Assign() {
        return (!Utility.isVar(opt));
    }

    @Override
    public String convert2Assign() {
        if (canConvert2Assign()) {
            int optNum = Integer.valueOf(opt);
            int not = (optNum == 0)? 1: 0;
            return String.valueOf(not);
        }
        else {
            return null;
        }
    }

    @Override
    public void copySpreadRename(String src, String dst) {
        if (opt.equals(src)) {
            opt = dst;
        }
    }

    @Override
    public String getCopySpreadLeftValue() {
        return dst;
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
