package MidCode.MidCodeElement;

public class END_WHILE extends MidCode implements ABSTRACT_LABEL {
    public String label;

    public END_WHILE(String label) {
        super();
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("label_end_while %s:\r\n",label);
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
