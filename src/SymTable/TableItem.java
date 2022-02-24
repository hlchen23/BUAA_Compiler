package SymTable;

import Grammer.Grammar;
import Macro.Macro;

import java.util.ArrayList;

public class TableItem {

    private String identName; // identity's name
    private String rename; // 变量的别名机制 解决block内作用域覆盖问题
    private IdentType identType; // var,const,para,func
    private DataType dataType; // int(var,const,para,func) void(fun)
    private int array_dim_1; // 数组第一维长度
    private int array_dim_2; // 数组第二位长度

    private String belong; // 记录所属的函数名称 全局是&global

    // dims
    // global表不需要dim
    private int dim; // from 0 on 对每一个函数表

    // functions
    private ArrayList<TableItem> paras = new ArrayList<>(); // 建立表内的指针

    // 记录常量的值
    private ArrayList<Integer> arrays = new ArrayList<>(); // 按照一维数组存储
    private int value; // 单个int的常量的值

    public TableItem() {

    }

    public TableItem(String identName,
                     IdentType identType,
                     DataType dataType,
                     int dim,
                     ArrayList<TableItem> paras) {
        this.identName = identName;
        this.identType = identType;
        this.dataType = dataType;
        this.dim = dim;
        this.paras = paras;
    }

    public void setBelong(String belong) {
        this.belong = belong;
    }

    public String getBelong() {
        return belong;
    }

    public String getRename() {
        return rename;
    }

    public void setRename(String rename) {
        this.rename = rename;
        if (Grammar.belong.equals(Macro.GLOBAL_MARK)) {
            // 全局加个标识
            this.rename += Macro.GLOBAL_VAR_CONST_MARK;
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public ArrayList<Integer> getArrays() {
        return arrays;
    }

    public void setArrays(ArrayList<Integer> arrays) {
        this.arrays = arrays;
    }

    public void setIdentName(String identName) {
        this.identName = identName;
    }

    public void setIdentType(IdentType identType) {
        this.identType = identType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public void setDim(int dim) {
        this.dim = dim;
    }

    public void setParas(ArrayList<TableItem> paras) {
        this.paras = paras;
    }

    public void setArray_dim_1(int array_dim_1) {
        this.array_dim_1 = array_dim_1;
    }

    public void setArray_dim_2(int array_dim_2) {
        this.array_dim_2 = array_dim_2;
    }

    public String getIdentName() {
        return identName;
    }

    public IdentType getIdentType() {
        return identType;
    }

    public DataType getDataType() {
        return dataType;
    }

    public int getDim() {
        return dim;
    }

    public ArrayList<TableItem> getParas() {
        return paras;
    }

    public int getArray_dim_1() {
        return array_dim_1;
    }

    public int getArray_dim_2() {
        return array_dim_2;
    }

    // 数组
    public int calculateArrayIndex(int ... args) {
        if (args.length == 1) {
            return args[0];
        }
        else if (args.length == 2){
            return args[0]*array_dim_1 + args[1];
        }
        else {
            return -1;
        }
    }

    public int calculateArrayOffset(int ... args) {
        return calculateArrayIndex(args)*4;
    }

    public int getArrayValue(int ... args) {
        int index = calculateArrayIndex(args);
        return arrays.get(index);
    }
}
