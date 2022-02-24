package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.RegisterAlloc.GlobalRegAlloc;
import Optimizer.RegisterAlloc.TempRegAlloc;

public class END_CALL_VOID extends MidCode {
    private String funName;

    public END_CALL_VOID(String funName) {
        super();
        this.funName = funName;
    }

    @Override
    public String toString() {
        return String.format("end_call_void %s()\r\n",funName);
    }

    @Override
    public void createMipsOpt() {

        Flow.save(this);
        // 开辟栈帧
        MipsFactory.mipsCode += String.format("addi %s, %s, -%s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));
        // 调用函数
        MipsFactory.mipsCode += String.format("jal %s\r\n",funName);
        // 恢复全局寄存器 s0-s7
        // 回收栈帧
        MipsFactory.mipsCode += String.format("addi %s, %s, %s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));
        Flow.restore(this);
    }

    public void createMipsOpt_Old() {

        GlobalRegAlloc.init();

        TempRegAlloc.save(this);
        // 开辟栈帧
        MipsFactory.mipsCode += String.format("addi %s, %s, -%s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));
        // 调用函数
        MipsFactory.mipsCode += String.format("jal %s\r\n",funName);

        // 恢复全局寄存器 s0-s7

        // 回收栈帧
        MipsFactory.mipsCode += String.format("addi %s, %s, %s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset(funName));
        TempRegAlloc.restore(this);
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
        return retStr;
    }

    public String getFunName() {
        return funName;
    }
}
