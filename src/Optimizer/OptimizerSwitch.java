package Optimizer;

public class OptimizerSwitch {
    // 优化开关

    public static boolean GLOBAL_OPT = false;

    public static int DIV_MOD_UP_BOUND = 1073741824+1073741823;
    public static int DIV_MOD_DOWN_BOUND = -1073741824-1073741823-1;

    public static boolean DIV_OPT = GLOBAL_OPT && true;

    public static boolean MOD_OPT = GLOBAL_OPT && true;

    public static boolean MULT_OPT = GLOBAL_OPT && true;

    public static boolean FLOW_OPT = GLOBAL_OPT && true;

    public static boolean REG_ALLOC_OPT = GLOBAL_OPT && FLOW_OPT && true;

    public static boolean STRONG_REG_ALLOC_OPT = GLOBAL_OPT && FLOW_OPT && true;

    // 只把能算的常量算出来 其余的不是常量的不生成其对应的计算代码
    // 边向后走边计算
    // 尽可能减少变量从定义到使用的距离
    public static boolean ADD_SUB_MERGE_OPT = GLOBAL_OPT && false;

    public static boolean MULT_DIV_MOD_MERGE_OPT = GLOBAL_OPT && false;

    // 将常数数组放在data域
    public static boolean CONST_ARRAY_INTO_DATA = GLOBAL_OPT && false;

    // 死代码删除优化
    public static boolean DEAD_CODE_DELETE_OPT = GLOBAL_OPT && FLOW_OPT && true;

    // 常数合并 + 复制传播优化
    public static boolean COPY_SPREAD_OPT = GLOBAL_OPT && FLOW_OPT && true;

    // 临时寄存器池的OPT策略
    public static boolean TEMP_REG_POOL_OPT_STRATEGY = GLOBAL_OPT && REG_ALLOC_OPT && true;

    // 使用到达定义链做全局的常量传播
    public static boolean GLOBAL_CONST_SPREAD = GLOBAL_OPT && FLOW_OPT && STRONG_REG_ALLOC_OPT && COPY_SPREAD_OPT && true;

    // 与主函数的出口连接的基本块若无副作用直接删掉
    // 把整个main函数的副作用语句消除
    public static boolean DELETE_MAIN_TAIL_OPT = GLOBAL_OPT && FLOW_OPT && false;

    // 副作用删除
    public static boolean NO_SIDE_EFFECT_DELETE_OPT = GLOBAL_OPT && FLOW_OPT && STRONG_REG_ALLOC_OPT && true;

    // 循环摘除优化
    public static boolean LOOP_REMOVE_OPT = GLOBAL_OPT && true;
}
