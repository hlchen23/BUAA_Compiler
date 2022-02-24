package MidCode.MidCodeElement;

public interface ABSTRACT_DECLARE {
    void rename(String dstName);
    ABSTRACT_DECLARE myClone();
    String getDeclareName();
}
