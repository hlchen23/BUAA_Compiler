package MidCode.MidCodeElement;

import Mips.MipsFactory;


public class PARA_ARRAY_DECLARE extends MidCode implements ABSTRACT_DECLARE {

    private String paraName;
    private int dim_1 = -1;
    private int dim_2 = -1;

    public PARA_ARRAY_DECLARE(String paraName) {
        super();
        this.paraName = paraName;
    }

    public PARA_ARRAY_DECLARE(String paraName,int dim_2) {
        super();
        this.paraName = paraName;
        this.dim_2 = dim_2;
    }

    @Override
    public String toString() {
        // 用来声明类型
        if (dim_2 != -1) {
            return String.format("para_array_declare %s[][%d]\r\n", paraName, dim_2);
        }
        else {
            return String.format("para_array_declare %s[]\r\n",paraName);
        }
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
    public PARA_ARRAY_DECLARE myClone() {
        if (dim_2 != -1) {
            PARA_ARRAY_DECLARE ret = new PARA_ARRAY_DECLARE(paraName,dim_2);
            ret.setBelong(super.getBelong());
            return ret;
        }
        else {
            PARA_ARRAY_DECLARE ret = new PARA_ARRAY_DECLARE(paraName);
            ret.setBelong(super.getBelong());
            return ret;
        }
    }

    @Override
    public String getDeclareName() {
        return paraName;
    }
}
