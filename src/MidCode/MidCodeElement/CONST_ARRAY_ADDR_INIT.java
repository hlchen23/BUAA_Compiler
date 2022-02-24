package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;

import MidCode.Utility;

public class CONST_ARRAY_ADDR_INIT extends MidCode implements ABSTRACT_DEF {
    // 常量数组的值放在全局data域
    // la指令获取


    // 数组分配完空间后把数组地址变量赋值为绝对地址
    private String dst;

    public CONST_ARRAY_ADDR_INIT(String dst) {
        super();
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = CONST_ARRAY_ADDR(%s)\r\n",dst,dst);
    }

    @Override
    public String createMips() {
        String retStr = "";
        String belong = super.getBelong();
        // TODO
        return retStr;
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            // TODO
        }
    }

    public void createMipsOpt_Old() {

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
        MipsFactory.mipsCode += "# Dead Code has been deleted!\r\n";
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
