package MidCode;

import Macro.Macro;
import Mips.MipsFactory;
import Mips.Register;
import SymTable.MipsFuncTable;
import SymTable.MipsGlobalTable;

public class Utility {

    private static int pushOffset = MipsFactory.BASE; // 函数调用传参的压栈基地址
    private static String callFunName; // 记录当前即将调用的函数名

    public static void setCallFunName(String callFunName) {
        Utility.callFunName = callFunName;
    }

    public static String getCallFunName() {
        return callFunName;
    }

    public static int getPushAlign() {
        String callFun = getCallFunName();
        return getFuncOffset(callFun);
    }

    public static int getPushOffset() {
        int ret = pushOffset;
        pushOffset += 4;
        return ret;
    }

    public static void clearPushOffset() {
        // 调用一个函数的时候使用
        pushOffset = MipsFactory.BASE;
    }

    public static int getRegOffset(Register reg) {
        // 得到保存现场时寄存器的偏移
        return reg.ordinal() * 4;
    }

    public static int getFuncOffset(String funName) {
        // 得到调用这个函数的栈针应当移动的偏移
        return MipsGlobalTable.getFuncTable(funName).getOffset() + MipsFactory.BASE;
    }

    public static int getOffset(String name, String belong) {
        if (belong.equals("&global")) {
            // 全局量一定引用全局量
            return MipsGlobalTable.getGlobalVars().get(name).getOffset();
        }
        else {
            MipsFuncTable funcTable = MipsGlobalTable.getFuncTable(belong);

            if (funcTable.getVars().get(name) != null) {
                // 函数里的变量放入运行栈 + BASE
                return funcTable.getVars().get(name).getOffset() + MipsFactory.BASE;
            }
            else if (funcTable.getParas().get(name) != null) {
                return funcTable.getParas().get(name).getOffset() + MipsFactory.BASE;
            }
            else {
                // 内部引用全局变量
                return MipsGlobalTable.getGlobalVars().get(name).getOffset();
            }
        }
    }

    public static Register getPointerReg(String name, String belong) {
        if (belong.equals("&global")) {
            // global的语句一定引用的是global的量
            return Register.gp;
        }
        else {
            // 函数内的语句引用的可能是函数内的量 也可能是全局量
            MipsFuncTable funcTable = MipsGlobalTable.getFuncTable(belong);
            if (funcTable.getVars().get(name) != null) {
                return Register.sp;
            }
            else if (funcTable.getParas().get(name) != null) {
                return Register.sp;
            }
            else {
                // 内部引用全局变量
                return Register.gp;
            }
        }
    }

    public static boolean isVar(String str) {
        return  (str.charAt(0) == '#' || str.charAt(0) == '@');
    }

    public static boolean isTemp(String str) {
        return (str.charAt(0) == '#');
    }

    public static boolean isGlobalVar(String str) {
        return (str.indexOf(Macro.GLOBAL_VAR_CONST_MARK) >= 0);
    }

    public static String lookUpStrMark(String str) {
        return MipsGlobalTable.getGlobalStrings().get(str).getStrMark();
    }
}
