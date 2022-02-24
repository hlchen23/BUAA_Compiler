package MidCode.MidCodeElement;

import Macro.Macro;
import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class RETURN extends MidCode
        implements ABSTRACT_JUMP, ABSTRACT_USE {

    private Type type;
    private String ret;

    // 记录行号在数据流检查报错用
    public int lineNo;

    public enum Type {
        INT,
        VOID
    }

    public RETURN(String ret) {
        super();
        this.ret = ret;
        type = Type.INT;
    }

    public RETURN() {
        super();
        type = Type.VOID;
    }

    @Override
    public String toString() {
        if (type == Type.INT) {
            return String.format("return %s\r\n",ret);
        }
        else {
            return String.format("return\r\n");
        }
    }

    @Override
    public void createMipsOpt() {
        if (type == Type.INT) {
            Register retReg = Flow.applyReadReg(ret, this);
            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                    Register.v0,
                    retReg);
        }
        MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                Register.ra,
                Utility.getRegOffset(Register.ra),
                Register.sp);

        Flow.saveBlockTail(this);

        MipsFactory.mipsCode += String.format("jr %s\r\n",Register.ra);
    }

    public void createMipsOpt_Old() {
        if (type == Type.INT) {
            Register retReg = RegAlloc.applyReg(ret,this);
            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                    Register.v0,
                    retReg);
        }
        MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                Register.ra,
                Utility.getRegOffset(Register.ra),
                Register.sp);
        MipsFactory.mipsCode += String.format("jr %s\r\n",Register.ra);
    }

    @Override
    public String createMips() {
        String retStr = "";
        String belong = super.getBelong();

        if (type == Type.INT) {
            if (Utility.isVar(ret)) {
                retStr += String.format("lw %s, %s(%s)\r\n",
                        Register.v0,
                        Utility.getOffset(ret, belong),
                        Utility.getPointerReg(ret,belong));
            }
            else {
                // 常数
                retStr += String.format("li %s, %s\r\n",
                        Register.v0,
                        ret);
            }
        }
        retStr += String.format("lw %s, %s(%s)\r\n",
                Register.ra,
                Utility.getRegOffset(Register.ra),
                Register.sp);
        retStr += String.format("jr %s\r\n",Register.ra);
        return retStr;
    }

    public Type getType() {
        return type;
    }

    public String getRet() {
        return ret;
    }

    @Override
    public int getDstNum() {
        return 1;
    }

    @Override
    public String getDst_1() {
        return Macro.EXIT_BLOCK_NAME;
    }

    @Override
    public String getDst_2() {
        return null;
    }

    @Override
    public void setDst_1(String dst_1) {

    }

    @Override
    public void setDst_2(String dst_2) {

    }

    @Override
    public HashSet<String> getUse() {
        if (isDelete) {
            // return语句被删除之后只有返回效果
            // 没有返回值
            return new HashSet<>();
        }
        else {
            HashSet<String> retSet = new HashSet<>();
            if (type == Type.INT) {
                if (Utility.isVar(ret)) {
                    retSet.add(ret);
                }
            }
            return retSet;
        }
    }

    @Override
    public void renameUse(String src, String suffix) {
        if (type == Type.INT) {
            if (ret.equals(src)) {
                if (arriveDefs.containsKey(ret)) {
                    ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(ret);
                    arriveDefs.remove(ret);
                    arriveDefs.put(ret + suffix,defs);
                }
                ret = ret + suffix;
            }
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
        if (type == Type.INT) {
            if (ret.equals(src)) {
                ret = dst;
            }
        }
    }

    @Override
    public String getCopySpreadLeftValue() {
        return null;
    }
}
