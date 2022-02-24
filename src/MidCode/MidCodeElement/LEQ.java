package MidCode.MidCodeElement;

import Macro.Macro;
import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class LEQ extends MidCode
        implements ABSTRACT_DEF, ABSTRACT_USE {
    private String opt1;
    private String opt2;
    private String dst;

    public LEQ(String opt1, String opt2, String dst) {
        super();
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = (%s <= %s)\r\n",dst,opt1,opt2);
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
                // 常数 <= 常数
                int num1 = Integer.valueOf(opt1);
                int num2 = Integer.valueOf(opt2);
                int out = (num1 <= num2)? 1: 0;
                Register dstReg = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                        dstReg,String.valueOf(out));
            }
            else {
                // 顶层抽象
                Register r1 = Flow.applyReadReg(opt1,this);
                Register r2 = Flow.applyReadReg(opt2,this);
                Register rDst = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("sgt %s, %s, %s\r\n",rDst,r1,r2);
                MipsFactory.mipsCode += String.format("xori %s, %s, %s\r\n",rDst,rDst,1);
            }
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 常数 <= 常数
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int out = (num1 <= num2)? 1: 0;
            Register dstReg = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                    dstReg,String.valueOf(out));
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
        else {
            // 顶层抽象
            Register r1 = RegAlloc.applyReg(opt1,this);
            Register r2 = RegAlloc.applyReg(opt2,this);
            Register rDst = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("sgt %s, %s, %s\r\n",rDst,r1,r2);
            MipsFactory.mipsCode += String.format("xori %s, %s, %s\r\n",rDst,rDst,1);
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
    }

    @Override
    public String createMips() {
        // <= 转化成 异或 >
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 全是数字
            String retStr = "";
            String belong = super.getBelong();
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int out = (num1 <= num2)? 1: 0;
            retStr += String.format("li %s, %s\r\n", Register.t0,String.valueOf(out));
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else if (!Utility.isVar(opt1) && Utility.isVar(opt2)) {
            // opt1是数字 opt2是变量
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("li %s, %s\r\n",
                    Register.t0,
                    opt1);
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(opt2,belong),
                    Utility.getPointerReg(opt2,belong));
            retStr += String.format("sgt %s, %s, %s\r\n",Register.t2,Register.t0,Register.t1);
            retStr += String.format("xori %s, %s, %s\r\n",Register.t2,Register.t2,1);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t2,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // opt1是变量 opt2是数字
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt1,belong),
                    Utility.getPointerReg(opt1,belong));
            retStr += String.format("li %s, %s\r\n",
                    Register.t1,
                    opt2);
            retStr += String.format("sgt %s, %s, %s\r\n",Register.t2,Register.t0,Register.t1);
            retStr += String.format("xori %s, %s, %s\r\n",Register.t2,Register.t2,1);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t2,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else {
            // 都是变量
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
            retStr += String.format("sgt %s, %s, %s\r\n",Register.t2,Register.t0,Register.t1);
            retStr += String.format("xori %s, %s, %s\r\n",Register.t2,Register.t2,1);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t2,
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
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public boolean canConvert2Assign() {
        return (!Utility.isVar(opt1) && !Utility.isVar(opt2));
    }

    @Override
    public String convert2Assign() {
        if (canConvert2Assign()) {
            int optNum1 = Integer.valueOf(opt1);
            int optNum2 = Integer.valueOf(opt2);
            int leq = (optNum1 <= optNum2)? 1: 0;
            return String.valueOf(leq);
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
