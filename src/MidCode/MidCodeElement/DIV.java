package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.DivOpt;
import Optimizer.OptimizerSwitch;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;

import MidCode.MidCodeFactory;

public class DIV extends MidCode
        implements ABSTRACT_DEF, ABSTRACT_USE {
    private String opt1;
    private String opt2;
    private String dst;

    private long m; // 相乘系数
    private int k; // 向右移位数

    public DIV(String opt1, String opt2, String dst) {
        super();
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = %s / %s\r\n",dst,opt1,opt2);
    }

    @Override
    public String createMips() {
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 全是数字
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int sum = num1 / num2;
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
            // 常数 / 变量
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt2,belong),
                    Utility.getPointerReg(opt2,belong));
            // 先把常数加载到寄存器
            retStr += String.format("li %s, %s\r\n",
                    Register.t2,
                    opt1);
            retStr += String.format("div %s, %s\r\n",Register.t2,Register.t0);
            retStr += String.format("mflo %s\r\n",Register.t1);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 变量 / 常数
            if (OptimizerSwitch.DIV_OPT) {
                return DivOpt.optimizer(this);
            }
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(opt1,belong),
                    Utility.getPointerReg(opt1,belong));
            // 把常数加载进寄存器
            retStr += String.format("li %s, %s\r\n",Register.t2,opt2);
            retStr += String.format("div %s, %s\r\n",Register.t0,Register.t2);
            retStr += String.format("mflo %s\r\n",Register.t1);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else {
            // 变量 / 变量
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
            retStr += String.format("div %s, %s\r\n",Register.t0,Register.t1);
            retStr += String.format("mflo %s\r\n",Register.t2);
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
                // 常数 / 常数
                int num1 = Integer.valueOf(opt1);
                int num2 = Integer.valueOf(opt2);
                int div = num1 / num2;
                Register dstReg = Flow.applyWriteReg(dst, this);
                MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                        dstReg,String.valueOf(div));
            }
            else {
                if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
                    // 变量 / 常数
                    // 魔数优化
                    if (OptimizerSwitch.DIV_OPT) {
                        int num = Integer.valueOf(opt2);
                        if (num <= OptimizerSwitch.DIV_MOD_UP_BOUND
                        && num >= OptimizerSwitch.DIV_MOD_DOWN_BOUND) {
                            div_optimizer();
                            return;
                        }
                    }
                }
                // 顶层抽象
                Register r1 = Flow.applyReadReg(opt1,this);
                Register r2 = Flow.applyReadReg(opt2,this);
                Register rDst = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("div %s, %s\r\n",r1,r2);
                MipsFactory.mipsCode += String.format("mflo %s\r\n",rDst);
            }
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        if (!Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 常数 / 常数
            int num1 = Integer.valueOf(opt1);
            int num2 = Integer.valueOf(opt2);
            int sum = num1 / num2;
            Register dstReg = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                    dstReg,String.valueOf(sum));
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
        }
        else {
            if (Utility.isVar(opt1) && !Utility.isVar(opt2) && OptimizerSwitch.DIV_OPT) {
                // 变量 / 常数 优化为除法优化特判
                DivOpt.optimizerOpt(this);
                return;
            }
            // 顶层抽象
            Register r1 = RegAlloc.applyReg(opt1,this);
            Register r2 = RegAlloc.applyReg(opt2,this);
            Register rDst = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("div %s, %s\r\n",r1,r2);
            MipsFactory.mipsCode += String.format("mflo %s\r\n",rDst);
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
        if (opt2.equals("1")) {
            return true;
        }
        if (opt1.equals("0")) {
            return true;
        }
        return (!Utility.isVar(opt1) && !Utility.isVar(opt2));
    }

    @Override
    public String convert2Assign() {
        if (canConvert2Assign()) {
            if (opt2.equals("1")) {
                return opt1;
            }
            if (opt1.equals("0")) {
                return "0";
            }
            int optNum1 = Integer.valueOf(opt1);
            int optNum2 = Integer.valueOf(opt2);
            int div = optNum1 / optNum2;
            return String.valueOf(div);
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

    public boolean is2Power(int n) {
        // 是否是2的幂
        return (n & (n - 1)) == 0;
    }

    public int log2(int n) {
        int power = -1;
        while (n != 0) {
            n >>= 1;
            power++;
        }
        return power;
    }

    public int log2Ceil(int n) {
        // log2 向上取整
        int power = -1;
        boolean is2Power = (n & (n - 1)) == 0;
        while (n != 0) {
            n >>= 1;
            power++;
        }
        if (!is2Power) {
            power++;
        }
        return power;
    }

    int N = 32;
    int l;
    int shpost;
    long mLow;
    long mHigh;

    public void calculateMagicCorrect(int d) {

        int prec = N-1;
        l = log2Ceil(d);
        shpost = l;
        // 注意到可能会有long的溢出
        BigInteger temp = BigInteger.valueOf(1L).shiftLeft(N+l).divide(BigInteger.valueOf(d));
        // mLow = (1L << (N+l)) / (long)d;
        mLow = temp.longValue();
        BigInteger temp1 = BigInteger.valueOf(1L).shiftLeft(N+l);
        BigInteger temp2 = BigInteger.valueOf(1L).shiftLeft(N+l-prec);
        BigInteger temp3 = temp1.add(temp2).divide(BigInteger.valueOf(d));
        // mHigh = ((1L<<(long)(N+l)) + (1L<<(long)(N+l-prec))) / (long)d;
        mHigh = temp3.longValue();

        while ((mLow/2 < mHigh/2) && (shpost>0)) {
            mLow = mLow/2;
            mHigh = mHigh/2;
            shpost = shpost - 1;
        }
    }

    public void calculateMagic(int c) {

        // 计算魔数
        // 先找一个正整数k 令2^k < c < 2^{k+1}
        k = log2(c);
        // 符号数 所以是31

        long floor_m = ((long)1 << (long)(k+31)) / (long)c;
        long ceil_m = floor_m + 1;
        BigDecimal real =
                BigDecimal.valueOf((long)1 << (long)(k+31)).divide(BigDecimal.valueOf(c),
                        100,BigDecimal.ROUND_HALF_UP);
        BigDecimal floor_e = real.subtract(BigDecimal.valueOf(floor_m));

        BigDecimal ceil_e = BigDecimal.valueOf(1).subtract(floor_e);
        BigDecimal std = BigDecimal.valueOf(1 << k).divide(BigDecimal.valueOf(c),
                100,BigDecimal.ROUND_HALF_UP);

        // 优先向上取整
        if (std.compareTo(ceil_e) < 0) {
            m = ceil_m;
        }
        else {
            m = floor_m;
        }

        // System.out.println(String.format("0x%x",m));
    }

    private void div_optimizer() {
        MipsFactory.mipsCode += "# ##########BEGIN_DIV_OPT###########\r\n";
        int int_opt2 = Integer.valueOf(opt2);
        boolean isMinus = (int_opt2 < 0);
        if (int_opt2 < 0) {int_opt2 = - int_opt2;}
        calculateMagicCorrect(int_opt2);

        long edge = (1L << (N-1));
        if (int_opt2 == 1) {
            if (isMinus) {
                // -1
                Register optReg = Flow.applyReadReg(opt1,this);
                Register dstReg = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("neg %s, %s\r\n",
                        dstReg, optReg);
            }
            else {
                Register optReg = Flow.applyReadReg(opt1, this);
                Register dstReg = Flow.applyWriteReg(dst, this);
                MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                        dstReg, optReg);
            }
        }
        else if (is2Power(int_opt2)) {
            Register optReg = Flow.applyReadReg(opt1,this);
            Register dstReg = Flow.applyWriteReg(dst,this);
            MipsFactory.mipsCode += String.format(
                    "sra %s, %s, %s\r\n",
                    dstReg,
                    optReg,
                    String.valueOf(l-1)
            );
            MipsFactory.mipsCode += String.format(
                    "srl %s, %s, %s\r\n",
                    dstReg,
                    dstReg,
                    String.valueOf(N-l)
            );
            MipsFactory.mipsCode += String.format(
                    "addu %s, %s, %s\r\n",
                    dstReg,
                    optReg,
                    dstReg
            );
            MipsFactory.mipsCode += String.format(
                    "sra %s, %s, %s\r\n",
                    dstReg,
                    dstReg,
                    String.valueOf(l)
            );
            if (isMinus) {
                MipsFactory.mipsCode += String.format(
                        "neg %s, %s\r\n",
                        dstReg,
                        dstReg
                );
            }
        }
        else if (mHigh < edge) {

            Register optReg = Flow.applyReadReg(opt1,this);
            Register dstReg = Flow.applyWriteReg(dst,this);
            Register borrow = Flow.borrowReg(this);

            MipsFactory.mipsCode += String.format(
                    "li %s, %s\r\n",
                    dstReg,
                    String.valueOf(mHigh)
            );
            MipsFactory.mipsCode += String.format(
                    "mult %s, %s\r\n",
                    optReg,
                    dstReg
            );
            MipsFactory.mipsCode += String.format(
                    "mfhi %s\r\n",
                    dstReg
            );
            MipsFactory.mipsCode += String.format(
                    "sra %s, %s, %s\r\n",
                    dstReg,
                    dstReg,
                    String.valueOf(shpost)
            );
            MipsFactory.mipsCode += String.format(
                    "srl %s, %s, %s\r\n",
                    borrow,
                    optReg,
                    String.valueOf(31)
            );
            MipsFactory.mipsCode += String.format(
                    "addu %s, %s, %s\r\n",
                    dstReg,
                    dstReg,
                    borrow
            );
            if (isMinus) {
                MipsFactory.mipsCode += String.format(
                        "neg %s, %s\r\n",
                        dstReg,
                        dstReg
                );
            }
            Flow.returnReg(borrow,this);
            Flow.clearBorrow(this);
        }
        else {

            Register optReg = Flow.applyReadReg(opt1,this);
            Register dstReg = Flow.applyWriteReg(dst,this);
            Register borrow = Flow.borrowReg(this);
            MipsFactory.mipsCode += String.format(
                    "li %s, %s\r\n",
                    dstReg,
                    String.valueOf(mHigh-(1L<<N))
            );
            MipsFactory.mipsCode += String.format(
                    "mult %s, %s\r\n",
                    dstReg,
                    optReg
            );
            MipsFactory.mipsCode += String.format(
                    "mfhi %s\r\n",
                    dstReg
            );
            MipsFactory.mipsCode += String.format(
                    "addu %s, %s, %s\r\n",
                    dstReg,
                    optReg,
                    dstReg
            );
            MipsFactory.mipsCode += String.format(
                    "sra %s, %s, %s\r\n",
                    dstReg,
                    dstReg,
                    String.valueOf(shpost)
            );
            MipsFactory.mipsCode += String.format(
                    "srl %s, %s, %s\r\n",
                    borrow,
                    optReg,
                    String.valueOf(31)
            );
            MipsFactory.mipsCode += String.format(
                    "addu %s, %s, %s\r\n",
                    dstReg,
                    dstReg,
                    borrow
            );
            if (isMinus) {
                MipsFactory.mipsCode += String.format(
                        "neg %s, %s\r\n",
                        dstReg,
                        dstReg
                );
            }
            Flow.returnReg(borrow,this);
            Flow.clearBorrow(this);
        }
        MipsFactory.mipsCode += "# ##########END_DIV_OPT###########\r\n";
    }


    private void div_optimizer_wrong() {
        MipsFactory.mipsCode += "# ##########BEGIN_DIV_OPT###########\r\n";
        int int_opt2 = Integer.valueOf(opt2);
        boolean isMinus = (int_opt2 < 0);
        // 绝对值
        if (int_opt2 < 0) {int_opt2 = - int_opt2;}
        if (is2Power(int_opt2)) {
            if (int_opt2 == 1) {
                if (!isMinus) {
                    Register optReg = Flow.applyReadReg(opt1, this);
                    Register dstReg = Flow.applyWriteReg(dst, this);
                    MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                            dstReg, optReg);
                }
                else {
                    Register optReg = Flow.applyReadReg(opt1, this);
                    Register dstReg = Flow.applyWriteReg(dst, this);
                    MipsFactory.mipsCode += String.format("neg %s, %s\r\n",
                            dstReg, optReg);
                }
            }
            else {
                // 2的幂的其他情况
                int bits = log2(int_opt2);
                Register optReg = Flow.applyReadReg(opt1,this);
                Register dstReg = Flow.applyWriteReg(dst,this);

                // dstReg可以中间过程用
                // 至少借2个寄存器
                Register borrow_1 = Flow.borrowReg(this);
                Register borrow_2 = Flow.borrowReg(this);

                MipsFactory.mipsCode += String.format("srl %s, %s, %s\r\n",
                        dstReg,
                        optReg,
                        31);
                MipsFactory.mipsCode += String.format("sra %s, %s, %s\r\n",
                        borrow_1,
                        optReg,
                        String.valueOf(bits));
                // 负数作被除数
                // 负数算数右移 向下取整
                // 所以如果不整除 则需要+1
                MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                        borrow_2,
                        borrow_1,
                        String.valueOf(bits));
                String label = MidCodeFactory.createAutoLabel();
                MipsFactory.mipsCode += String.format("beq %s, %s, %s\r\n",
                        optReg,
                        borrow_2,
                        label);
                // 不整除 负数时 +1
                MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                        borrow_1,
                        borrow_1,
                        dstReg);
                // 整除跳过
                MipsFactory.mipsCode += String.format("%s:\r\n",label);
                MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                        dstReg,
                        borrow_1);

                // 归还临时寄存器
                Flow.returnReg(borrow_1,this);
                Flow.returnReg(borrow_2,this);
                // 清空借条 释放占用资源
                Flow.clearBorrow(this);

                if (isMinus) {
                    MipsFactory.mipsCode += String.format("neg %s, %s\r\n",
                            dstReg,
                            dstReg);
                }
            }
        }
        else {
            // 不是2的幂
            calculateMagic(int_opt2);
            Register optReg = Flow.applyReadReg(opt1,this);
            Register dstReg = Flow.applyWriteReg(dst,this);

            // 至少借2个寄存器
            Register borrow_1 = Flow.borrowReg(this);
            Register borrow_2 = Flow.borrowReg(this);

            MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                    dstReg,
                    String.format("0x%x",m));
            MipsFactory.mipsCode += String.format("mult %s, %s\r\n",
                    optReg,
                    dstReg);
            MipsFactory.mipsCode += String.format("mfhi %s\r\n",
                    dstReg);

            MipsFactory.mipsCode += String.format("sra %s, %s, %s\r\n",
                    borrow_1,
                    dstReg,
                    String.valueOf(k-1));
            MipsFactory.mipsCode += String.format("srl %s, %s, %s\r\n",
                    borrow_2,
                    borrow_1,
                    31);
            MipsFactory.mipsCode += String.format("add %s, %s, %s\r\n",
                    dstReg,
                    borrow_2,
                    borrow_1);
            // 归还寄存器
            Flow.returnReg(borrow_1,this);
            Flow.returnReg(borrow_2,this);
            // 释放
            Flow.clearBorrow(this);
            if (isMinus) {
                MipsFactory.mipsCode += String.format("neg %s, %s\r\n",
                        dstReg,
                        dstReg);
            }
        }
        MipsFactory.mipsCode += "# ##########END_DIV_OPT###########\r\n";
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
