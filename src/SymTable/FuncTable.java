package SymTable;

import java.util.Stack;

public class FuncTable {

    private Stack<TableItem> inner = new Stack<>();

    public void addInner(TableItem tableItem) {
        inner.push(tableItem);
    }

    public boolean reDef(String name) {
        for (TableItem item : inner) {
            // 全局声明 函数 互相之间也不可以重名
            if (item.getIdentName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
