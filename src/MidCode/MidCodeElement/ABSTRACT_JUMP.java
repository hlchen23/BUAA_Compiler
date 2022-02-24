package MidCode.MidCodeElement;

public interface ABSTRACT_JUMP {
    // 便于流图分析
    int getDstNum();
    String getDst_1();
    String getDst_2();
    void setDst_1(String dst_1);
    void setDst_2(String dst_2);
}
