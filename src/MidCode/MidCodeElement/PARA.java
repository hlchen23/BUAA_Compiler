package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

public class PARA extends MidCode implements ABSTRACT_DEF {
    private String paraName;

    public PARA(String paraName) {
        super();
        this.paraName = paraName;
    }

    @Override
    public String toString() {
        return String.format("para %s\r\n",paraName);
    }

    @Override
    public String createMips() {
        String retStr = "";
        String belong = super.getBelong();
        // 其实没用
        retStr += String.format("lw %s, %s(%s)\r\n",
                Register.t0,
                Utility.getPushOffset(),
                Register.sp);
        retStr += String.format("sw %s %s(%s)\r\n",
                Register.t0,
                Utility.getOffset(paraName,belong),
                Utility.getPointerReg(paraName,belong));
        return retStr;
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            // 置dirty
            Register dst = Flow.applyWriteReg(paraName,this);
            MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                    dst,
                    Utility.getPushOffset(),
                    Register.sp);
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        Register paraReg = RegAlloc.applyReg(paraName,this);
        // 置dirty
        RegAlloc.writeReg(paraName,this);
        if (!Utility.isTemp(paraName)) {
            GlobalRegAlloc.writeBack(paraName,this);
        }
    }

    public String getParaName() {
        return paraName;
    }

    @Override
    public String getDef() {
        return paraName;
    }

    @Override
    public void renameDef(String src, String suffix) {
        if (paraName.equals(src)) {
            paraName = paraName + suffix;
        }
    }

    @Override
    public void behaviorAfterDelete() {
        // 形参删除之后有副作用
        // 删除之后的副作用 其分配的地址要空过去
        Utility.getPushOffset();
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.paraName.equals(src)) {
            this.paraName = dst;
        }
    }
}
