package MidCode.MidCodeElement;

public class SUPPLEMENT_LABEL extends MidCode implements ABSTRACT_LABEL {
    // 补充标签
    // 程序中无实际作用
    // 方便切分基本块
    private String label;

    public SUPPLEMENT_LABEL(String label) {
        super();
        this.label = label;
    }

    @Override
    public String toString() {
        return String.format("supplement_label %s:\r\n",label);
    }

    @Override
    public String createMips() {
        String retStr = "";
        retStr += "# 辅助标签 用于划分基本块\r\n";
        retStr += String.format("%s:\r\n",label);
        return retStr;
    }

    @Override
    public String getEntrance() {
        return label;
    }
}
