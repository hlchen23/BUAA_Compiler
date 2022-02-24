package MidCode.MidCodeElement;

import MidCode.Utility;
import Mips.Register;

public class FUN extends MidCode {
    private String funName;

    // 中间代码有必要记录函数的类型信息
    public Type type;

    public int tailNo;

    public enum Type {
        VOID,
        INT
    }

    public FUN(String funName) {
        super();
        this.funName = funName;
    }

    @Override
    public String toString() {
        return String.format("fun %s():\r\n",funName);
    }

    @Override
    public String createMips() {
        String retStr = "";
        retStr += String.format("%s:\r\n",funName);
        // 维护ra寄存器
        retStr += String.format("sw %s, %s(%s)\r\n",
                Register.ra,
                Utility.getRegOffset(Register.ra),
                Register.sp);
        // 清空接收参数时的偏移
        Utility.clearPushOffset();
        return retStr;
    }

    public String getFunName() {
        return funName;
    }
}
