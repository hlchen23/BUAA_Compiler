package Optimizer.RegisterAlloc;

import Macro.Macro;

public class Utils {

    public static boolean isTempVar(String var) {
        return var.charAt(0) == '#';
    }

    public static boolean isConst(String var) {
        return var.charAt(0) != '#' && var.charAt(0) != '@';
    }

    public static boolean isVar(String name) {
        return name.charAt(0) == '#' || name.charAt(0) == '@';
    }

    public static boolean isGlobalVar(String name) {
        return isVar(name) && (name.indexOf(Macro.GLOBAL_VAR_CONST_MARK) >= 0);
    }
}
