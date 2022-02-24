package MidCode.MidCodeElement;

import java.util.HashSet;

public interface ABSTRACT_USE {

    // 每条语句中的使用是无顺序的
    HashSet<String> getUse();

    void renameUse(String src,String suffix);

    // 存在转化成赋值语句的潜能

    // 标记指令是否能够转换成赋值语句
    boolean canConvert2Assign();

    // 返回转化成的赋值语句的右值
    String convert2Assign();

    // 将src更新为dst
    void copySpreadRename(String src,String dst);

    String getCopySpreadLeftValue();
}
