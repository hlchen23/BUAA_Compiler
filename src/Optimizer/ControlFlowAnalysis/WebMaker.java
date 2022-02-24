package Optimizer.ControlFlowAnalysis;

import MidCode.MidCodeElement.ABSTRACT_DEF;
import MidCode.MidCodeElement.ABSTRACT_USE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class WebMaker {

    // 邻接表
    private static HashMap<Chain,HashSet<Chain>> adj;
    // 标记是否已经走过
    private static HashMap<Chain,Boolean> mark;

    private static ArrayList<Chain> toBeMerged;

    public static HashSet<Web> createWebs(HashMap<ABSTRACT_DEF,Chain> chains) {
        // 定义辅助数据结构
        HashMap<ABSTRACT_USE,HashSet<Chain>> referenceCount = new HashMap<>();
        HashSet<ABSTRACT_USE> nodes = new HashSet<>();
        adj = new HashMap<>();
        mark = new HashMap<>();
        toBeMerged = new ArrayList<>();
        toBeMerged.addAll(chains.values());


        // 把一个变量的链合并成网
        HashSet<Web> retWebs = new HashSet<>();
        // 引用
        for (Chain chain : chains.values()) {
            HashSet<ABSTRACT_USE> uses = chain.getUses();
            for (ABSTRACT_USE use : uses) {
                if (!referenceCount.containsKey(use)) {
                    referenceCount.put(use,new HashSet<>());
                }
                referenceCount.get(use).add(chain);
            }
        }

        nodes.addAll(referenceCount.keySet());

        // 计算邻接关系 含有use节点的链条才会出现在邻接关系中
        for (ABSTRACT_USE use : nodes) {

            HashSet<Chain> chs = referenceCount.get(use);
            for (Chain ch : chs) {
                if (!adj.containsKey(ch)) {
                    adj.put(ch,new HashSet<>());
                    mark.put(ch,false);
                }
                // 自动去重
                adj.get(ch).addAll(chs);
                // 去掉自己
                adj.get(ch).remove(ch);
            }
        }

        // BFS 连通分量
        while (toBeMerged.size() > 0) {
            Chain first = toBeMerged.get(0);
            if (first.getUses().size() == 0) {
                // 只有定义节点 没有使用节点
                toBeMerged.remove(0);
                Web web = new Web();
                web.addChain(first);
                retWebs.add(web);
            }
            else {
                ArrayList<Chain> connect = BFS(toBeMerged.get(0));
                Web web = new Web();
                web.addChains(connect);
                // 有使用节点 则有0索引
                retWebs.add(web);
            }
        }

        for (Web web:retWebs) {
            web.setCanGetSReg();
        }
        return retWebs;
    }

    public static ArrayList<Chain> BFS(Chain chain) {
        ArrayList<Chain> retChains = new ArrayList<>();
        ArrayList<Chain> queue = new ArrayList<>();
        retChains.add(chain);
        queue.add(chain);
        mark.put(chain,true);
        while (queue.size() > 0) {
            Chain head = queue.get(0);
            HashSet<Chain> adjs = adj.get(head);

            for (Chain ch : adjs) {
                if (!mark.get(ch)) {
                    // false 没有访问过
                    mark.put(ch,true);
                    queue.add(ch);
                    retChains.add(ch);
                }
            }
            Chain removeChain = queue.remove(0);
            toBeMerged.remove(removeChain);
        }
        return retChains;
    }
}
