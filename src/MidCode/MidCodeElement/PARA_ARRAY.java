package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

public class PARA_ARRAY extends MidCode implements ABSTRACT_DEF {

    private String paraName;
    private int dim_1 = -1;
    private int dim_2 = -1;

    public PARA_ARRAY(String paraName) {
        super();
        this.paraName = paraName;
    }

    public PARA_ARRAY(String paraName,int dim_2) {
        super();
        this.paraName = paraName;
        this.dim_2 = dim_2;
    }

    @Override
    public String toString() {
        if (dim_2 != -1) {
            return String.format("para_array %s[][%d]\r\n", paraName, dim_2);
        }
        else {
            return String.format("para_array %s[]\r\n",paraName);
        }
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
        // 副作用
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
