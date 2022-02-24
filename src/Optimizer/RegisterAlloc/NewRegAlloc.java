package Optimizer.RegisterAlloc;

import MidCode.MidCodeElement.ABSTRACT_DEF;
import MidCode.MidCodeElement.ABSTRACT_USE;
import MidCode.MidCodeElement.MidCode;
import Mips.MipsFactory;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.BaseBlock;
import Optimizer.ControlFlowAnalysis.SubFlow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import MidCode.Utility;
import Optimizer.OptimizerSwitch;


public class NewRegAlloc {

    // 一定要按照先申请读寄存器 再申请写寄存器的顺序

    // 新的寄存器分配器

    // 进入一个函数

    // 每个函数内部管理一个自己的寄存器池
    SubFlow subFlow;

    // 先不考虑全局 全局也有一个

    // 全局寄存器分配情况
    public HashMap<String, Register> sRegAllocTable;

    // 一条中间代码的 读 使用的寄存器不能被覆盖

    // 借的寄存器不可以与读的冲突 只需要 读 的时候借寄存器
    // 可以与写的冲突 最后完成写操作就行

    // 临时寄存器可能存放
    // 全局变量
    // 函数中的跨越基本块的局部变量但是s寄存器不够用了
    // 不跨越基本块的局部变量
    // 常数

    // 释放的优先级

    // 先释放不活跃的
    // 再释放常数
    // 没有的话随便找一个

    // 全局变量每次释放都要回写到变量的位置

    // 可用的临时寄存期池
    public HashSet<Register> availableTReg;

    // t寄存器分配状况
    // 没有分配则是null
    public HashMap<Register,String> tRegAllocTable = new HashMap<>();

    // 脏位信息 当t对应的不是null时才有意义
    public HashMap<Register,Boolean> dirty = new HashMap<>();


    public NewRegAlloc(
            // 函数的所有基本块
            SubFlow subFlow
    ) {
        if (OptimizerSwitch.STRONG_REG_ALLOC_OPT) {
            availableTReg = new HashSet<>(
                    Arrays.asList(
                            Register.t0,
                            Register.t1,
                            Register.t2,
                            Register.t3,
                            Register.t4,
                            Register.t5,
                            Register.t6,
                            Register.t8,
                            Register.t9,
                            Register.fp
                    ));
        }
        else {
            // 弱寄存器分配优化
            availableTReg = new HashSet<>(
                    Arrays.asList(
                            Register.t0,
                            Register.t1,
                            Register.t2,
                            Register.t3,
                            Register.t4,
                            Register.t5,
                            Register.t6,
                            Register.t8,
                            Register.t9,
                            Register.fp
                    ));
        }
        this.subFlow = subFlow;
        this.sRegAllocTable = this.subFlow.sRegAllocTable;
        // 没用到的s寄存器当t寄存器用
        HashSet<Register> used = new HashSet<>();
        for (String key:sRegAllocTable.keySet()) {
            used.add(sRegAllocTable.get(key));
        }
        HashSet<Register> s = new HashSet<>();
        s.add(Register.s0);
        s.add(Register.s1);
        s.add(Register.s2);
        s.add(Register.s3);
        s.add(Register.s4);
        s.add(Register.s5);
        s.add(Register.s6);
        s.add(Register.s7);
        // a0,v0 不能乱用 在打印的使用会使用
        s.add(Register.v1);
        s.add(Register.a1);
        s.add(Register.a2);
        s.add(Register.a3);
        s.add(Register.k0);
        s.add(Register.k1);

        // 本函数中不用的s寄存器当做t寄存器使用
        for (Register sReg : s) {
            if (!used.contains(sReg)) {
                availableTReg.add(sReg);
            }
        }

        // 初始化临时寄存器池
        for (Register reg:availableTReg) {
            tRegAllocTable.put(reg,null);
        }

        // 计算数据流信息 并保存在中间代码中

        calculateDataFlow();

    }




    public void calculateDataFlow() {
        if (OptimizerSwitch.STRONG_REG_ALLOC_OPT) {
            HashMap<String,BaseBlock> baseBlocks = subFlow.baseBlocks;

            // 主要是局部数据流
            // 含有不可达变量的语句不会翻译
            HashSet<String> haveS = new HashSet<>();
            // 已经拥有全局寄存器了
            for (String var:sRegAllocTable.keySet()) {
                haveS.add(var);
            }

            // 基本块内部已经实现SSA

            // 活跃分析

            for (BaseBlock baseBlock : baseBlocks.values()) {

                // 逐个基本块内部
                ArrayList<MidCode> midCodes = baseBlock.getMidCodes();
                // 首部活跃变量
                HashSet<String> activeHead = new HashSet<>();
                activeHead.addAll(baseBlock.getIn());
                activeHead.removeAll(haveS);
                // 尾部活跃变量
                HashSet<String> activeTail = new HashSet<>();
                activeTail.addAll(baseBlock.getOut());
                activeTail.removeAll(haveS);

                // 测试时test为1
                //if (Macro.test != 1) {
                HashSet<String> used = new HashSet<>();
                for (int i = midCodes.size() - 1; i >= 0; i--) {
                    // 反向获取使用信息
                    MidCode midCode = midCodes.get(i);
                    midCode.willUses.addAll(used);
                    if (midCode instanceof ABSTRACT_USE) {
                        HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                        for (String use : uses) {
                            if (!haveS.contains(use)) {
                                used.add(use);
                            }
                        }
                    }
                }
                //}

//                HashSet<String> defined = new HashSet<>();
//                for (int i = 0; i < midCodes.size(); i++) {
//                    // 正向获取定义信息
//                    MidCode midCode = midCodes.get(i);
//                    midCode.alreadyDefs.addAll(defined);
//                    if (midCode instanceof ABSTRACT_DEF) {
//                        String def = ((ABSTRACT_DEF) midCode).getDef();
//                        if (!haveS.contains(def)) {
//                            defined.add(def);
//                        }
//                    }
//                }

                HashSet<String> defined = new HashSet<>();
                //if (Macro.test != 1) {
                // 计算每一步中间代码的活跃变量 + 定义信息
                for (int i = 0; i < midCodes.size(); i++) {

                    MidCode midCode = midCodes.get(i);
                    midCode.alreadyDefs.addAll(defined);
                    if (midCode instanceof ABSTRACT_DEF) {
                        String def = ((ABSTRACT_DEF) midCode).getDef();
                        if (!haveS.contains(def)) {
                            defined.add(def);
                        }
                    }

                    // MidCode midCode = midCodes.get(i);
                    HashSet<String> up = new HashSet<>();
                    up.addAll(activeHead);
                    up.addAll(midCode.alreadyDefs);
                    HashSet<String> down = new HashSet<>();
                    down.addAll(activeTail);
                    down.addAll(midCode.willUses);
                    HashSet<String> cross = new HashSet<>();
//                        for (String s : up) {
//                            if (down.contains(s)) {
//                                cross.add(s);
//                            }
//                        }
                    // up和down的交集
                    cross.addAll(up);
                    cross.retainAll(down);
                    midCode.active.addAll(cross);
                }
            }
            //}
        }
        else {
            // 弱寄存器分配
            // willUse需要修改 新的定义可能会kill掉使用
            HashMap<String,BaseBlock> baseBlocks = subFlow.baseBlocks;

            // 主要是局部数据流
            // 含有不可达变量的语句不会翻译
            HashSet<String> haveS = new HashSet<>();

            // 有s寄存器的变量不分析了
            for (String var:sRegAllocTable.keySet()) {
                haveS.add(var);
            }

            // 活跃分析

            for (BaseBlock baseBlock : baseBlocks.values()) {

                // 逐个基本块内部
                ArrayList<MidCode> midCodes = baseBlock.getMidCodes();
                // 首部活跃变量
                HashSet<String> activeHead = new HashSet<>();
                activeHead.addAll(baseBlock.getIn());
                activeHead.removeAll(haveS);
                // 尾部活跃变量
                HashSet<String> activeTail = new HashSet<>();
                activeTail.addAll(baseBlock.getOut());
                activeTail.removeAll(haveS);

                // 测试时test为1
                //if (Macro.test != 1) {
                HashSet<String> used = new HashSet<>();
                for (int i = midCodes.size() - 1; i >= 0; i--) {
                    // 反向获取使用信息
                    // 基本块内部不是SSA 使用可能会被kill掉

                    // kill 加到代码的willUse 然后再添加到use

                    MidCode midCode = midCodes.get(i);

                    if (midCode instanceof ABSTRACT_DEF) {
                        String def = ((ABSTRACT_DEF) midCode).getDef();
                        if (used.contains(def)) {
                            // kill掉
                            used.remove(def);
                        }
                    }

                    midCode.willUses.addAll(used);

                    if (midCode instanceof ABSTRACT_USE) {
                        HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                        for (String use : uses) {
                            if (!haveS.contains(use)) {
                                used.add(use);
                            }
                        }
                    }
                }
                //}

//                HashSet<String> defined = new HashSet<>();
//                for (int i = 0; i < midCodes.size(); i++) {
//                    // 正向获取定义信息
//                    MidCode midCode = midCodes.get(i);
//                    midCode.alreadyDefs.addAll(defined);
//                    if (midCode instanceof ABSTRACT_DEF) {
//                        String def = ((ABSTRACT_DEF) midCode).getDef();
//                        if (!haveS.contains(def)) {
//                            defined.add(def);
//                        }
//                    }
//                }

                HashSet<String> defined = new HashSet<>();
                //if (Macro.test != 1) {
                // 计算每一步中间代码的活跃变量 + 定义信息
                for (int i = 0; i < midCodes.size(); i++) {

                    MidCode midCode = midCodes.get(i);
                    midCode.alreadyDefs.addAll(defined);
                    if (midCode instanceof ABSTRACT_DEF) {
                        String def = ((ABSTRACT_DEF) midCode).getDef();
                        if (!haveS.contains(def)) {
                            defined.add(def);
                        }
                    }

                    // MidCode midCode = midCodes.get(i);
                    HashSet<String> up = new HashSet<>();
                    up.addAll(activeHead);
                    up.addAll(midCode.alreadyDefs);
                    HashSet<String> down = new HashSet<>();
                    down.addAll(activeTail);
                    down.addAll(midCode.willUses);
                    HashSet<String> cross = new HashSet<>();
//                        for (String s : up) {
//                            if (down.contains(s)) {
//                                cross.add(s);
//                            }
//                        }
                    // up和down的交集
                    cross.addAll(up);
                    cross.retainAll(down);
                    midCode.active.addAll(cross);
                }
            }
            //}
        }
    }

    // midCode是上下文语境
    public Register applyReadReg(String name, MidCode midCode) {

        // 可能是常数 0
        if (name.equals("0")) {
            return Register.zero;
        }
        // s寄存器直接返回
        if (sRegAllocTable.containsKey(name)) {
            return sRegAllocTable.get(name);
        }
        // 同一个midCode的use不能冲突
        HashSet<String> uses = new HashSet<>();
        if (midCode instanceof ABSTRACT_USE) {
            uses.addAll(((ABSTRACT_USE) midCode).getUse());
        }

        // 查看临时寄存器
        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) != null) {
                if (tRegAllocTable.get(reg).equals(name)) {
                    // 不能置dirty位
                    return reg;
                }
            }
        }
        // 没有映射好的 看下有无空闲
        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) == null) {
                load(name,reg,midCode);
                tRegAllocTable.put(reg,name);
                dirty.put(reg,false);
                return reg;
            }
        }
        // 加入没有空闲的 从非全局变量中寻找不活跃的变量
        for (Register reg : tRegAllocTable.keySet()) {
            // 一定不是null
            String value = tRegAllocTable.get(reg);
            if (uses.contains(value)) {
                continue;
            }
            if (Utils.isGlobalVar(value)) {
                // 全局变量 不讨论活跃与否
                continue;
            }
            if (!midCode.active.contains(value)) {
                // 不是当前语句的活跃变量
                // 可能是常量等
                load(name,reg,midCode);
                tRegAllocTable.put(reg,name);
                dirty.put(reg,false);
                return reg;
            }
        }
        // 活跃变量与全局 随便替换一个 全局要回写
        // 全局变量可以认为一直活跃
        for (Register reg : tRegAllocTable.keySet()) {
            String value = tRegAllocTable.get(reg);
            if (uses.contains(value)) {
                continue;
            }
            if (dirty.get(reg)) {
                writeBack(reg, midCode);
            }
            load(name,reg,midCode);
            tRegAllocTable.put(reg,name);
            dirty.put(reg,false);
            return reg;
        }
        return null;
    }

    // 保守 写和读的寄存器也不能冲突
    public Register applyWriteReg(String name, MidCode midCode) {
        // 一定是变量
        // 全局直接返回
        if (sRegAllocTable.containsKey(name)) {
            return sRegAllocTable.get(name);
        }

        // 保守起见 写的也不能和读的冲突
        HashSet<String> uses = new HashSet<>();
        if (midCode instanceof ABSTRACT_USE) {
            uses.addAll(((ABSTRACT_USE) midCode).getUse());
        }

        // 是否有已经在里面的
        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) != null) {
                if (tRegAllocTable.get(reg).equals(name)) {
                    dirty.put(reg,true);
                    return reg;
                }
            }
        }
        // 有空闲
        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) == null) {
                tRegAllocTable.put(reg,name);
                dirty.put(reg,true);
                return reg;
            }
        }
        // 没有空闲
        for (Register reg : tRegAllocTable.keySet()) {
            String value = tRegAllocTable.get(reg);
            if (uses.contains(value)) {
                continue;
            }
            if (Utils.isGlobalVar(value)) {
                continue;
            }
            if (!midCode.active.contains(value)) {
                tRegAllocTable.put(reg,name);
                dirty.put(reg,true);
                return reg;
            }
        }
        // 随便替换一个
        for (Register reg : tRegAllocTable.keySet()) {
            String value = tRegAllocTable.get(reg);
            if (uses.contains(value)) {
                continue;
            }
            if (dirty.get(reg)) {
                writeBack(reg,midCode);
            }
            tRegAllocTable.put(reg,name);
            dirty.put(reg,true);
            return reg;
        }
        return null;
    }

    public void save(MidCode midCode) {
        // 常量不保存
        // 活跃的且已经修改过的保存到变量所在的位置
        // 自动解决全局变量的问题
        // 保存现场
        // 全局变量要保存到全局变量的存储位置

        // 只保留脏的
        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) == null) {
                continue;
            }
            String value = tRegAllocTable.get(reg);
            if (Utils.isGlobalVar(value) || midCode.active.contains(value)) {
                if (dirty.get(reg)) {

                    writeBack(reg, midCode);
                }
            }
            else {
                // 常数要clear掉 因为没有恢复 类似于基本块结尾清空寄存器池一样
                tRegAllocTable.put(reg,null);
            }
        }
        // 保存s寄存器
        HashSet<Register> sRegs = new HashSet<>();
        for (String var : sRegAllocTable.keySet()) {
            sRegs.add(sRegAllocTable.get(var));
        }
        for (Register reg : sRegs) {
            // 保存s寄存器不能按照变量存储 要放入寄存器保护区
            MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                    reg,
                    Utility.getRegOffset(reg),
                    Register.sp);
        }
    }

    public void restore(MidCode midCode) {
        // 恢复现场
        // 全局变量要从全局变量的存储位置获取
        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) == null) {
                continue;
            }
            String value = tRegAllocTable.get(reg);
            if (Utils.isGlobalVar(value) || midCode.active.contains(value)) {
                // 全部恢复
                load(value,reg,midCode);
            }
        }
        // 恢复s寄存器
        HashSet<Register> sRegs = new HashSet<>();
        for (String var : sRegAllocTable.keySet()) {
            sRegs.add(sRegAllocTable.get(var));
        }
        for (Register reg : sRegs) {
            // 保存s寄存器不能按照变量存储 要放入寄存器保护区
            MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                    reg,
                    Utility.getRegOffset(reg),
                    Register.sp);
        }
    }

    // 每次借出去的不能是一样的
    private HashSet<Register> hasBorrowed = new HashSet<>();

    public Register borrowReg(MidCode midCode) {
        // 中间过程需要借用寄存器
        // 如果有空的
        // 与当前的use不能冲突

        // 借用的变量与midCode的def与use均不能冲突
        HashSet<String> conflicts = new HashSet<>();
        if (midCode instanceof ABSTRACT_USE) {
            conflicts.addAll(((ABSTRACT_USE) midCode).getUse());
        }
        if (midCode instanceof ABSTRACT_DEF) {
            conflicts.add(((ABSTRACT_DEF) midCode).getDef());
        }
        // 不能与当前步骤借出去的相同

        // 没有映射好的 看下有无空闲
        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) == null) {
                if (!hasBorrowed.contains(reg)) {
                    // 未被借
                    hasBorrowed.add(reg);
                    return reg;
                }
            }
        }
        // 加入没有空闲的 从非全局变量中寻找不活跃的变量
        for (Register reg : tRegAllocTable.keySet()) {
            // 一定不是null
            String value = tRegAllocTable.get(reg);
            if (conflicts.contains(value)) {
                continue;
            }
            if (Utils.isGlobalVar(value)) {
                // 全局变量 不讨论活跃与否
                continue;
            }
            if (!midCode.active.contains(value)) {
                // 不是当前语句的活跃变量
                // 可能是常量等
                if (!hasBorrowed.contains(reg)) {
                    tRegAllocTable.put(reg, null);
                    hasBorrowed.add(reg);
                    return reg;
                }
            }
        }
        // 活跃变量与全局 随便替换一个 全局要回写
        // 全局变量可以认为一直活跃
        for (Register reg : tRegAllocTable.keySet()) {
            String value = tRegAllocTable.get(reg);
            if (conflicts.contains(value)) {
                continue;
            }
            if (!hasBorrowed.contains(reg)) {
                if (dirty.get(reg)) {
                    writeBack(reg, midCode);
                }
                hasBorrowed.add(reg);
                return reg;
            }
        }
        return null;
    }

    public void returnReg(Register reg, MidCode midCode) {
        if (tRegAllocTable.get(reg) != null) {
            load(tRegAllocTable.get(reg),reg,midCode);
        }
    }

    public void clearBorrow() {
        hasBorrowed.clear();
    }

    private void load(String name,Register reg, MidCode midCode) {
        if (Utils.isConst(name)) {
            MipsFactory.mipsCode += String.format("li %s, %s\r\n",
                    reg,
                    name);
        }
        else if (Utils.isVar(name)) {
            MipsFactory.mipsCode += String.format("lw %s, %s(%s)\r\n",
                    reg,
                    Utility.getOffset(name,midCode.getBelong()),
                    Utility.getPointerReg(name,midCode.getBelong()));
        }
    }

    private void writeBack(Register reg, MidCode midCode) {
        // t寄存器的回写


        MipsFactory.mipsCode += String.format("sw %s, %s(%s)\r\n",
                reg,
                Utility.getOffset(tRegAllocTable.get(reg),midCode.getBelong()),
                Utility.getPointerReg(tRegAllocTable.get(reg),midCode.getBelong()));
    }

    // 基本块的末尾要清空寄存器池
    public void saveBlockTail(MidCode midCode) {

        for (Register reg : tRegAllocTable.keySet()) {
            if (tRegAllocTable.get(reg) != null) {
                String value = tRegAllocTable.get(reg);

                if (Utils.isGlobalVar(value)) {
                    if (dirty.get(reg)) {
                        writeBack(reg, midCode);
                    }
                }
                else {
                    // 看出口的out
                    if (midCode.getInBaseBlock().getOut().contains(value)) {
                        if (dirty.get(reg)) {
                            writeBack(reg, midCode);
                        }
                    }
                }
            }
            tRegAllocTable.put(reg,null);
        }
    }
}
