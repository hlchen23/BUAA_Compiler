package MidCode.MidCodeElement;

public class CONST_ARRAY extends MidCode implements ABSTRACT_DECLARE {

    private String constName;
    private int dim_1 = -1;
    private int dim_2 = -1;

    public CONST_ARRAY(String constName,int dim_1) {
        super();
        this.constName = constName;
        this.dim_1 = dim_1;
    }

    public CONST_ARRAY(String constName,int dim_1,int dim_2) {
        super();
        this.constName = constName;
        this.dim_1 = dim_1;
        this.dim_2 = dim_2;
    }

    @Override
    public String toString() {
        if (dim_2 != -1) {
            return String.format("const_array %s[%d][%d]\r\n", constName,dim_1,dim_2);
        }
        else {
            return String.format("const_array %s[%d]\r\n",constName,dim_1);
        }
    }

    @Override
    public String createMips() {
        return "\r\n";
    }

    public String getConstName() {
        return constName;
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
        constName = dstName;
    }

    @Override
    public CONST_ARRAY myClone() {
        CONST_ARRAY const_array = new CONST_ARRAY(constName,dim_1,dim_2);
        const_array.setBelong(super.getBelong());
        return const_array;
    }

    @Override
    public String getDeclareName() {
        return constName;
    }
}
