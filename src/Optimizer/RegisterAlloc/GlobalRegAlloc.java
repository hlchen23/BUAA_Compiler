package Optimizer.RegisterAlloc;

import MidCode.MidCodeElement.MidCode;
import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;

public class GlobalRegAlloc {

    // 最多一步使用两个全局
    private static boolean s0 = true;
    private static boolean s1 = true;

    public static void init() {
        s0 = true;
        s1 = true;
    }

    public static Register applyReg(String var, MidCode midCode) {
        if (s0) {
            MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                    Register.s0,
                    Utility.getOffset(var, midCode.getBelong()),
                    Utility.getPointerReg(var, midCode.getBelong()));
            s0 = false;
            return Register.s0;
        }
        else {
            MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                    Register.s1,
                    Utility.getOffset(var, midCode.getBelong()),
                    Utility.getPointerReg(var, midCode.getBelong()));
            s1 = false;
            return Register.s1;
        }
    }

    public static Register writeReg(String var, MidCode midCode) {
        return Register.s0;
    }

    public static void writeBack(String var, MidCode midCode) {
        MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                Register.s0,
                Utility.getOffset(var,midCode.getBelong()),
                Utility.getPointerReg(var,midCode.getBelong()));
    }
}
