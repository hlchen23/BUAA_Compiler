package MidCode.MidCodeElement;

import Mips.Register;

public class END_PROGRAM extends MidCode {
    @Override
    public String toString() {
        return "END OF PROGRAM\r\n";
    }

    @Override
    public String createMips() {
        String retStr = "";
        // 结束程序
        retStr += String.format("li %s, %s\r\n", Register.v0,10);
        retStr += "syscall\r\n";
        return retStr;
    }
}
