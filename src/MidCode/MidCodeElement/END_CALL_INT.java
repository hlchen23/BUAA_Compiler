package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.RegAlloc;
import Optimizer.RegisterAlloc.TempRegAlloc;

public class END_CALL_INT extends MidCode implements ABSTRACT_DEF {

    private String funName;
    private String dst;

    public END_CALL_INT(String funName, String dst) {
        super();
        this.funName = funName;
        this.dst = dst;
    }

    @Override
    public String toString() {
        return String.format("%s = end_call_int %s()\r\n",dst,funName);
    }

    @Override
    public void createMipsOpt() {
        if (isDelete) {
            behaviorAfterDelete();
        }
        else {
            Flow.save(this);
            MipsFactory.mipsCode += String.format("addi %s, %s, -%s\r\n",
                    Register.sp,
                    Register.sp,
                    Utility.getFuncOffset(funName));
            MipsFactory.mipsCode += String.format("jal %s\r\n",funName);
            MipsFactory.mipsCode += String.format("addi %s, %s, %s\r\n",
                    Register.sp,
                    Register.sp,
                    Utility.getFuncOffset(funName));
            Flow.restore(this);
            Register dstReg = Flow.applyWriteReg(dst,this);
            MipsFactory.mipsCode += String.format("move %s, %s\r\n",
                    dstReg,
                    Register.v0);
        }
    }

    public void createMipsOpt_Old() {

        GlobalRegAlloc.init();

        TempRegAlloc.save(this);

        MipsFactory.mipsCode += String.format("addi %s, %s, -%s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));

        MipsFactory.mipsCode += String.format("jal %s\r\n",funName);

        MipsFactory.mipsCode += String.format("addi %s, %s, %s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));

        TempRegAlloc.restore(this);

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

        // 开辟栈帧
        retStr += String.format("addi %s, %s, -%s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));
        // 调用函数
        retStr += String.format("jal %s\r\n",funName);

        // 恢复全局寄存器 s0-s7

        // 回收栈帧
        retStr += String.format("addi %s, %s, %s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));

        // 获取返回值 int型返回值通过寄存器v0传递
        retStr += String.format("sw %s, %s(%s)\r\n",
                Register.v0,
                Utility.getOffset(dst,belong),
                Utility.getPointerReg(dst,belong));
        return retStr;
    }

    public String getFunName() {
        return funName;
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
        // 函数调用不能直接删
        Flow.save(this);
        MipsFactory.mipsCode += String.format("addi %s, %s, -%s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));
        MipsFactory.mipsCode += String.format("jal %s\r\n",funName);
        MipsFactory.mipsCode += String.format("addi %s, %s, %s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));
        Flow.restore(this);
        // 只删除返回值的赋值
        MipsFactory.mipsCode += "# Dead assign has been deleted!\r\n";
    }

    @Override
    public void newRename(String src, String dst) {
        if (this.dst.equals(src)) {
            this.dst = dst;
        }
    }
}
