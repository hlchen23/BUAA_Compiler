package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;


public class GETINT extends MidCode
        implements ABSTRACT_DEF {
    private String dst;

    public GETINT(String dst) {
        super();
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = getint()\r\n",dst);
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,5);
            MipsFactory.mipsCode += "syscall\r\n";
            Register dstReg = Flow.applyWriteReg(dst,this);
            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                    dstReg,
                    Register.v0);
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,5);
        MipsFactory.mipsCode += "syscall\r\n";
        Register dstReg = RegAlloc.writeReg(dst,this);
        MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                dstReg,
                Register.v0);
        if (!Utility.isTemp(dst)) {
            GlobalRegAlloc.writeBack(dst,this);
        }
    }

    @Override
    public String createMips() {
        String retStr = "";
        String belong = super.getBelong();
        retStr += String.format("li %s, %s\r\n", Register.v0,5);
        retStr += "syscall\r\n";
        retStr += String.format("sw %s, %s(%s)\r\n",
                Register.v0,
                Utility.getOffset(dst,belong),
                Utility.getPointerReg(dst,belong));
        return retStr;
    }

    public String getDst() {
        return dst;
    }

    @Override
    public String getDef() {
        return dst;
    }

    @Override
    public void renameDef(String src, String suffix) {
        if (dst.equals(src)) {
            dst = dst + suffix;
        }
    }

    @Override
    public void behaviorAfterDelete() {
        // getint要保留
        MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,5);
        MipsFactory.mipsCode += "syscall\r\n";
        MipsFactory.mipsCode += "# Dead assign has been deleted!\r\n";
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
