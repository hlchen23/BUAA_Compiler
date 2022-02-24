package MidCode.MidCodeElement;

import Macro.Macro;
import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.OptimizerSwitch;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class MULT extends MidCode
        implements ABSTRACT_DEF, ABSTRACT_USE {
    private String opt1;
    private String opt2;
    private String dst;

    public MULT(String opt1, String opt2, String dst) {
        super();
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = %s * %s\r\n",dst,opt1,opt2);
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
                // 常数 * 常数
                int num1 = Integer.valueOf(opt1);
                int num2 = Integer.valueOf(opt2);
                int out = num1 * num2;
                Register dstReg = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                        dstReg,String.valueOf(out));
            }
            else {
                // 乘法优化
                // 变量 * 常数 常数是2的幂
                if (OptimizerSwitch.MULT_OPT) {
                    if ((!Utility.isVar(opt1) && Utility.isVar(opt2))
                            || (Utility.isVar(opt1) && !Utility.isVar(opt2))) {
                        // 有一个是2的幂
                        int num;
                        Register read;
                        // 乘法交换律
                        if (!Utility.isVar(opt1) && Utility.isVar(opt2)) {
                            // opt1是常数 opt2是变量
                            num = Integer.valueOf(opt1);
                            read = Flow.applyReadReg(opt2,this);
                        }
                        else {
                            // opt1是变量 opt2是常数
                            num = Integer.valueOf(opt2);
                            read = Flow.applyReadReg(opt1,this);
                        }
                        Register dstReg = Flow.applyWriteReg(dst,this);
                        // 0与1的情况已经在中间代码常量合并解决了
                        int absNum = (num >= 0)? num: -num;
                        boolean isMinus = (num < 0);
                        if (absNum == 0) {
                            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                                    dstReg,Register.zero);
                            return;
                        }
                        else {
                            // >0
                            if ((absNum & (absNum-1)) == 0) {
                                // 2的幂次
                                int bits = log2(absNum);
                                // 与正负无关 直接移位
                                MipsFactory.mipsCode += "# ==========乘法移位优化===========\r\n";
                                MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                                        dstReg,
                                        read,
                                        String.valueOf(bits));
                                if (isMinus) {
                                    MipsFactory.mipsCode += String.format("neg %s, %s\r\n",
                                            dstReg,
                                            dstReg);
                                }
                                return;
                            }
                        }
                    }
                }

                // 顶层抽象
                Register r1 = Flow.applyReadReg(opt1,this);
                Register r2 = Flow.applyReadReg(opt2,this);
                Register rDst = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("mult %s, %s\r\n",r1,r2);
                MipsFactory.mipsCode += String.format("mflo %s\r\n",rDst);
            }
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 常数 * 常数
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int sum = num1 * num2;
            Register dstReg = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                    dstReg,String.valueOf(sum));
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
        else {
            // 顶层抽象
            Register r1 = RegAlloc.applyReg(opt1,this);
            Register r2 = RegAlloc.applyReg(opt2,this);
            Register rDst = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("mult %s, %s\r\n",r1,r2);
            MipsFactory.mipsCode += String.format("mflo %s\r\n",rDst);
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
    }

    @Override
    public String createMips() {
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 全是数字
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int sum = num1 * num2;
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
            // 把常量赋给寄存器
            retStr += String.format("li %s, %s\r\n",Register.t2,opt1);
            retStr += String.format("mult %s, %s\r\n",Register.t0,Register.t2);
            retStr += String.format("mflo %s\r\n",Register.t1);
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
            retStr += String.format("li %s, %s\r\n",Register.t2,opt2);
            retStr += String.format("mult %s, %s\r\n",Register.t0,Register.t2);
            retStr += String.format("mflo %s\r\n", Register.t1);
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
            retStr += String.format("mult %s, %s\r\n",Register.t0,Register.t1);
            retStr += String.format("mflo %s\r\n",Register.t2);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t2,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
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
        if (opt1.equals("1") || opt2.equals("1") || opt1.equals("0") || opt2.equals("0")) {
            return true;
        }
        return (!Utility.isVar(opt1) && !Utility.isVar(opt2));
    }

    @Override
    public String convert2Assign() {
        if (canConvert2Assign()) {
            if (opt1.equals("1")) {
                return opt2;
            }
            if (opt2.equals("1")) {
                return opt1;
            }
            if (opt1.equals("0")) {
                return "0";
            }
            if (opt2.equals("0")) {
                return "0";
            }
            int optNum1 = Integer.valueOf(opt1);
            int optNum2 = Integer.valueOf(opt2);
            int mult = optNum1 * optNum2;
            return String.valueOf(mult);
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

    public int log2(int n) {
        int power = -1;
        while (n != 0) {
            n >>= 1;
            power++;
        }
        return power;
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
