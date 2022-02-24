package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class ASSIGN extends MidCode
        implements ABSTRACT_DEF, ABSTRACT_USE {
    private String opt;
    private String dst;

    public ASSIGN(String opt, String dst) {
        super();
        this.opt = opt;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = %s\r\n",dst,opt);
    }

    @Override
    public String createMips() {
        if (Utility.isVar(opt)) {
            // 变量赋值
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt,belong),
                    Utility.getPointerReg(opt,belong));
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else {
            // 常数赋值
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("li %s, %s\r\n",
                    Register.t0,
                    opt);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            if (Utility.isVar(opt)) {
                // 变量赋值
                Register srcReg = Flow.applyReadReg(opt, this);
                Register dstReg = Flow.applyWriteReg(dst,this);

                MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                        dstReg,srcReg);
            }
            else {
                // 常数赋值
                Register dstReg = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                        dstReg,opt);
            }
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        if (Utility.isVar(opt)) {
            // 变量赋值
            String belong = super.getBelong();
            Register srcReg = RegAlloc.applyReg(opt,this);
            Register dstReg = RegAlloc.writeReg(dst,this);

            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                    dstReg,srcReg);

            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
        else {
            // 常数赋值
            String belong = super.getBelong();
            Register dstReg = RegAlloc.writeReg(dst,this);

            MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                    dstReg,opt);

            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
    }

    public String getOpt() {
        return opt;
    }

    public String getDst() {
        return dst;
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
        // 赋值可以直接删
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public boolean canConvert2Assign() {
        // 已经是赋值语句了
        return false;
    }

    @Override
    public String convert2Assign() {
        return null;
    }

    @Override
    public void copySpreadRename(String src, String dst) {
        if (opt.equals(src)) {
            opt = dst;
        }
    }

    @Override
    public String getCopySpreadLeftValue() {
        return null;
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
