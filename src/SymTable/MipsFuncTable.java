package SymTable;

import MidCode.MidCodeElement.MidCode;
import MidCode.MidCodeElement.VAR;

import java.util.ArrayList;
import java.util.HashMap;


public class MipsFuncTable {
    // 汇编函数符号表
    // 每个函数对应一个符号表

    // 所有的变量与形参 均以重命名过 不会有重名现象
    private HashMap<String,MipsTableItem> vars = new HashMap<>();
    private HashMap<String,MipsTableItem> paras = new HashMap<>();

    private int offset = 0; // 分配局部变量与形参的内存空间
    private int count = 0; // 记录变量与形参一共的个数

    // 存储全局的中间代码
    private ArrayList<MidCode> midCodes = new ArrayList<>();

    public void addVar(String varName) {
        MipsTableItem tableItem = new MipsTableItem();
        tableItem.setItemName(varName);
        tableItem.setType(MipsTableItem.Type.VAR);
        tableItem.setOffset(offset);
        offset += 4;
        count += 1;
        vars.put(varName,tableItem);
    }

    public void addPara(String paraName) {
        MipsTableItem tableItem = new MipsTableItem();
        tableItem.setItemName(paraName);
        tableItem.setType(MipsTableItem.Type.PARA);
        tableItem.setOffset(offset);
        offset += 4;
        count += 1;
        paras.put(paraName,tableItem);
    }

    public void addArrayVar(String varName,int len) {
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
        vars.put(varName,item);
    }

    public void addArrayConst(String constName,int len) {
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
        vars.put(constName,item);
    }

    public void addArrayPara(String paraName) {
        // 形参其实是指针
        MipsTableItem item = new MipsTableItem();
        item.setItemName(paraName);
        item.setType(MipsTableItem.Type.PARA_ARRAY);
        item.setOffset(offset);
        // 指针
        offset += 4;
        count += 1;
        // 不划分数组空间
        paras.put(paraName,item);
    }

    public void addMidCode(MidCode midCode) {
        midCodes.add(midCode);
    }

    public int getCount() {
        return count;
    }

    public int getOffset() {
        return offset;
    }

    public HashMap<String, MipsTableItem> getVars() {
        return vars;
    }

    public HashMap<String, MipsTableItem> getParas() {
        return paras;
    }

    public ArrayList<MidCode> getMidCodes() {
        return midCodes;
    }
}
