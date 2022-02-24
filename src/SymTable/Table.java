package SymTable;

import java.util.HashMap;
import java.util.Stack;

public class Table {

    // 最外层是一个特殊的符号表 每个函数拿到一个符号表 每个函数内部使用栈式符号表
    private static Stack<TableItem> global = new Stack<>();
    private static HashMap<String,FuncTable> funcName2Table = new HashMap<>();

    // 向全局的表中插入数据
    public static void addGlobal(TableItem tableItem) {
        global.push(tableItem);
    }

    // 向全局插入一个函数的子表
    private static void setInFuncTable(String funcName,FuncTable funcTable) {
        funcName2Table.put(funcName,funcTable);
    }

    public static FuncTable getFuncTable(String funcName) {
        return funcName2Table.getOrDefault(funcName,null);
    }

    // 函数是否重定义
    public static boolean reDef(String name) {
        for (TableItem item : global) {
            // 全局声明 函数 互相之间也不可以重名
            if (item.getIdentName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
