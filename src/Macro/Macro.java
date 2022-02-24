package Macro;

public class Macro {
    // 全局宏定义
    public static String EXIT_BLOCK_NAME = "#Exit";
    public static String GLOBAL_MARK = "&global";
    public static String MAIN = "main";
    public static String WEB_RENAME_SUFFIX = "+WEB_";
    // 表示变量是全局变量
    public static String GLOBAL_VAR_CONST_MARK = "-GLOBAL-";
    // 全局寄存器数量
    public static int S_REG_NUM = 8+6;

    // 避免函数与自定义label标签冲突 避免函数与.data段的标签冲突 需要给函数统一加一个前缀
    public static String FUN_PREFIX = "fun_";

    public static int test = 0;
}
