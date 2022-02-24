package Mips;

import Macro.Macro;
import MidCode.MidCodeElement.*;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.OptimizerSwitch;
import SymTable.MipsFuncTable;
import SymTable.MipsGlobalTable;
import SymTable.MipsTableItem;

import java.util.ArrayList;
import java.util.HashMap;


public class MipsFactory {
    // 生成mips代码
    // 工厂模式

    // 设定一些人工固定的超参数
    // 栈针移动的基础量
    public static final int BASE = 32*4; // 32个通用寄存器
    public static String mipsCode = "";


    // 把中间代码读进来 生成符号表
    public static void createMips(ArrayList<MidCode> midCodes) {
        for (MidCode midCode : midCodes) {
            // 先处理全局表
            if (midCode.getBelong().equals("&global")) {
                MipsGlobalTable.addMidCode(midCode);
                if (midCode instanceof VAR) {
                    MipsGlobalTable.addGlobalVar(((VAR) midCode).getVarName());
                }
                else if (midCode instanceof VAR_ARRAY) {
                    MipsGlobalTable.addGlobalArrayVar(
                            ((VAR_ARRAY) midCode).getVarName(),((VAR_ARRAY) midCode).getLength()
                    );
                }
                else if (midCode instanceof CONST_ARRAY) {
                    MipsGlobalTable.addGlobalArrayConst(
                            ((CONST_ARRAY) midCode).getConstName(),((CONST_ARRAY) midCode).getLength()
                    );
                }
                else if (midCode instanceof FUN) {
                    MipsGlobalTable.addFun(((FUN) midCode).getFunName());
                }
            }
            else {
                if (midCode instanceof FUN) {
                    MipsGlobalTable.addFun(((FUN) midCode).getFunName());
                }
            }
        }
        // 再处理函数内部
        for (MidCode midCode : midCodes) {
            if (!midCode.getBelong().equals("&global")) {
                String belongFunc = midCode.getBelong();
                MipsFuncTable funcTable = MipsGlobalTable.getFuncTable(belongFunc);
                // 添加这个函数管理的中间代码
                funcTable.addMidCode(midCode);

                // 形参拆分成多个变量后可以共享一个位置
                if (midCode instanceof PARA_DECLARE) {
                    funcTable.addPara(((PARA_DECLARE) midCode).getParaName());
                }
                else if (midCode instanceof VAR){
                    funcTable.addVar(((VAR) midCode).getVarName());
                }
                else if (midCode instanceof PARA_ARRAY_DECLARE) {
                    funcTable.addArrayPara(((PARA_ARRAY_DECLARE) midCode).getParaName());
                }
                else if (midCode instanceof VAR_ARRAY) {
                    funcTable.addArrayVar(((VAR_ARRAY) midCode).getVarName(),((VAR_ARRAY) midCode).getLength());
                }
                else if (midCode instanceof CONST_ARRAY) {
                    funcTable.addArrayConst(((CONST_ARRAY) midCode).getConstName(),((CONST_ARRAY) midCode).getLength());
                }
                else if (midCode instanceof PRINT_STR) {
                    // 全局保存一共需要打印的字符串
                    MipsGlobalTable.addGlobalString(((PRINT_STR) midCode).getDstString());
                }
            }
        }
        mipsCode += ".data\r\n";
        HashMap<String, MipsTableItem> globalStrings = MipsGlobalTable.getGlobalStrings();
        for (String key : globalStrings.keySet()) {
            mipsCode += String.format("%s: .asciiz %s\r\n",
                    globalStrings.get(key).getStrMark(),
                    globalStrings.get(key).getItemName());
        }
        mipsCode += "\r\n";
        mipsCode += ".text\r\n";


        if (OptimizerSwitch.DIV_OPT) {
            mipsCode += "# 除法优化开启\r\n";
        }
        if (OptimizerSwitch.MOD_OPT) {
            mipsCode += "# 取模优化开启\r\n";
        }
        if (OptimizerSwitch.MULT_OPT) {
            mipsCode += "# 乘法优化开启\r\n";
        }
        if (OptimizerSwitch.FLOW_OPT) {
            mipsCode += "# 数据流优化开启\r\n";
        }
        if (OptimizerSwitch.REG_ALLOC_OPT) {
            mipsCode += "# 开启寄存器分配\r\n";
        }
        if (OptimizerSwitch.STRONG_REG_ALLOC_OPT) {
            mipsCode += "# 开启强寄存器分配\r\n";
        }
        if (OptimizerSwitch.COPY_SPREAD_OPT) {
            mipsCode += "# 开启复制传播和常量合并\r\n";
        }
        if (OptimizerSwitch.GLOBAL_CONST_SPREAD) {
            mipsCode += "# 开启全局常量传播\r\n";
        }
        if (OptimizerSwitch.DEAD_CODE_DELETE_OPT) {
            mipsCode += "# 开启死代码删除\r\n";
        }
        if (OptimizerSwitch.NO_SIDE_EFFECT_DELETE_OPT) {
            mipsCode += "# 开启全局无副作用代码删除\r\n";
        }
        if (OptimizerSwitch.LOOP_REMOVE_OPT) {
            mipsCode += "# 开启无用循环删除\r\n";
        }

        if (OptimizerSwitch.REG_ALLOC_OPT) {
            // OPT
            for (MidCode midCode : midCodes) {
                mipsCode += String.format("# %s",midCode); // mips中注释 中间代码
                try {
                    midCode.createMipsOpt();
                } catch (NullPointerException e) {
                    mipsCode += "# 对变量的使用没有可以到达的定义!属于不可达代码!\r\n";
                }
                // 基本块的结尾要清空寄存器池子
                // 全局的比较特殊
                if (!midCode.getBelong().equals(Macro.GLOBAL_MARK)) {
                    if (midCode.isBlockTail) {
                        // JUMP单独做
                        if (!(midCode instanceof ABSTRACT_JUMP)) {
                            // 既然是已经能够申请到寄存器 那么变量一定是存在的
                            mipsCode += "# 基本块结尾清空寄存器池\r\n";
                            Flow.saveBlockTail(midCode);
                        }
                    }
                }
                mipsCode += "\r\n";
            }
        }
        else {
            for (MidCode midCode : midCodes) {
                mipsCode += String.format("# %s",midCode); // mips中注释 中间代码
                try {
                    mipsCode += midCode.createMips();
                } catch (NullPointerException e) {
                    mipsCode += "# 对变量的使用没有可以到达的定义!属于不可达代码!\r\n";
                }
                mipsCode += "\r\n";
            }
        }
    }
    public static void printMipsTable() {
        // 打印符号表项
        HashMap<String, MipsTableItem> globalStrings = MipsGlobalTable.getGlobalStrings();
        HashMap<String, MipsTableItem> globalVars = MipsGlobalTable.getGlobalVars();
        HashMap<String,MipsFuncTable> funs = MipsGlobalTable.getFuns();
        for (String key : globalStrings.keySet()) {
            System.out.println(globalStrings.get(key));
        }
        for (String key : globalVars.keySet()) {
            System.out.println(globalVars.get(key));
        }
        for (String key : funs.keySet()) {
            MipsFuncTable fun = funs.get(key);
            System.out.println(String.format("\n\t\t====================fun %s=====================",key)); // 打印函数名称
            // 打印形参与变量
            HashMap<String,MipsTableItem> vars = fun.getVars();
            HashMap<String,MipsTableItem> paras = fun.getParas();
            for (String innerKey : vars.keySet()) {
                System.out.println(vars.get(innerKey));
            }
            for (String innerKey : paras.keySet()) {
                System.out.println(paras.get(innerKey));
            }
            System.out.println("\t\t================================================\n");
        }
    }

    public static String mipsToString() {
        return mipsCode;
    }
}
