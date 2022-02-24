package Optimizer.RegisterAlloc;


import Macro.Macro;
import MidCode.MidCodeElement.ABSTRACT_DEF;
import MidCode.MidCodeElement.ABSTRACT_USE;
import MidCode.MidCodeElement.MidCode;
import Mips.Register;
import Optimizer.ControlFlowAnalysis.BaseBlock;
import Optimizer.ControlFlowAnalysis.SubFlow;

import java.util.*;


public class ConflictGraph {

    // 每一个函数子流图应该有一个冲突图
    SubFlow subFlow;

    // 实例模式
    // 冲突图与全局寄存器分配
    public HashMap<String, Register> allocTable;

    // 以变量为单位进行冲突图构建
    public ConflictGraph(SubFlow subFlow) {
        // 冲突图的子流图
        this.subFlow = subFlow;
        // 变量与对应的寄存器
        // 若部分配寄存器 则对应的Register为null
        allocTable = new HashMap<>();
    }

    public void analyze() {
        // 确定参与s寄存器分配的变量
        // 跨越基本块的变量
        // 即in与out的并集 去掉全局变量 去掉不可到达的变量
        // 使用不可到达变量的语句不翻译

        // 在这一步计算中间代码的数据流信息

        // 参与s寄存器分配的变量
        HashSet<String> participate = new HashSet<>();
        HashMap<String, BaseBlock> baseBlocks = subFlow.baseBlocks;
        HashMap<String,Node> nodes = new HashMap<>();

        for (BaseBlock baseBlock:baseBlocks.values()) {
            participate.addAll(baseBlock.getIn());
            participate.addAll(baseBlock.getOut());
        }

        HashSet<String> toBeDeleted = new HashSet<>();

        // 不含web字符串的要么是全局变量 要么是use的变量没有可以到达的定义
        for (String var : participate) {
            if (var.indexOf(Macro.WEB_RENAME_SUFFIX) < 0) {
                toBeDeleted.add(var);
            }
        }

        participate.removeAll(toBeDeleted);

        for (String var:participate) {
            // 创建节点
            nodes.put(var,new Node(var));
        }

        // 逐个基本块扫描
        for (BaseBlock baseBlock:baseBlocks.values()) {
            ArrayList<MidCode> midCodes = baseBlock.getMidCodes();
            // 首部活跃变量
            HashSet<String> activeHead = new HashSet<>();
            activeHead.addAll(baseBlock.getIn());
            // 删除全局与不可达
            HashSet<String> delTemp = new HashSet<>();
            for (String var:activeHead) {
                if (!participate.contains(var)) {
                    delTemp.add(var);
                }
            }
            activeHead.removeAll(delTemp);
            // 尾部活跃变量
            HashSet<String> activeTail = new HashSet<>();
            activeTail.addAll(baseBlock.getOut());
            delTemp = new HashSet<>();
            for (String var:activeTail) {
                if (!participate.contains(var)) {
                    delTemp.add(var);
                }
            }
            activeTail.removeAll(delTemp);

            ArrayList<HashSet<String>> everyActives = new ArrayList<>();
            ArrayList<HashSet<String>> everyDefs = new ArrayList<>();
            ArrayList<HashSet<String>> everyUses = new ArrayList<>();
            // 每条代码处有自己的活跃变量
            for (MidCode midCode:midCodes) {
                // 初始化
                everyActives.add(new HashSet<>());
                everyDefs.add(new HashSet<>());
                everyUses.add(new HashSet<>());
            }
            // 在前面定义过或者在activeHead中 且 在后面使用过或者在activeTail中 --> 活跃变量
            // 知道每条语句前面定义过的变量 后面使用过的变量 -- (在分配s寄存器的变量范围内)
            HashSet<String> defined = new HashSet<>();
            for (int i=0; i<midCodes.size();i++) {
                // 正向获取定义信息
                everyDefs.get(i).addAll(defined);
                MidCode midCode = midCodes.get(i);
                if (midCode instanceof ABSTRACT_DEF) {
                    String def = ((ABSTRACT_DEF) midCode).getDef();
                    if (participate.contains(def)) {
                        defined.add(def);
                    }
                }
            }
            HashSet<String> used = new HashSet<>();
            for (int i=midCodes.size()-1;i>=0;i--) {
                // 反向获取使用信息
                everyUses.get(i).addAll(used);
                MidCode midCode = midCodes.get(i);
                if (midCode instanceof ABSTRACT_USE) {
                    HashSet<String> uses = ((ABSTRACT_USE) midCode).getUse();
                    for (String use : uses) {
                        if (participate.contains(use)) {
                            used.add(use);
                        }
                    }
                }
            }

            // 计算每一步的活跃变量
            for (int i=0;i<midCodes.size();i++) {
                HashSet<String> up = new HashSet<>();
                up.addAll(activeHead);
                up.addAll(everyDefs.get(i));
                HashSet<String> down = new HashSet<>();
                down.addAll(activeTail);
                down.addAll(everyUses.get(i));
                HashSet<String> cross = new HashSet<>();
                for (String s:up) {
                    if (down.contains(s)) {
                        cross.add(s);
                    }
                }
                everyActives.get(i).addAll(cross);
            }

            for (int i=0; i<midCodes.size();i++) {
                MidCode midCode = midCodes.get(i);
                if (midCode instanceof ABSTRACT_DEF) {
                    String defVar = ((ABSTRACT_DEF) midCode).getDef();
                    if (!participate.contains(defVar)) {
                        // 定义的变量不在s寄存器分配范围内
                        continue;
                    }
                    // defVar与此时活跃的变量冲突
                    HashSet<String> conflicts = everyActives.get(i);
                    for (String conflict:conflicts) {
                        // defVar与conflict变量之间有一条边
                        addLine(nodes.get(defVar),nodes.get(conflict));
                    }
                }
            }
        }
        // 冲突图建立完成
        // 图着色算法
        ArrayList<Node> nodesList = new ArrayList<>();
        ArrayList<Node> nodesListMemory = new ArrayList<>();
        for (Node node:nodes.values()) {
            nodesList.add(node);
            nodesListMemory.add(node);
        }

        // 当前节点数
        int count = nodesList.size();
        int regNum = Macro.S_REG_NUM;
        int order = 0;

        while (count>0) {
            // 按节点的度降序排序
            Collections.sort(nodesList, new Comparator<Node>() {
                @Override
                public int compare(Node o1, Node o2) {
                    return o2.getDegree()-o1.getDegree();
                }
            });

            int mark = 0;
            for (int i=0; i < nodesList.size();i++) {
                if (nodesList.get(i).getDegree() < regNum) {
                    Node node = nodesList.remove(i);
                    node.order = order++;
                    node.haveReg = true;
                    // 维护图
                    HashSet<Node> adjs = node.adj;
                    for (Node node1:adjs) {
                        node1.adj.remove(node);
                    }
                    node.adj.clear();
                    count--;
                    mark = 1;
                    break;
                }
            }
            if (mark == 0) {
                // 没有找到合适的寄存器 移除第一个度最大的
                Node node = nodesList.remove(0);
                node.order = order++;
                node.haveReg = false;
                HashSet<Node> adjs = node.adj;
                for (Node node1:adjs) {
                    node1.adj.remove(node);
                }
                node.adj.clear();
                count--;
            }
        }

        // 按照order加回去
        // 按节点的order升序排序
        Collections.sort(nodesListMemory, new Comparator<Node>() {
            @Override
            public int compare(Node o1, Node o2) {
                return o1.order-o2.order;
            }
        });

        // 待分配寄存器
        HashSet<Register> alloc = new HashSet<>();
        alloc.add(Register.s0);
        alloc.add(Register.s1);
        alloc.add(Register.s2);
        alloc.add(Register.s3);
        alloc.add(Register.s4);
        alloc.add(Register.s5);
        alloc.add(Register.s6);
        alloc.add(Register.s7);
        alloc.add(Register.v1);
        alloc.add(Register.a1);
        alloc.add(Register.a2);
        alloc.add(Register.a3);
        alloc.add(Register.k0);
        alloc.add(Register.k1);

        for (int i =0; i < nodesListMemory.size();i++) {
            Node node = nodesListMemory.get(i);
            HashSet<Node> memory = node.memory;
            for (Node node1:memory) {
                if (node1.order<node.order) {
                    addLine(node, node1);
                }
            }
            // 所连接的寄存器的颜色
            HashSet<Register> temp = new HashSet<>();
            for (Node node1:node.adj) {
                if (node1.haveReg) {
                    temp.add(node1.register);
                }
            }

            if (node.haveReg) {
                // 如果当前节点是拥有寄存器的
                for (Register register:alloc) {
                    // 每次按照同样的顺序拿寄存器
                    // 尽可能使用更少数目的寄存器
                    if (!temp.contains(register)) {
                        // 分配一个相邻节点没有用过的寄存器
                        node.register = register;
                        allocTable.put(node.varName,register);
                    }
                }
            }
            else {
                // allocTable.put(node.varName,null);
            }
        }

    }

    public void print() {
        System.out.println(subFlow.funcName);
        System.out.println(allocTable);
    }

    public class Node {

        public String varName;
        public Register register;
        // 相邻节点
        public HashSet<Node> adj = new HashSet<>();

        public HashSet<Node> memory = new HashSet<>();

        // 默认
        public boolean haveReg = true;

        // 记录节点拿走的顺序
        public int order;

        public Node(String varName) {
            this.varName = varName;
        }

        public int getDegree() {
            return adj.size();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof Node)) {
                return false;
            }
            return this.varName.equals(((Node) obj).varName);
        }
    }

    public void addLine(Node node1, Node node2) {
        // 不算自己和自己冲突
        if (!node1.varName.equals(node2.varName)) {
            node1.adj.add(node2);
            node2.adj.add(node1);
            node1.memory.add(node2);
            node2.memory.add(node1);
        }
    }

}
