package MidCode.MidCodeElement;

public class ELSE extends MidCode implements ABSTRACT_LABEL {

    private String label;

    public ELSE(String label) {
        super();
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("label_else %s:\r\n",label);
    }

    @Override
    public String createMips() {
        return String.format("%s:\r\n",label);
    }

    @Override
    public String getEntrance() {
        return label;
    }
}
