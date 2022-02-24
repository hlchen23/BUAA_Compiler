package MidCode.MidCodeElement;

public class VAR_ARRAY extends MidCode implements ABSTRACT_DECLARE {

    private String varName;
    private int dim_1 = -1;
    private int dim_2 = -1;

    public VAR_ARRAY(String varName,int dim_1) {
        super();
        this.varName = varName;
        this.dim_1 = dim_1;
    }

    public VAR_ARRAY(String varName, int dim_1, int dim_2) {
        this.varName = varName;
        this.dim_1 = dim_1;
        this.dim_2 = dim_2;
    }

    @Override
    public String toString() {
        if (dim_2 != -1) {
            return String.format("var_array %s[%d][%d]\r\n", varName,dim_1,dim_2);
        }
        else {
            return String.format("var_array %s[%d]\r\n",varName,dim_1);
        }
    }

    @Override
    public String createMips() {
        return "\r\n";
    }

    public String getVarName() {
        return varName;
    }

    public int getLength() {
        if (dim_2 != -1) {
            return dim_1 * dim_2;
        }
        else {
            return dim_1;
        }
    }

    @Override
    public void rename(String dstName) {
        varName = dstName;
    }

    @Override
    public VAR_ARRAY myClone() {
        VAR_ARRAY ret = new VAR_ARRAY(varName,dim_1,dim_2);
        ret.setBelong(super.getBelong());
        return ret;
    }

    @Override
    public String getDeclareName() {
        return varName;
    }
}
