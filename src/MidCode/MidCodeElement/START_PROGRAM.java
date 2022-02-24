package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;
import Optimizer.ControlFlowAnalysis.Flow;

public class START_PROGRAM extends MidCode {
    // 开始程序
    @Override
    public String toString() {
        return "ENTRANCE OF PROGRAM\r\n";
    }

    @Override
    public String createMips() {
        String retStr = "";
        // 清空压栈时的偏移
        Utility.clearPushOffset();
        // 开辟栈帧
        retStr += String.format("addi %s, %s, -%s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset("main"));


        retStr += "jal main\r\n";
        return retStr;
    }

    @Override
    public void createMipsOpt() {

        Flow.saveBlockTail(this);

        // 清空压栈时的偏移
        Utility.clearPushOffset();
        // 开辟栈帧
        MipsFactory.mipsCode += String.format("addi %s, %s, -%s\r\n",
                Register.sp,
                Register.sp,
                Utility.getFuncOffset("main"));


        MipsFactory.mipsCode += "jal main\r\n";
    }
}
