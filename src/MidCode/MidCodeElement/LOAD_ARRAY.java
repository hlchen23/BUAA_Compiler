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

public class LOAD_ARRAY extends MidCode
        implements ABSTRACT_DEF, ABSTRACT_USE {

    private String src;
    private String index;
    private String dst;

    public LOAD_ARRAY(String src, String index, String dst) {
        super();
        this.src = src;
        this.index = index;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = %s[%s]\r\n",dst,src,index);
    }

    @Override
    public String createMips() {
        // src dst均不能是常数 index可能是常数
        if (Utility.isVar(index)) {
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
            // 从index计算偏移
            retStr += String.format("sll %s, %s, %s\r\n",
                    Register.t2,
                    Register.t1,
                    2);
            retStr += String.format("addu %s, %s, %s\r\n",
                    Register.t3,
                    Register.t0,
                    Register.t2);
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t4,
                    0,
                    Register.t3);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t4,
                    Utility.getOffset(dst,belong),
                    Utility.getPointerReg(dst,belong));
            return retStr;
        }
        else {
            String retStr = "";
            String belong = super.getBelong();
            // index是常数
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t0,
                    Utility.getOffset(src,belong),
                    Utility.getPointerReg(src,belong));
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.t1,
                    String.valueOf(offset),
                    Register.t0);
            retStr += String.format("sw %s, %s(%s)\r\n",
                    Register.t1,
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
            // src dst均不能是常数 index可能是常数
            if (Utility.isVar(index)) {
                Register srcReg = Flow.applyReadReg(src,this);
                Register indexReg = Flow.applyReadReg(index,this);
                Register dstReg = Flow.applyWriteReg(dst,this);
                Register temp = Flow.borrowReg(this);

                // 从index计算偏移
                MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                        temp,
                        indexReg,
                        2);
                MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                        temp,
                        srcReg,
                        temp);
                MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                        dstReg,
                        0,
                        temp);
                Flow.returnReg(temp,this);
                Flow.clearBorrow(this);
            }
            else {
                // index是常数
                int int_index = Integer.valueOf(index);
                int offset = int_index * 4;
                Register srcReg = Flow.applyReadReg(src,this);
                Register dstReg = Flow.applyWriteReg(dst,this);
                MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                        dstReg,
                        String.valueOf(offset),
                        srcReg);
            }
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        // src dst均不能是常数 index可能是常数
        if (Utility.isVar(index)) {
            Register srcReg = RegAlloc.applyReg(src,this);
            Register indexReg = RegAlloc.applyReg(index,this);
            Register dstReg = RegAlloc.writeReg(dst,this);

//            HashMap<Register,String> b = TempRegAlloc.borrowReg(1,this);
//            ArrayList<Register> arrayList = new ArrayList<>();
//            arrayList.addAll(b.keySet());

            //
            // 从index计算偏移
            MipsFactory.mipsCode += String.format("sll %s, %s, %s\r\n",
                    /*arrayList.get(0)*/dstReg,
                    indexReg,
                    2);
            MipsFactory.mipsCode += String.format("addu %s, %s, %s\r\n",
                    /*arrayList.get(0)*/dstReg,
                    srcReg,
                    /*arrayList.get(0)*/dstReg);
            MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                    dstReg,
                    0,
                    /*arrayList.get(0)*/dstReg);
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
//            TempRegAlloc.returnReg(b,this);
        }
        else {
            // index是常数
            int int_index = Integer.valueOf(index);
            int offset = int_index * 4;
            Register srcReg = RegAlloc.applyReg(src,this);
            Register dstReg = RegAlloc.writeReg(dst,this);
            MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                    dstReg,
                    String.valueOf(offset),
                    srcReg);
            if (!Utility.isTemp(dst)) {
                GlobalRegAlloc.writeBack(dst,this);
            }
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
            retSet.add(src);
            if (Utility.isVar(index)) {
                retSet.add(index);
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
        if (this.src.equals(src)) {
            if (arriveDefs.containsKey(this.src)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(this.src);
                arriveDefs.remove(this.src);
                arriveDefs.put(this.src + suffix,defs);
            }
            this.src = this.src + suffix;
        }
        if (index.equals(src)) {
            if (arriveDefs.containsKey(index)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(index);
                arriveDefs.remove(index);
                arriveDefs.put(index + suffix,defs);
            }
            index = index + suffix;
        }
    }

    @Override
    public void behaviorAfterDelete() {
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public boolean canConvert2Assign() {
        // 加载数组 不能
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


