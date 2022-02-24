package Optimizer.RegisterAlloc;

import MidCode.MidCodeElement.MidCode;
import Mips.Register;

public class RegAlloc {

    // 全局寄存器其实比较死
    // 进入一个函数就固定了

    // 全局变量跨越函数 不能用全局寄存器

    // 由寄存器管理类统一进行load/Store

    public static Register applyReg(String var, MidCode midCode) {
        // 读 可能读常数
        // 获取寄存器的时候可能需要从内存读值
        if (Utils.isTempVar(var) || Utils.isConst(var)) {
            return TempRegAlloc.applyReg(var,midCode);
        }
        else {
            return GlobalRegAlloc.applyReg(var,midCode);
        }
    }

    public static Register writeReg(String var, MidCode midCode) {
        // 写 不可能写常数
        // 不需要读值 只需要读一个值
        // 只有被替换的时候需要回写
        if (Utils.isTempVar(var)) {
            return TempRegAlloc.writeReg(var,midCode);
        }
        else {
            return GlobalRegAlloc.writeReg(var,midCode);
        }
    }
}
