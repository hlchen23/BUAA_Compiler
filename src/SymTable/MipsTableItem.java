package SymTable;


public class MipsTableItem {
    // 汇编符号表项
    // 消解 域 的概念
    private String itemName; // 变量名称 也可以是一个字符串的"变量名"
    private Type type; // 标记是函数还是变量还是一个字符串
    private String strMark; // 这个字符串的标记名
    private int offset; // 这个变量放在内存距离这个区域的起始的偏移
    // 目前所有的变量都放在内存 只有计算时使用临时寄存器
    private int reg; // 当前使用的寄存器编号

    private String constArrayMark; // 常量数组在data段的标记名

    // 数组长度信息 (对于变量与常量 不包括形参)
    private int len;

    public enum Type {
        FUN,
        VAR,
        PARA,
        STR,
        //////////////
        VAR_ARRAY,
        CONST_ARRAY,
        PARA_ARRAY,
    }

    public String toString() {
        return String.format("%10s\t%10s\t%10s\t%10s\t%10s\r\n",itemName,type,strMark,offset,reg);
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setStrMark(String strMark) {
        this.strMark = strMark;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setReg(int reg) {
        this.reg = reg;
    }

    public String getItemName() {
        return itemName;
    }

    public Type getType() {
        return type;
    }

    public String getStrMark() {
        return strMark;
    }

    public int getOffset() {
        return offset;
    }

    public int getReg() {
        return reg;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }
}
