package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class ADD extends MidCode
        implements ABSTRACT_DEF, ABSTRACT_USE {
    private String opt1;
    private String opt2;
    private String dst;

    public ADD(String opt1, String opt2, String dst) {
        super();
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = %s + %s\r\n",dst,opt1,opt2);
    }

    @Override
    public String createMips() {
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 全是数字
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int sum = num1 + num2;
            String retStr = "";
            retStr += String.format("li %s, %s\r\n", Register.t0,String.valueOf(sum));
            String belong = super.getBelong();
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else if (!Utility.isVar(opt1) && Utility.isVar(opt2)) {
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt2,belong),
                    Utility.getPointerReg(opt2,belong));
            retStr += String.format("addiu %s, %s, %s\r\n",Register.t1,Register.t0,opt1);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt1,belong),
                    Utility.getPointerReg(opt1,belong));
            retStr += String.format("addiu %s, %s, %s\r\n",Register.t1,Register.t0,opt2);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else {
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt1,belong),
                    Utility.getPointerReg(opt1,belong));
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(opt2,belong),
                    Utility.getPointerReg(opt2,belong));
            retStr += String.format("addu %s, %s, %s\r\n",Register.t2,Register.t0,Register.t1);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t2,
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
            if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
                int num1 = Integer.valueOf(opt1);
                int num2 = Integer.valueOf(opt2);
                int sum = num1 + num2;
                Register dstReg = Flow.applyWriteReg(dst, this);
                MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                        dstReg, String.valueOf(sum));
            } else if (!Utility.isVar(opt1) && Utility.isVar(opt2)) {
                Register r2 = Flow.applyReadReg(opt2, this);
                Register rDst = Flow.applyWriteReg(dst, this);
                MipsFactory.mipsCode += String.format("addiu %s, %s, %s\r\n", rDst, r2, opt1);
            } else if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
                Register r1 = Flow.applyReadReg(opt1, this);
                Register rDst = Flow.applyWriteReg(dst, this);
                MipsFactory.mipsCode += String.format("addiu %s, %s, %s\r\n", rDst, r1, opt2);
            } else {
                Register r1 = Flow.applyReadReg(opt1, this);
                Register r2 = Flow.applyReadReg(opt2, this);
                Register rDst = Flow.applyWriteReg(dst, this);
                MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n", rDst, r1, r2);
            }
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 全是数字
            String belong = super.getBelong();
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int sum = num1 + num2;
            Register dstReg = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                    dstReg,String.valueOf(sum));
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
        else if (!Utility.isVar(opt1) && Utility.isVar(opt2)) {
            String belong = super.getBelong();
            Register r1 = RegAlloc.applyReg(opt2,this);
            Register rDst = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("addiu %s, %s, %s\r\n",rDst,r1,opt1);
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
        else if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            String belong = super.getBelong();
            Register r1 = RegAlloc.applyReg(opt1,this);
            Register rDst = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("addiu %s, %s, %s\r\n",rDst,r1,opt2);
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
        else {
            String belong = super.getBelong();
            Register r1 = RegAlloc.applyReg(opt1,this);
            Register r2 = RegAlloc.applyReg(opt2,this);
            Register rDst = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",rDst,r1,r2);
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
    }

    public String getOpt1() {
        return opt1;
    }

    public String getOpt2() {
        return opt2;
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
            // 如果被删了不再有任何定义
            return new HashSet<>();
        }
        else {
            HashSet<String> retSet = new HashSet<>();
            if (Utility.isVar(opt1)) {
                retSet.add(opt1);
            }
            if (Utility.isVar(opt2)) {
                retSet.add(opt2);
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
        if (opt1.equals(src)) {
            if (arriveDefs.containsKey(opt1)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(opt1);
                arriveDefs.remove(opt1);
                arriveDefs.put(opt1 + suffix,defs);
            }
            opt1 = opt1 + suffix;
        }
        if (opt2.equals(src)) {
            if (arriveDefs.containsKey(opt2)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(opt2);
                arriveDefs.remove(opt2);
                arriveDefs.put(opt2 + suffix,defs);
            }
            opt2 = opt2 + suffix;
        }
    }

    @Override
    public void behaviorAfterDelete() {
        // 加法直接删除
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public boolean canConvert2Assign() {
        // 0+ +0
        if (opt1.equals("0") || opt2.equals("0")) {
            return true;
        }
        return (!Utility.isVar(opt1) && !Utility.isVar(opt2));
    }

    @Override
    public String convert2Assign() {
        if (canConvert2Assign()) {
            // 含0
            if (opt1.equals("0")) {
                return opt2;
            }
            if (opt2.equals("0")) {
                return opt1;
            }
            // 其余情况
            int optNum1 = Integer.valueOf(opt1);
            int optNum2 = Integer.valueOf(opt2);
            int sum = optNum1 + optNum2;
            return String.valueOf(sum);
        }
        else {
            return null;
        }
    }

    @Override
    public void copySpreadRename(String src, String dst) {
        if (opt1.equals(src)) {
            opt1 = dst;
        }
        if (opt2.equals(src)) {
            opt2 = dst;
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
