package MidCode.MidCodeElement;

import Mips.MipsFactory;
import Mips.Register;
import MidCode.Utility;

public class PRINT_STR extends MidCode
        implements ABSTRACT_OUTPUT {
    private String dstString;

    public PRINT_STR(String dstString) {
        super();
        this.dstString = dstString;
    }

    @Override
    public String toString() {
        return String.format("print_str %s\r\n",dstString);
    }

    @Override
    public String createMips() {
        String retStr = "";
        String belong = super.getBelong();
        retStr += String.format("la %s, %s\r\n",
                Register.a0,
                Utility.lookUpStrMark(dstString));
        retStr += String.format("li %s, %s\r\n", Register.v0,4);
        retStr += "syscall\r\n";
        return retStr;
    }

    @Override
    public void createMipsOpt() {
        if (dstString.equals("\"\"")) {
            // 空串不打印
            MipsFactory.mipsCode += "# NULL String do not need print!\r\n";
            return;
        }
        MipsFactory.mipsCode += String.format("la %s, %s\r\n",
                Register.a0,
                Utility.lookUpStrMark(dstString));
        MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,4);
        MipsFactory.mipsCode += "syscall\r\n";
    }

    public void createMipsOpt_Old() {
        String belong = super.getBelong();
        MipsFactory.mipsCode += String.format("la %s, %s\r\n",
                Register.a0,
                Utility.lookUpStrMark(dstString));
        MipsFactory.mipsCode += String.format("li %s, %s\r\n", Register.v0,4);
        MipsFactory.mipsCode += "syscall\r\n";
    }

    public String getDstString() {
        return dstString;
    }
}
