package MidCode.MidCodeElement;


public class VAR extends MidCode implements ABSTRACT_DECLARE {
    private String varName;

    public VAR(String varName) {
        super();
        this.varName = varName;
    }

    @Override
    public String toString() {
        return String.format("var %s\r\n",varName);
    }

    @Override
    public String createMips() {
        return "\r\n";
    }

    public String getVarName() {
        return varName;
    }

    @Override
    public void rename(String dstName) {
        varName = dstName;
    }

    @Override
    public VAR myClone() {
        VAR ret = new VAR(varName);
        ret.setBelong(super.getBelong());
        return ret;
    }

    @Override
    public String getDeclareName() {
        return varName;
    }
}
