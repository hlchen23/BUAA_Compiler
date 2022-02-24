package MidCode.MidCodeElement;

public class LABEL extends MidCode implements ABSTRACT_LABEL {
    // 自定义label
    private String label;

    public LABEL(String label) {
        super();
        this.label = label;
    }

    @Override
    public String toString() {
        // if主体的入口标签
        return String.format("label_self_defined %s:\r\n",label);
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
