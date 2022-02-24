import Macro.Macro;
import MidCode.MidCodeElement.*;

import java.util.ArrayList;
import java.util.HashSet;

import Optimizer.ControlFlowAnalysis.Flow;

import MidCode.Utility;

public class Test {

    public static void main(String[] args) {
        boolean test1 = false;
        boolean test2 = false;
        boolean test3 = false;
        boolean test4 = false;
        boolean test5 = false;
        boolean test6 = false;
        boolean test7 = false;
        boolean test8 = false;
        boolean test9 = false;
        boolean test10 = true;
        if (test1) {
            long opt =
                    ((923l * (long) 3435973837l) / 4294967296l) / (long) 8l;
            long opt_no = 923 / 10;
            System.out.println(opt);
            System.out.println(opt_no);
        }
        if (test2) {
            ArrayList<MidCode> midCodes = new ArrayList<>();

            String label_1 = "B1";
            String label_2 = "B2";
            String label_3 = "B3";
            String label_4 = "B4";
            String label_5 = "B5";

            MidCode midCode;

            midCode = new SUPPLEMENT_LABEL(label_1);
            midCodes.add(midCode);
            midCode = new ASSIGN("@a","@x");
            midCodes.add(midCode);
            midCode = new ASSIGN("@b","@y");
            midCodes.add(midCode);
            midCode = new ASSIGN("0","@i");
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(label_2);
            midCodes.add(midCode);
            midCode = new BEZ("@i",Macro.EXIT_BLOCK_NAME);
            ((BEZ) midCode).setDst_1(label_3);
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(label_3);
            midCodes.add(midCode);
            midCode = new ASSIGN("@a","@z");
            midCodes.add(midCode);
            midCode = new ADD("@x","@y","@x");
            midCodes.add(midCode);
            midCode = new CMP("@x","@z",label_5);
            ((CMP) midCode).setDst_1(label_4);
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(label_4);
            midCodes.add(midCode);
            midCode = new SUB("@x","@y","@x");
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(label_5);
            midCodes.add(midCode);
            midCode = new ADD("@y","1","@y");
            midCodes.add(midCode);
            midCode = new ADD("@i","1","@i");
            midCodes.add(midCode);
            midCode = new GOTO(label_2);
            midCodes.add(midCode);


            for (MidCode midCode1:midCodes) {
                midCode1.setBelong("main");
            }

            Flow.createFlow(midCodes);
            Flow.calculateSuccessors();
            Flow.calculatePrecursors();
            Flow.useDefAnalyze();
            Flow.activeAnalyze();
            Flow.killGenAnalyze();
            Flow.arriveDefAnalyze();
            System.out.println(Flow.inOut2Str());
        }
        if (test3) {
            ArrayList<MidCode> midCodes = new ArrayList<>();

            String B1 = "B1";
            String B2 = "B2";
            String B3 = "B3";
            String B4 = "B4";
            String B5 = "B5";
            String B6 = "B6";

            MidCode midCode;
            midCode = new SUPPLEMENT_LABEL(B1);
            midCodes.add(midCode);
            midCode = new ASSIGN("0","@a");
            midCodes.add(midCode);
            midCode = new ASSIGN("0","@i");
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(B2);
            midCodes.add(midCode);
            midCode = new BEZ("@i",B4);
            ((BEZ) midCode).setDst_1(B3);
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(B3);
            midCodes.add(midCode);
            midCode = new MULT("@a","@i","@a");
            midCodes.add(midCode);
            midCode = new ADD("@i","1","@i");
            midCodes.add(midCode);
            midCode = new GOTO(B2);
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(B4);
            midCodes.add(midCode);
            midCode = new ASSIGN("@a","@b");
            midCodes.add(midCode);
            midCode = new ASSIGN("0","@i");
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(B5);
            midCodes.add(midCode);
            midCode = new BEZ("@i",Macro.EXIT_BLOCK_NAME);
            ((BEZ) midCode).setDst_1(B6);
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(B6);
            midCodes.add(midCode);
            midCode = new ADD("@b","@i","@b");
            midCodes.add(midCode);
            midCode = new ADD("@i","1","@i");
            midCodes.add(midCode);
            midCode = new GOTO(B5);
            midCodes.add(midCode);

            for (MidCode midCode1:midCodes) {
                midCode1.setBelong("main");
            }

            Flow.createFlow(midCodes);
            Flow.calculateSuccessors();
            Flow.calculatePrecursors();
            Flow.useDefAnalyze();
            Flow.activeAnalyze();
            Flow.killGenAnalyze();
            Flow.arriveDefAnalyze();
            Flow.calculateAllUses();
            Flow.defUseChainAnalyze();
            Flow.defUseWebAnalyze();
            // 分完web后对变量再次重命名
            // Flow.webRename();
            // 重新做活跃变量分析
            // Flow.useDefAnalyze();
            // Flow.activeAnalyze();

            System.out.println(Flow.web2String());
        }
        if (test4) {
            ArrayList<Integer> a = new ArrayList<>();
            ArrayList<Integer> b = new ArrayList<>();
            a.add(2);
            a.add(3);
            b.add(2);
            b.add(2);
            a.addAll(b);
            System.out.println(a);
        }
        if (test5) {
            ArrayList<MidCode> midCodes = new ArrayList<>();

            MidCode midCode;
            midCode = new ADD("@a","@b","#T1");
            midCodes.add(midCode);
            midCode = new ADD("#T1","@c","#T2");
            midCodes.add(midCode);
            midCode = new ADD("#T1","@d","#T3");
            midCodes.add(midCode);
            midCode = new ADD("#T2","@e","#T4");
            midCodes.add(midCode);
            midCode = new ADD("#T3","#T4","#T5");
            midCodes.add(midCode);
            // 测试寄存器的替换
            midCode = new ADD("#T5","#T4","#T6");
            midCodes.add(midCode);
            midCode = new ADD("#T1","#T2","#T7");
            midCodes.add(midCode);
            midCode = new ADD("#T1","#T2","#T8");
            midCodes.add(midCode);
            midCode = new ADD("#T1","#T2","#T9");
            midCodes.add(midCode);
            midCode = new ADD("#T1","#T2","#T10");
            midCodes.add(midCode);
            midCode = new ADD("#T1","#T2","#T11");
            midCodes.add(midCode);
            midCode = new ADD("#T1","#T2","#T12");
            midCodes.add(midCode);

            for (MidCode midCode1:midCodes) {
                midCode1.setBelong("main");
            }

            Flow.createFlow(midCodes);
            Flow.innerTempUseDefAnalyze();
            System.out.println("不活跃");
            System.out.println(Flow.inactive2Str());
            System.out.println("活跃");
            System.out.println(Flow.activeTemp2Str());
        }
        if (test6) {
            ArrayList<MidCode> midCodes = new ArrayList<>();

            String B1 = "B1";
            String B2 = "B2";
            String B3 = "B3";
            String B4 = "B4";
            String B5 = "B5";
            String B6 = "B6";

            MidCode midCode;
            midCode = new SUPPLEMENT_LABEL(B1);
            midCodes.add(midCode);
            midCode = new ASSIGN("1","@b");
            midCodes.add(midCode);
            midCode = new ASSIGN("@b","@a");
            midCodes.add(midCode);
            midCode = new BEZ("@a",B3);
            ((BEZ) midCode).setDst_1(B2);
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(B2);
            midCodes.add(midCode);
            midCode = new ADD("@a","1","@a");
            midCodes.add(midCode);
            midCode = new ADD("@a","2","@a");
            midCodes.add(midCode);
            midCode = new ADD("@a","3","@a");
            midCodes.add(midCode);

            midCode = new SUPPLEMENT_LABEL(B3);
            midCodes.add(midCode);
            midCode = new MULT("@a","1","@m");
            midCodes.add(midCode);

            for (MidCode midCode1:midCodes) {
                midCode1.setBelong("main");
            }

            Flow.createFlow(midCodes);
            Flow.calculateSuccessors();
            Flow.calculatePrecursors();
            Flow.useDefAnalyze();
            Flow.activeAnalyze();
            Flow.killGenAnalyze();
            Flow.arriveDefAnalyze();
            Flow.calculateAllUses();
            Flow.defUseChainAnalyze();
            Flow.defUseWebAnalyze();
            // 分完web后对变量再次重命名
            Flow.webRename();
            // 重新做活跃变量分析
            Flow.useDefAnalyze();
            Flow.activeAnalyze();

            System.out.println(Flow.inOut2Str());
        }
        if (test7) {
            int n = 15;
            int power = -1;
            boolean is2Power = (n & (n - 1)) == 0;
            while (n != 0) {
                n >>= 1;
                power++;
            }
            if (!is2Power) {
                power++;
            }
            System.out.println(power);
        }
        if (test8) {
            int d = 204;
            int N = 32;
            int prec = N-1;
            int l = log2Ceil(d);
            int shpost = l;
            long mLow = (1 << (N+l)) / d;
            long mHigh = (1<<(N+l) + 1<<(N+l-prec)) / d;
            while ((mLow/2 < mHigh/2) && (shpost>0)) {
                mLow = mLow/2;
                mHigh = mHigh/2;
                shpost = shpost - 1;
            }
        }
        if (test9) {
            System.out.println(log2(1));
        }
        if (test10) {
            HashSet<Integer> a = new HashSet<>();
            HashSet<Integer> b = new HashSet<>();
            a.add(1);
            a.add(2);
            b.add(3);
            b.add(4);
            a.retainAll(b);
            System.out.println(a);
        }
    }

    public static int log2Ceil(int n) {
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

    public static int log2(int n) {
        int power = -1;
        while (n != 0) {
            n >>= 1;
            power++;
        }
        return power;
    }
}

class CMP extends MidCode implements ABSTRACT_JUMP, ABSTRACT_USE {

    private String opt1;
    private String opt2;
    private String label;

    public CMP(String opt1,String opt2,String label) {
        super();
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("cmp %s, %s, %s\r\n",opt1,opt2,label);
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
        if (Utility.isVar(opt1)) {
            retSet.add(opt1);
        }
        if (Utility.isVar(opt2)) {
            retSet.add(opt2);
        }
        return retSet;
    }

    @Override
    public void renameUse(String src, String suffix) {
        if (opt1.equals(src)) {
            opt1 = opt1 + suffix;
        }
        if (opt2.equals(src)) {
            opt2 = opt2 + suffix;
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

    }

    @Override
    public String getCopySpreadLeftValue() {
        return null;
    }
}