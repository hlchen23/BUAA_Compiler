package Optimizer;

import MidCode.MidCodeElement.DIV;
import MidCode.MidCodeFactory;
import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;
import Optimizer.RegisterAlloc.TempRegAlloc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;


public class DivOpt {

    private static long m; // 相乘系数
    private static int k; // 向右移位数

    public static void optimizerOpt(DIV divMidCode) {

        // 寄存器分配下的除法优化
        String opt1 = divMidCode.getOpt1();
        String opt2 = divMidCode.getOpt2();
        String dst = divMidCode.getDst();
        MipsFactory.mipsCode += "# ##########BEGIN_DIV_OPT###########\r\n";

        if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 变量 / 常数
            int int_opt2 = Integer.valueOf(opt2);
            boolean isMinus = (int_opt2 < 0);
            if (int_opt2 < 0) {int_opt2 = -int_opt2;}
            if (is2Power(int_opt2)) {
                if (int_opt2 == 1) {
                    Register srcReg = RegAlloc.applyReg(opt1,divMidCode);
                    Register dstReg = RegAlloc.writeReg(dst,divMidCode);
                    MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                            dstReg,srcReg);
                    if (!Utility.isTemp(dst)) {
                        GlobalRegAlloc.writeBack(dst,divMidCode);
                    }
                }
                else {
                    int bits = log2(int_opt2);
                    Register opt1Reg = RegAlloc.applyReg(opt1,divMidCode);
                    Register dstReg = RegAlloc.writeReg(dst,divMidCode);
                    // 借三个寄存器
                    HashMap<Register,String> borrows = TempRegAlloc.borrowReg(3,divMidCode);
                    ArrayList<Register> brList = new ArrayList<>();
                    brList.addAll(borrows.keySet());

                    // 被除数是负数时 要考虑符号位
                    MipsFactory.mipsCode += String.format("srl %s, %s, %s\r\n",
                            brList.get(1),
                            opt1Reg,
                            31);
                    MipsFactory.mipsCode += String.format("sra %s, %s, %s\r\n",
                            brList.get(0),
                            opt1Reg,
                            String.valueOf(bits));
                    // 负数作被除数
                    // 负数算数右移 向下取整
                    // 所以如果不整除 则需要+1
                    MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                            brList.get(2),
                            brList.get(0),
                            String.valueOf(bits));
                    String label = MidCodeFactory.createAutoLabel();
                    MipsFactory.mipsCode += String.format("beq %s, %s, %s\r\n",
                            opt1Reg,
                            brList.get(2),
                            label);
                    // 不整除 负数时 +1
                    MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                            brList.get(0),
                            brList.get(0),
                            brList.get(1));
                    // 整除 跳过
                    MipsFactory.mipsCode += String.format("%s:\r\n",label);
                    MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                            dstReg,
                            brList.get(0));

                    if (!Utility.isTemp(dst)) {
                        GlobalRegAlloc.writeBack(dst,divMidCode);
                    }
                    // 归还借用的寄存器
                    TempRegAlloc.returnReg(borrows,divMidCode);
                }
            }
            else {
                calculateMagic(int_opt2);
                Register opt1Reg = RegAlloc.applyReg(opt1,divMidCode);
                Register dstReg = RegAlloc.writeReg(dst,divMidCode);
                // 借4个寄存器
                HashMap<Register,String> borrows = TempRegAlloc.borrowReg(3,divMidCode);
                ArrayList<Register> brList = new ArrayList<>();
                brList.addAll(borrows.keySet());

                MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                        brList.get(1),
                        String.format("0x%x",m));
                MipsFactory.mipsCode += String.format("mult %s, %s\r\n",
                        opt1Reg,
                        brList.get(1));
                MipsFactory.mipsCode += String.format("mfhi %s\r\n",brList.get(0));

                MipsFactory.mipsCode += String.format("sra %s, %s, %s\r\n",
                        brList.get(1),
                        brList.get(0),
                        String.valueOf(k-1));
                MipsFactory.mipsCode += String.format("srl %s, %s, %s\r\n",
                        brList.get(2),
                        brList.get(1),
                        31);
                MipsFactory.mipsCode += String.format("add %s, %s, %s\r\n",
                        dstReg,
                        brList.get(2),
                        brList.get(1));
                if (!Utility.isTemp(dst)) {
                    GlobalRegAlloc.writeBack(dst,divMidCode);
                }
                // 归还借用的寄存器
                TempRegAlloc.returnReg(borrows,divMidCode);
            }

            if (isMinus) {
                // 再取负数
                Register dr = RegAlloc.applyReg(dst,divMidCode);
                Register dd = RegAlloc.writeReg(dst,divMidCode);

                MipsFactory.mipsCode += String.format("neg %s, %s\r\n",
                        dd,
                        dr);
                if (!Utility.isTemp(dst)) {
                    GlobalRegAlloc.writeBack(dst,divMidCode);
                }
            }
        }
        MipsFactory.mipsCode += "# ##########END_DIV_OPT###########\r\n";
    }

    public static String optimizer(DIV divMidCode) {
        String opt1 = divMidCode.getOpt1();
        String opt2 = divMidCode.getOpt2();
        String dst = divMidCode.getDst();
        String belong = divMidCode.getBelong();
        String retStr = "";
        retStr += "# ##########BEGIN_DIV_OPT###########\r\n";
        // 传入一条除法指令
        // 返回优化后的mips
        if (Utility.isVar(opt1) && !Utility.isVar(opt2)) {
            // 变量 / 常数
            int int_opt2 = Integer.valueOf(opt2);
            boolean isMinus = (int_opt2 < 0);
            if (int_opt2 < 0) {int_opt2 = -int_opt2;}
            if (is2Power(int_opt2)) {
                if (int_opt2 == 1) {
                    retStr += String.format("lw %s, %s(%s)\r\n",
                            Register.t0,
                            Utility.getOffset(opt1,belong),
                            Utility.getPointerReg(opt1,belong));
                    retStr += String.format("sw %s, %s(%s)\r\n",
                            Register.t0,
                            Utility.getOffset(dst,belong),
                            Utility.getPointerReg(dst,belong));
                }
                else {
                    int bits = log2(int_opt2);
                    retStr += String.format("lw %s, %s(%s)\r\n",
                            Register.t0,
                            Utility.getOffset(opt1,belong),
                            Utility.getPointerReg(opt1,belong));
                    // 被除数是负数时 要考虑符号位
                    retStr += String.format("srl %s, %s, %s\r\n",
                            Register.t2,
                            Register.t0,
                            31);
                    retStr += String.format("sra %s, %s, %s\r\n",
                            Register.t1,
                            Register.t0,
                            String.valueOf(bits));
                    // 负数作被除数
                    // 负数算数右移 向下取整
                    // 所以如果不整除 则需要+1
                    retStr += String.format("sll %s, %s, %s\r\n",
                            Register.t4,
                            Register.t1,
                            String.valueOf(bits));
                    String label = MidCodeFactory.createAutoLabel();
                    retStr += String.format("beq %s, %s, %s\r\n",
                            Register.t0,
                            Register.t4,
                            label);
                    // 不整除 +1
                    retStr += String.format("add %s, %s, %s\r\n",
                            Register.t1,
                            Register.t1,
                            Register.t2);
                    // 整除 跳过
                    retStr += String.format("%s:\r\n",label);
                    retStr += String.format("sw %s, %s(%s)\r\n",
                            Register.t1,
                            Utility.getOffset(dst,belong),
                            Utility.getPointerReg(dst,belong));
                }
            }
            else {
                calculateMagic(int_opt2);
                retStr += String.format("lw %s, %s(%s)\r\n",
                        Register.t0,
                        Utility.getOffset(opt1,belong),
                        Utility.getPointerReg(opt1,belong));
                retStr += String.format("li %s, %s\r\n",
                        Register.t3,
                        String.format("0x%x",m));
                retStr += String.format("mult %s, %s\r\n",
                        Register.t0,
                        Register.t3);
                retStr += String.format("mfhi %s\r\n",Register.t1);

                retStr += String.format("sra %s, %s, %s\r\n",
                        Register.t3,
                        Register.t1,
                        String.valueOf(k-1));
                retStr += String.format("srl %s, %s, %s\r\n",
                        Register.t4,
                        Register.t3,
                        31);

                retStr += String.format("add %s, %s, %s\r\n",
                        Register.t5,
                        Register.t4,
                        Register.t3);
                retStr += String.format("sw %s, %s(%s)\r\n",
                        Register.t5,
                        Utility.getOffset(dst,belong),
                        Utility.getPointerReg(dst,belong));
            }

            if (isMinus) {
                // 再取负数
                retStr += String.format("lw %s, %s(%s)\r\n",
                        Register.t0,
                        Utility.getOffset(dst,belong),
                        Utility.getPointerReg(dst,belong));
                retStr += String.format("neg %s, %s\r\n",
                        Register.t1,
                        Register.t0);
                retStr += String.format("sw %s, %s(%s)\r\n",
                        Register.t1,
                        Utility.getOffset(dst,belong),
                        Utility.getPointerReg(dst,belong));
            }
        }
        retStr += "# ##########END_DIV_OPT###########\r\n";
        return retStr;
    }

    public static boolean is2Power(int n) {
        return (n & (n - 1)) == 0;
    }

    public static int log2(int n) {
        int power = -1;
        while (n != 0) {
            n >>= 1;
            power++;
        }
        return power;
    }

    public static void calculateMagic(int c) {
        // 计算魔数
        // 相乘系数m 向右移动位数k
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

        // System.out.println(std);
        // System.out.println(floor_e);
        // System.out.println(ceil_e);
        // 向上或向下取整必然有一种满足
        if (ceil_e.compareTo(std) > 0) {
            m = floor_m;
        }
        else {
            m = ceil_m;
        }
        // System.out.println(c);
        // System.out.println(String.format("0x%x",m));
    }
}
