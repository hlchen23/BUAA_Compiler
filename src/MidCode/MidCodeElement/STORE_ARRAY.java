package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class STORE_ARRAY extends MidCode
        implements ABSTRACT_USE {

    private String src;
    private String index;
    private String dst;

    public STORE_ARRAY(String src, String index, String dst) {
        super();
        this.src = src;
        this.index = index;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s[%s] = %s\r\n",dst,index,src);
    }

    @Override
    public String createMips() {
        // dst一定是变量 index可能是常量 src可能是常量
        if (!Utility.isVar(index) && !Utility.isVar(src)) {
            // 都是常量
            String retStr = "";
            String belong = super.getBelong();
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;
            retStr += String.format("li %s, %s\r\n", Register.t0,src);
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    String.valueOf(offset),
                    Register.t1);
            return retStr;
        }
        else if (Utility.isVar(index) && !Utility.isVar(src)) {
            // index是变量 src是常量
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("li %s, %s\r\n",
                    Register.t0,
                    src);
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(index,belong),
                    Utility.getPointerReg(index,belong));
            retStr += String.format("sll %s, %s, %s\r\n",
                    Register.t2,
                    Register.t1,
                    2);
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t3,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            retStr += String.format("addu %s, %s, %s\r\n",
                    Register.t4,
                    Register.t2,
                    Register.t3);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    0,
                    Register.t4);
            return retStr;
        }
        else if (!Utility.isVar(index) && Utility.isVar(src)) {
            // index是常量 src是变量
            String retStr = "";
            String belong = super.getBelong();
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(src,belong),
                    Utility.getPointerReg(src,belong));
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    String.valueOf(offset),
                    Register.t1);
            return retStr;
        }
        else {
            // 都是变量
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(src,belong),
                    Utility.getPointerReg(src,belong));
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t1,
                    Utility.getOffset(index,belong),
                    Utility.getPointerReg(index,belong));
            retStr += String.format("sll %s, %s, %s\r\n",
                    Register.t2,
                    Register.t1,
                    2);
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t3,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            retStr += String.format("addu %s, %s, %s\r\n",
                    Register.t4,
                    Register.t2,
                    Register.t3);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t0,
                    0,
                    Register.t4);
            return retStr;
        }
    }

    @Override
    public void createMipsOpt() {
        // dst一定是变量 index可能是常量 src可能是常量
        if (!Utility.isVar(index) && !Utility.isVar(src)) {
            // 都是常量
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;
            Register srcReg = Flow.applyReadReg(src,this);
            // 注意 取数组指针也是读 不是写
            Register dstReg = Flow.applyReadReg(dst,this);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    String.valueOf(offset),
                    dstReg);
        }
        else if (Utility.isVar(index) && !Utility.isVar(src)) {
            // index是变量 src是常量
            Register srcReg = Flow.applyReadReg(src,this);
            Register indexReg = Flow.applyReadReg(index,this);
            Register dstReg = Flow.applyReadReg(dst,this);

            Register regTemp = Flow.borrowReg(this);
            MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                    regTemp,
                    indexReg,
                    2);
            MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                    regTemp,
                    regTemp,
                    dstReg);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    0,
                    regTemp);
            Flow.returnReg(regTemp,this);
            Flow.clearBorrow(this);
        }
        else if (!Utility.isVar(index) && Utility.isVar(src)) {
            // index是常量 src是变量
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;

            Register srcReg = Flow.applyReadReg(src,this);
            Register dstReg = Flow.applyReadReg(dst,this);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    String.valueOf(offset),
                    dstReg);
        }
        else {
            // 都是变量
            Register srcReg = Flow.applyReadReg(src,this);
            Register indexReg = Flow.applyReadReg(index,this);
            Register dstReg = Flow.applyReadReg(dst,this);

            Register regTemp = Flow.borrowReg(this);
            MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                    regTemp,
                    indexReg,
                    2);
            MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                    regTemp,
                    regTemp,
                    dstReg);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    0,
                    regTemp);
            Flow.returnReg(regTemp,this);
            Flow.clearBorrow(this);
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        // dst一定是变量 index可能是常量 src可能是常量
        if (!Utility.isVar(index) && !Utility.isVar(src)) {
            // 都是常量
            String retStr = "";
            String belong = super.getBelong();
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;
            Register srcReg = RegAlloc.applyReg(src,this);
            // 注意 取数组指针也是读 不是写
            Register dstReg = RegAlloc.applyReg(dst,this);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    String.valueOf(offset),
                    dstReg);
        }
        else if (Utility.isVar(index) && !Utility.isVar(src)) {
            // index是变量 src是常量
            String retStr = "";
            String belong = super.getBelong();
            Register srcReg = RegAlloc.applyReg(src,this);
            Register indexReg = RegAlloc.applyReg(index,this);
            Register dstReg = RegAlloc.applyReg(dst,this);

            // TODO 这里有个bug indexReg被覆盖了 应该调用借寄存器方法
            MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                    indexReg,
                    indexReg,
                    2);
            MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                    indexReg,
                    indexReg,
                    dstReg);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    0,
                    indexReg);
        }
        else if (!Utility.isVar(index) && Utility.isVar(src)) {
            // index是常量 src是变量
            String retStr = "";
            String belong = super.getBelong();
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;

            Register srcReg = RegAlloc.applyReg(src,this);
            Register dstReg = RegAlloc.applyReg(dst,this);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    String.valueOf(offset),
                    dstReg);
        }
        else {
            // 都是变量
            String retStr = "";
            String belong = super.getBelong();

            Register srcReg = RegAlloc.applyReg(src,this);
            Register indexReg = RegAlloc.applyReg(index,this);
            Register dstReg = RegAlloc.applyReg(dst,this);

            MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                    indexReg,
                    indexReg,
                    2);
            MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                    indexReg,
                    indexReg,
                    dstReg);
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    srcReg,
                    0,
                    indexReg);
        }
    }

    @Override
    public HashSet<String> getUse() {
        HashSet<String> retSet = new HashSet<>();
        if (Utility.isVar(index)) {
            retSet.add(index);
        }
        if (Utility.isVar(src)) {
            retSet.add(src);
        }
        // 数组的首地址其实也是使用 并没有改变这个指针的值
        retSet.add(dst);
        return retSet;
    }

    @Override
    public void renameUse(String src, String suffix) {
        if (index.equals(src)) {
            if (arriveDefs.containsKey(index)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(index);
                arriveDefs.remove(index);
                arriveDefs.put(index + suffix,defs);
            }
            index = index + suffix;
        }
        if (this.src.equals(src)) {
            if (arriveDefs.containsKey(this.src)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(this.src);
                arriveDefs.remove(this.src);
                arriveDefs.put(this.src + suffix,defs);
            }
            this.src = this.src + suffix;
        }
        if (dst.equals(src)) {
            if (arriveDefs.containsKey(dst)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(dst);
                arriveDefs.remove(dst);
                arriveDefs.put(dst + suffix,defs);
            }
            dst = dst + suffix;
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
        if (this.src.equals(src)) {
            this.src = dst;
        }
        if (this.index.equals(src)) {
            this.index = dst;
        }
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }

    @Override
    public String getCopySpreadLeftValue() {
        return null;
    }
}
