package MidCode.MidCodeElement;

import Grammer.Grammar;
import Mips.MipsFactory;
import Optimizer.ControlFlowAnalysis.BaseBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MidCode {

    public MidCode nextMidCode;
    public MidCode prevMidCode;

    // 如果是使用语句 表示可以到达其某一个变量的定义
    public HashMap<String,ArrayList<ABSTRACT_DEF>> arriveDefs = new HashMap<>();
    // 如果是定义语句 表示所有可以到达的使用语句
    public ArrayList<ABSTRACT_USE> arriveUses = new ArrayList<>();

    // 标记当前中间代码是否被删除
    public boolean isDelete;
    // 是否是基本块的结尾
    public boolean isBlockTail;

    // 中间代码编号
    // 编号唯一确定中间代码
    private static int createNo = 0;

    // 所有MidCode的共有属性
    // 记录当前所属的函数
    private String belong;
    private int No;

    // 当前语句所处的基本块
    private BaseBlock inBaseBlock;

    // 当前四元式 "含本身四元式" 之后可能使用的临时变量
    public ArrayList<String> useTemp = new ArrayList<>();

    // 不活跃的变量
    public ArrayList<String> inactiveTemp = new ArrayList<>();
    // 当前四元式 "含本身四元式" 之前定义过的临时变量
    public ArrayList<String> defTemp = new ArrayList<>();

    // 四元式 位置 活跃的临时变量
    public ArrayList<String> activeTemp = new ArrayList<>();

    // 当前语句的活跃变量
    public HashSet<String> active = new HashSet<>();
    public HashSet<String> alreadyDefs = new HashSet<>();
    public HashSet<String> willUses = new HashSet<>();

    public MidCode() {
        this.isDelete = false;
        this.belong = Grammar.belong;
        this.No =  createNo++;
    }

    public String getBelong() {
        return belong;
    }

    public void setBelong(String belong) {
        this.belong = belong;
    }

    public int getNo() {
        return No;
    }

    public String createMips() {
        return "# ********************NO MIPS********************\r\n";
    }

    public void createMipsOpt() {
        // 优化版的生成mips 默认调用原来的
        MipsFactory.mipsCode += this.createMips();
    }

    public BaseBlock getInBaseBlock() {
        return inBaseBlock;
    }

    public void setInBaseBlock(BaseBlock inBaseBlock) {
        this.inBaseBlock = inBaseBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof MidCode)) {
            return false;
        }
        MidCode midCodeObj = (MidCode) o;
        return this.No == midCodeObj.getNo();
    }

    @Override
    public int hashCode() {
        return this.No;
    }
}
