package Mips.Instruction;

import MidCode.MidCodeElement.MidCode;
import MidCode.Utility;
import Mips.Register;
import Optimizer.RegisterAlloc.RegAlloc;

public class ADD extends INSTR {

    private String opt1;
    private String opt2;
    private String dst;

    private Register optReg1;
    private Register optReg2;
    private Register dstReg;

    private MidCode midCode;

    public ADD(String opt1, String opt2, String dst, MidCode midCode) {
        this.opt1 = opt1;
        this.opt2 = opt2;
        this.dst = dst;
        this.midCode = midCode; // 上下文

        System.out.println(midCode.inactiveTemp);

        // 指令选择优化
        if (Utility.isVar(opt1) && Utility.isVar(opt2)) {
            optReg1 = RegAlloc.applyReg(opt1,midCode);
            optReg2 = RegAlloc.applyReg(opt2,midCode);
            dstReg = RegAlloc.writeReg(dst,midCode);
        }
    }

    @Override
    public String toString() {
        if (Utility.isVar(opt1) && Utility.isVar(opt2)) {
            return String.format("addu %s, %s, %s\r\n",dstReg,optReg1,optReg2);
        }
        return "";
    }
}
