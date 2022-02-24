package MidCode.MidCodeElement;

import Mips.Register;
import MidCode.Utility;

public class CALL_INT extends MidCode {

    private String funName;

    public CALL_INT(String funName) {
        super();
        this.funName = funName;
    }

    @Override
    public String toString() {
        return String.format("call_int %s()\r\n",funName);
    }

    @Override
    public String createMips() {
        String retStr = "";
        // 清空压栈时的偏移
        Utility.clearPushOffset();
        Utility.setCallFunName(funName);
        // 开辟栈帧

        

        // 保存全局寄存器s0-s7

        // 暂时没有分配全局寄存器

        // 由函数内部维护ra

        return retStr;
    }

    public String getFunName() {
        return funName;
    }
}
