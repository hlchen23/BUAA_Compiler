package MidCode.MidCodeElement;

import Mips.MipsFactory;


public class PARA_DECLARE extends MidCode implements ABSTRACT_DECLARE {

    private String paraName;

    public PARA_DECLARE(String paraName) {
        super();
        this.paraName = paraName;
    }

    @Override
    public String toString() {
        // 只是用来声明空间的
        return String.format("para_declare %s\r\n",paraName);
    }

    @Override
    public String createMips() {
        return "\r\n";
    }

    @Override
    public void createMipsOpt() {
        MipsFactory.mipsCode += "\r\n";
    }

    public String getParaName() {
        return paraName;
    }

    @Override
    public void rename(String dstName) {
        paraName = dstName;
    }

    @Override
    public PARA_DECLARE myClone() {
        // 形参再次出现时就是变量
        PARA_DECLARE ret = new PARA_DECLARE(paraName);
        ret.setBelong(super.getBelong());
        return ret;
    }

    @Override
    public String getDeclareName() {
        return paraName;
    }
}
