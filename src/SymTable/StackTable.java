package SymTable;

import Grammer.Grammar;

import java.util.Stack;

public class StackTable {
    /*
    * 简单的栈式符号表 只有一个 使用static类型
    */
    public static Stack<TableItem> tables = new Stack<>();

    public static void push(TableItem tableItem) {
        tables.push(tableItem);
    }

    public static void pop() {
        tables.pop();
    }

    public static TableItem peek() {
        return tables.peek();
    }

    public static boolean reDef(String name,boolean isFunc) {

        // 查找当前作用域内是否重定义
        for (int i = tables.size()-1; i>=0; i--) {
            if (tables.get(i).getDim() == Grammar.dim) {
                if (tables.get(i).getIdentName().equals(name)) {
                    if (isFunc) {
                        if (tables.get(i).getIdentType() == IdentType.FUNC) {
                            return true;
                        }
                    }
                    else {
                        if (tables.get(i).getIdentType() != IdentType.FUNC) {
                            return true;
                        }
                    }
                }
            }
            else { break; }
        }
        return false;
    }

    public static TableItem def(String name,boolean isFunc) {
        // 如果定义过返回该项的TableItem
        for (int i = tables.size()-1;i>=0;i--) {
            if (tables.get(i).getIdentName().equals(name)) {
                if (isFunc) {
                    if (tables.get(i).getIdentType() == IdentType.FUNC) {
                        return tables.get(i);
                    }
                }
                else {
                    if (tables.get(i).getIdentType() != IdentType.FUNC) {
                        return tables.get(i);
                    }
                }
            }
        }
        return null; // 表示没找到
    }

    public static void clear() {
        // 清空当前dim的符号表项
        while (!tables.empty() && tables.peek().getDim() == Grammar.dim) {
            tables.pop();
        }
    }
}
