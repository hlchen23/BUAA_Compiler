package MidCode;

public enum Opt {
    START_PROGRAM,
    END_PROGRAM,
    ////////////////
    ADD,
    SUB,
    MULT,
    DIV,
    MOD,
    ASSIGN,
    NEG, // 取相反数 对应mips中伪(扩展)指令
    ////////////////
    FUN,
    PARA,
    RETURN,
    ////////////////
    PUSH,
    CALL_INT,
    CALL_VOID,
    END_CALL_INT,
    END_CALL_VOID,
    ////////////////
    VAR,
    CONST,
    ////////////////
    PRINT_STR,
    PRINT_INT,
    GETINT,
    ///////////////
    BEZ,
    IF,
    ELSE,
    END_IF,
    LABEL,
    SUPPLEMENT_LABEL,
    ///////////////
    EQL, // ==
    NEQ, // !=
    LSS, // <
    GRE, // >
    LEQ, // <=
    GEQ, // >=
    NOT, // !
    ///////////////
    GOTO,
    ///////////////
    BEGIN_WHILE,
    END_WHILE,
    ///////////////
    CONTINUE,
    BREAK,
    ///////////////
    VAR_ARRAY,
    CONST_ARRAY,
    PARA_ARRAY,
    ///////////////
    ARRAY_ADDR_INIT,
    CONST_ARRAY_ADDR_INIT,
    ///////////////
    LOAD_ARRAY,
    STORE_ARRAY,
}

/**
 *  四元式格式文档
 *      作业一
 *      常量声明
 *      变量声明
 *      读语句getint
 *      写语句printf
 *      赋值语句
 *      加减乘除模等运算
 *      函数定义
 *      函数调用
 *
 *      不涉及数组 不涉及控制流语句
 *
 *     工厂模式
 *
 *     每进入一个表达式 临时变量从0开始命名
 *     ADD opt1 opt2 r
 *     SUB opt1 opt2 r
 *     MULT opt1 opt2 r
 *     DIV opt1 opt2 r
 *     MOD opt1 opt2 r
 *     ASSIGN opt1 null r
 *
 *     FUN funName null null // 在这里创建函数标签
 *     // 数组需要模板 模板应该在符号表存
 *     PARA name
 *
 *     PUSH 变量名或数字
 *     CALL_INT 函数名 r(可以是null 一个变量名)
 *     CALL_VOID
 *     RETURN 变量名或数字(可以是null)
 *
 *     初始值 对常量来讲是常量表达式 编译阶段直接计算 常量值在编译阶段替换
 *     对变量来讲是一个表达式 捆绑赋值指令 与表达式计算指令
 *     数组型的变量 a[1][2][3]...逐个赋值 按照一维列出即可
 *     VAR name
 *     CONST name
 *
 *     PRINT_STR "string"
 *     PRINT_INT num
 *     GETINT dst
 *
 *
 *
 *
 *
 *     ============================================================
 *                           本次作业用不到
 *     ============================================================
 *     // 本次作业不涉及这些关系 逻辑等运算
 *     // 可能用不到 因为会被作为逻辑真值计算 可能并不会直接跳转
 *     BEQ, // == opt1 opt2 label
 *     BNE, // != opt1 opt2 label
 *     BLT, // < opt1 opt2 label
 *     BLE, // <= opt1 opt2 label
 *     BGT, // > opt1 opt2 label
 *     BGE, // >= opt1 opt2 label
 *     LOAD_ARR 数组a offset r
 *     STORE_ARR v offset 数组a
 *     GOTO, // goto #LABEL_NAME#
 */
