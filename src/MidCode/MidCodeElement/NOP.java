package MidCode.MidCodeElement;

import Mips.MipsFactory;

public class NOP extends MidCode {

    @Override
    public String toString() {
        // 用来声明类型
        return "nop\r\n";
    }

    @Override
    public String createMips() {
        return "\r\n";
    }

    @Override
    public void createMipsOpt() {
        MipsFactory.mipsCode += "\r\n";
    }
}
