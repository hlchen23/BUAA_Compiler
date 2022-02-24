package SymTable;

import MidCode.MidCodeElement.MidCode;

import java.util.ArrayList;
import java.util.HashMap;


public class MipsGlobalTable {
    // 汇编符号表
    // 记录需要打印的字符串常量
    // 记录全局变量
    // 记录所有函数块的映射

    // 全局变量 放入全局变量区 gp
    // 字符串 放入.data段
    // 函数 用来从函数名快速查到相应的函数的table

    // 应该从字符串的值来找到相应的这个字符串对应的命名
    private static String strName = "String_";
    private static int autoStrName = 0; // 自动给字符串命名

    //

    private static HashMap<String,MipsTableItem> globalStrings = new HashMap<>(); // 整个程序中使用到的所有字符串常量

    private static HashMap<String,ArrayList<Integer>> globalConstArrays = new HashMap<>(); // 整个程序中所有的常数数组

    private static HashMap<String,MipsFuncTable> funs = new HashMap<>(); // 所有的函数

    private static HashMap<String,MipsTableItem> globalVars = new HashMap<>(); // 所有的全局变量

    private static int offset = 0; // 全局变量所占用的存储空间
    private static int count = 0; // 全局变量的个数
    // 存储全局的中间代码
    private static ArrayList<MidCode> midCodes = new ArrayList<>();

    public static void addGlobalString(String str) {
        if (globalStrings.containsKey(str)) {
            return;
        }
        else {
            MipsTableItem item = new MipsTableItem();
            item.setItemName(str);
            item.setType(MipsTableItem.Type.STR);
            item.setStrMark(strName + (autoStrName++));
            globalStrings.put(str,item);
        }
    }

    public static void addFun(String funName) {
        funs.put(funName,new MipsFuncTable());
    }

    public static void addGlobalVar(String varName) {
        MipsTableItem item = new MipsTableItem();
        item.setItemName(varName);
        item.setType(MipsTableItem.Type.VAR);
        item.setOffset(offset); // 给全局变量分配内存空间
        offset += 4;
        count += 1;
        globalVars.put(varName,item);
    }

    public static void addGlobalArrayVar(String varName,int len) {
        MipsTableItem item = new MipsTableItem();
        item.setItemName(varName);
        item.setType(MipsTableItem.Type.VAR_ARRAY);
        item.setOffset(offset);
        item.setLen(len);
        // 指针
        offset += 4;
        count += 1;
        // 划分连续的数组空间
        offset += (len * 4);
        globalVars.put(varName,item);
    }

    public static void addGlobalArrayConst(String constName,int len) {
        MipsTableItem item = new MipsTableItem();
        item.setItemName(constName);
        item.setType(MipsTableItem.Type.CONST_ARRAY);
        item.setOffset(offset);
        item.setLen(len);
        // 指针
        offset += 4;
        count += 1;
        // 划分连续的数组空间
        offset += (len * 4);
        globalVars.put(constName,item);
    }

    public static void addMidCode(MidCode midCode) {
        midCodes.add(midCode);
    }

    public static MipsFuncTable getFuncTable(String funName) {
        return funs.get(funName);
    }

    public static int getCount() {
        return count;
    }

    public static HashMap<String, MipsTableItem> getGlobalStrings() {
        return globalStrings;
    }

    public static HashMap<String, MipsFuncTable> getFuns() {
        return funs;
    }

    public static HashMap<String, MipsTableItem> getGlobalVars() {
        return globalVars;
    }

    public static ArrayList<MidCode> getMidCodes() {
        return midCodes;
    }
}
