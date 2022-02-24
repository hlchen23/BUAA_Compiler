package MidCode.MidCodeElement;

public class IF extends MidCode implements ABSTRACT_LABEL {

    private String label;

    public IF(String label) {
        super();
        this.label = label;
    }

    @Override
    public String toString() {
        // if主体的入口标签
        return String.format("label_if %s:\r\n",label);
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
