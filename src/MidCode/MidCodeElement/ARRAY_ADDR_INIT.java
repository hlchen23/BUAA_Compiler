package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

public class ARRAY_ADDR_INIT extends MidCode implements ABSTRACT_DEF {

    // 数组分配完空间后把数组地址变量赋值为绝对地址
    private String dst;

    public ARRAY_ADDR_INIT(String dst) {
        super();
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = ADDR(%s)\r\n",dst,dst);
    }

    @Override
    public String createMips() {
        String retStr = "";
        String belong = super.getBelong();
        retStr += String.format("addiu %s, %s, %s\r\n",
                Register.t0,
                Utility.getPointerReg(dst,belong),
                (Utility.getOffset(dst,belong) + 4));
        retStr += String.format("sw %s, %s(%s)\r\n",
                Register.t0,
                Utility.getOffset(dst,belong),
                Utility.getPointerReg(dst,belong));
        return retStr;
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            String belong = super.getBelong();
            Register dstReg = Flow.applyWriteReg(dst, this);
            MipsFactory.mipsCode += String.format("addiu %s, %s, %s\r\n",
                    dstReg,
                    Utility.getPointerReg(dst,belong),
                    (Utility.getOffset(dst,belong) + 4));
        }
    }

    public void createMipsOpt_Old() {
        GlobalRegAlloc.init();
        String belong = super.getBelong();
        Register dstReg = RegAlloc.writeReg(dst,this);
        MipsFactory.mipsCode += String.format("addiu %s, %s, %s\r\n",
                dstReg,
                Utility.getPointerReg(dst,belong),
                (Utility.getOffset(dst,belong) + 4));
        if (!Utility.isTemp(dst)) {
            GlobalRegAlloc.writeBack(dst,this);
        }
    }

    @Override
    public String getDef() {
        return dst;
    }

    @Override
    public void renameDef(String src, String suffix) {
        if (dst.equals(src)) {
            dst  = dst + suffix;
        }
    }

    @Override
    public void behaviorAfterDelete() {
        // 数组地址定义 可以直接删
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
