package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import java.util.ArrayList;
import java.util.HashSet;

public class PRINT_INT extends MidCode
        implements ABSTRACT_USE, ABSTRACT_OUTPUT {
    private String dstInt;

    public PRINT_INT(String dstInt) {
        super();
        this.dstInt = dstInt;
    }

    @Override
    public String toString() {
        return String.format("print_int %s\r\n",dstInt);
    }

    @Override
    public String createMips() {
        if (Utility.isVar(dstInt)) {
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("lw %s, %s(%s)\r\n",
                    Register.a0,
                    Utility.getOffset(dstInt,belong),
                    Utility.getPointerReg(dstInt,belong));
            retStr += String.format("li %s, %s\r\n", Register.v0,1);
            retStr += "syscall\r\n";
            return retStr;
        }
        else {
            // 常数
            String retStr = "";
            String belong = super.getBelong();
            retStr += String.format("li %s, %s\r\n", Register.a0,dstInt);
            retStr += String.format("li %s, %s\r\n", Register.v0,1);
            retStr += "syscall\r\n";
            return retStr;
        }
    }

    @Override
    public void createMipsOpt() {
        if (Utility.isVar(dstInt)) {
            Register r = Flow.applyReadReg(dstInt,this);
            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                    Register.a0,
                    r);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,1);
            MipsFactory.mipsCode += "syscall\r\n";
        }
        else {
            // 常数
            MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.a0,dstInt);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,1);
            MipsFactory.mipsCode += "syscall\r\n";
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        if (Utility.isVar(dstInt)) {
            String belong = super.getBelong();

            Register r = RegAlloc.applyReg(dstInt,this);
            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                    Register.a0,
                    r);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,1);
            MipsFactory.mipsCode += "syscall\r\n";
        }
        else {
            // 常数
            String belong = super.getBelong();
            MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.a0,dstInt);
            MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,1);
            MipsFactory.mipsCode += "syscall\r\n";
        }
    }

    public String getDstInt() {
        return dstInt;
    }

    @Override
    public HashSet<String> getUse() {
        HashSet<String> retSet = new HashSet<>();
        if (Utility.isVar(dstInt)) {
            retSet.add(dstInt);
        }
        return retSet;
    }

    @Override
    public void renameUse(String src, String suffix) {
        if (dstInt.equals(src)) {
            if (arriveDefs.containsKey(dstInt)) {
                ArrayList<ABSTRACT_DEF> defs = arriveDefs.get(dstInt);
                arriveDefs.remove(dstInt);
                arriveDefs.put(dstInt + suffix,defs);
            }
            dstInt = dstInt + suffix;
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
        if (dstInt.equals(src)) {
            dstInt = dst;
        }
    }

    @Override
    public String getCopySpreadLeftValue() {
        return null;
    }
}
