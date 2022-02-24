package MidCode.MidCodeElement;

public interface ABSTRACT_DEF {
    // 所有的抽象赋值
    String getDef();

    void renameDef(String src,String suffix);

    void newRename(String src, String dst);

    // 定义语句删除后的行为 函数调用 getint要保留
    void behaviorAfterDelete();
}
