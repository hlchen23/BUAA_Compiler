package MidCode.MidCodeElement;

public class CONST extends MidCode implements ABSTRACT_DECLARE {
    private String constName;

    public CONST(String constName) {
        super();
        this.constName = constName;
    }

    @Override
    public String toString() {
        return String.format("const %s\r\n",constName);
    }

    @Override
    public String createMips() {
        return "\r\n";
    }

    public String getConstName() {
        return constName;
    }

    @Override
    public void rename(String dstName) {
        constName = dstName;
    }

    @Override
    public CONST myClone() {
        CONST constNew = new CONST(constName);
        constNew.setBelong(super.getBelong());
        return constNew;
    }

    @Override
    public String getDeclareName() {
        return constName;
    }
}
