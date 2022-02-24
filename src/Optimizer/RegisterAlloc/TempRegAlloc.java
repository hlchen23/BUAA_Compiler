package Optimizer.RegisterAlloc;

import MidCode.MidCodeElement.MidCode;
import MidCode.Utility;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.BaseBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class TempRegAlloc {

    // 一条语句中 一个临时寄存器不能分配给多个变量 常数

    // 单例模式

    // 寄存器-->变量名 或者 常数
    private static HashMap<Register,String> regs = new HashMap<>();

    // 寄存器-->脏位
    private static HashMap<Register,Boolean> dirty = new HashMap<>();

    // OPT

    private static MidCode midCodeNow = null;
    private static String belongNow = null;
    private static BaseBlock baseBlockNow = null;

    private static HashSet<Register> ban = new HashSet<>();

    private static void init(MidCode midCode) {
        // 设置当前的作用域
        belongNow = midCode.getBelong();

        // 进入一个新的语句时更新ban
        if (!midCode.equals(midCodeNow)) {
            ban = new HashSet<>();
            midCodeNow = midCode;
        }

        // 进入一个新的基本块中时 才执行如下的初始化
        if (!midCode.getInBaseBlock().equals(baseBlockNow)) {

            baseBlockNow = midCode.getInBaseBlock();

            regs = new HashMap<>();
            dirty = new HashMap<>();

            regs.put(Register.t0,null);
            regs.put(Register.t1,null);
            regs.put(Register.t2,null);
            regs.put(Register.t3,null);
            regs.put(Register.t4,null);
            regs.put(Register.t5,null);
            regs.put(Register.t6,null);
            regs.put(Register.t7,null);
            regs.put(Register.t8,null);
            regs.put(Register.t9,null);
        }
    }

    public static void writeBack() {

    }

    public static void save(MidCode midCode) {

        // 保存t寄存器
        for (Register reg : regs.keySet()) {
            // 利用现场保护区
            if (regs.get(reg) != null) {
                String name = regs.get(reg);

                if (midCode.inactiveTemp.contains(name)) {
                    // 不活跃的变量 放弃
                    regs.put(reg,null);
                }
                else {
                    // 活跃变量或者常数
                    // 常数也不能存 代价太高 需要的时候li就行
                    if (Utils.isConst(name)) {
                        // 把常数清空
                        regs.put(reg,null);
                    }
                    else {
                        MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                                reg,
                                Utility.getRegOffset(reg),
                                Register.sp
                        );
                    }
                }

            }
        }
    }

    public static void restore(MidCode midCode) {
        // 恢复t寄存器 保存时已经把不需要保存的置null了
        for (Register reg : regs.keySet()) {
            if (regs.get(reg) != null) {
                MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                        reg,
                        Utility.getRegOffset(reg),
                        Register.sp);
            }
        }
    }

    public static HashMap<Register,String> borrowReg(int num, MidCode midCode) {
        // 不对应常数或者或者变量
        // 转mips过程中额外需要的寄存器

        // 表示借用的寄存器 对应的变量
        HashMap<Register,String> regsRet = new HashMap<>();
        int count = 0;
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            if (regs.get(reg) == null) {
                ban.add(reg);
                regsRet.put(reg,null);
                count += 1;
                if (count == num) {
                    return regsRet;
                }
            }
        }
        // 现在如果不够 已经没有null了
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            String var = regs.get(reg);
            if (midCode.inactiveTemp.contains(var)) {
                // 如果不活跃了 可以直接扔掉
                ban.add(reg);
                regsRet.put(reg,null);
                count += 1;
                if (count == num) {
                    return regsRet;
                }
            }
        }
        // 活跃的变量 或者 常数
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            // 用完了要恢复的
            ban.add(reg);
            regsRet.put(reg,regs.get(reg));
            // 变量要保存到栈 常数直接记下来
            if (Utility.isVar(regs.get(reg))) {
                MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                        reg,
                        Utility.getOffset(regs.get(reg), midCode.getBelong()),
                        Utility.getPointerReg(regs.get(reg), midCode.getBelong()));
            }
            count += 1;
            if (count == num) {
                return regsRet;
            }
        }
        // 借寄存器不会超过临时寄存器数目
        return regsRet;
    }

    public static void returnReg(HashMap<Register,String> regsPara, MidCode midCode) {
        // 归还寄存器
        for (Register keyReg:regsPara.keySet()) {
            if (regsPara.get(keyReg) != null) {
                // 归还
                String name = regsPara.get(keyReg);
                if (Utility.isVar(name)) {
                    // !!! 手误 啊 是lw!!!
                    MipsFactory.mipsCode += "# 归还后的补偿代码\r\n";
                    MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                            keyReg,
                            Utility.getOffset(name, midCode.getBelong()),
                            Utility.getPointerReg(name, midCode.getBelong()));
                }
                else {
                    MipsFactory.mipsCode += "# 归还后的补偿代码\r\n";
                    MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                            keyReg,
                            name);
                }
            }
        }
    }

    public static Register applyReg(String var, MidCode midCode) {

        init(midCode);
        if (var.equals("0")) {
            return Register.zero;
        }
        ArrayList<String> active = midCode.activeTemp;
        ArrayList<String> inactive = midCode.inactiveTemp;
        // 如果存在
        for (Register reg : regs.keySet()) {
            // if (ban.contains(reg)) {
            //     continue;
            // }
            if (regs.get(reg) != null) {
                if (regs.get(reg).equals(var)) {
                    ban.add(reg);
                    return reg;
                }
            }
        }
        // 没有找到 检查一下有没有空闲的
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            if (regs.get(reg) == null) {
                // 有空闲寄存器
                if (Utils.isConst(var)) {
                    MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                            reg,
                            var);
                }
                else {
                    MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                            reg,
                            Utility.getOffset(var,belongNow),
                            Utility.getPointerReg(var,belongNow));
                }

                regs.put(reg,var);
                // 读 不会脏
                dirty.put(reg,false);
                ban.add(reg);
                return reg;
            }
        }
        // 假如也没有空闲的
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            // 如果有不活跃 可以替换 且不用回写
            if (inactive.contains(regs.get(reg))) {
                if (Utils.isConst(var)) {
                    MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                            reg,
                            var);
                }
                else {
                    MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                            reg,
                            Utility.getOffset(var,belongNow),
                            Utility.getPointerReg(var,belongNow));
                }
                // 覆盖掉
                regs.put(reg,var);
                dirty.put(reg,false);
                ban.add(reg);
                return reg;
            }
        }
        // 假如没有找到不活跃的
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            // 如果不是临时变量 则是常数
            // 替换一个常数 不用回写
            if (!Utils.isTempVar(regs.get(reg))) {
                if (Utils.isConst(var)) {
                    MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                            reg,
                            var);
                }
                else {
                    MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                            reg,
                            Utility.getOffset(var,belongNow),
                            Utility.getPointerReg(var,belongNow));
                }
                regs.put(reg,var);
                dirty.put(reg,false);
                ban.add(reg);
                return reg;
            }
        }
        // 常数也没有 只能替换活跃的
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            if (dirty.get(reg)) {
                // 如果脏 回写
                MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                        reg,
                        Utility.getOffset(regs.get(reg),belongNow),
                        Utility.getPointerReg(regs.get(reg),belongNow));
            }
            if (Utils.isConst(var)) {
                MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                        reg,
                        var);
            }
            else {
                MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                        reg,
                        Utility.getOffset(var,belongNow),
                        Utility.getPointerReg(var,belongNow));
            }
            regs.put(reg,var);
            dirty.put(reg,false);
            ban.add(reg);
            return reg;
        }
        return null;
    }

    // 左边多个使用时 两个使用是不能冲突的
    public static Register writeReg(String var, MidCode midCode) {

        init(midCode);
        // var不可能是常数
        ArrayList<String> active = midCode.activeTemp;
        ArrayList<String> inactive = midCode.inactiveTemp;
        // 如果存在
        for (Register reg : regs.keySet()) {
            // 如果存在的情况下 直接给出 并不覆盖其他的 可以重复输出 不用ban
            //if (ban.contains(reg)) {
            //    continue;
            //}
            if (regs.get(reg) != null) {
                if (regs.get(reg).equals(var)) {
                    dirty.put(reg, true);
                    ban.add(reg);
                    return reg;
                }
            }
        }
        //  检查一下有没有空闲的
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            if (regs.get(reg) == null) {
                // 有空闲寄存器
                regs.put(reg,var);
                // 脏
                dirty.put(reg,true);
                ban.add(reg);
                return reg;
            }
        }
        // 假如也没有空闲的
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            // 如果有不活跃 可以替换 且不用回写
            if (inactive.contains(regs.get(reg))) {
                // 覆盖掉
                regs.put(reg,var);
                dirty.put(reg,true);
                ban.add(reg);
                return reg;
            }
        }

        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            // 如果不是临时变量 则是常数
            // 替换一个常数 不用回写
            if (!Utils.isTempVar(regs.get(reg))) {
                regs.put(reg,var);
                dirty.put(reg,true);
                ban.add(reg);
                return reg;
            }
        }
        // 常数也没有 只能替换活跃的
        for (Register reg : regs.keySet()) {
            if (ban.contains(reg)) {
                continue;
            }
            // 注意避免冲突
            if (dirty.get(reg)) {
                // 如果脏 回写
                MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                        reg,
                        Utility.getOffset(regs.get(reg),belongNow),
                        Utility.getPointerReg(regs.get(reg),belongNow));
            }
            regs.put(reg,var);
            dirty.put(reg,true);
            ban.add(reg);
            return reg;
        }
        return null;
    }
}
