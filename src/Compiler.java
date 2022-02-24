
import Grammer.Grammar;
import Lexer.Lexer;
import Lexer.Token;
import MidCode.MidCodeElement.FUN;
import MidCode.MidCodeElement.MidCode;
import MidCode.MidCodeFactory;
import Mips.MipsFactory;
import MyException.EOF;
import Optimizer.ControlFlowAnalysis.Flow;
import Optimizer.OptimizerSwitch;

import java.io.IOException;
import java.util.ArrayList;

import Error.MyError;

public class Compiler {
    public static void main(String[] args) throws IOException {
        FileOutput fileOutput = new FileOutput("error.txt");

        // FileOutput midCodeFileOutput = new FileOutput("midCode.txt");

        Lexer lexer = new Lexer();
        ArrayList<Token> tokens = getTokens(lexer);
        Grammar.init(tokens);
        try {Grammar.analyze();} catch (EOF e) {}
        // fileOutput.output(OutputList.list2String());



        // fileOutput.output(MyError.errors2String(false));
        // System.out.println(MyError.errors2String(true)); // 输出详细信息
        // fileOutput.output(MidCodeFactory.MidCode2Str());
        // System.out.println(MidCodeFactory.MidCode2Str());

        // 生成mips

        ArrayList<MidCode> midCodes = MidCodeFactory.getMidCodes();

        // 修饰一下中间代码
        // 把函数定义放在函数自己的域内反正无所谓
        for (MidCode midCode : midCodes) {
            if (midCode instanceof FUN) {
                midCode.setBelong(((FUN) midCode).getFunName());
            }
        }

        Flow.createFlow(midCodes);
        Flow.checkReturnError(); // 不破坏中间代码

        if (MyError.myErrors.size() > 0) {
            fileOutput = new FileOutput("error.txt");
            MyError.sort();
            fileOutput.output(MyError.errors2String(false));
            System.out.println(MyError.errors2String(true));
            fileOutput.closeFile();
            System.exit(0);
        }
        else {
            fileOutput = new FileOutput("mips.txt");
        }

        // midCodeFileOutput.output(MidCodeFactory.MidCode2Str());
        // midCodeFileOutput.closeFile();

        if (OptimizerSwitch.FLOW_OPT) {
            // 传递的是基本块间的数据流
            // 不含有基本块内部的数据流

            if (OptimizerSwitch.STRONG_REG_ALLOC_OPT) {
                // 应先把基本块内部数据流优化完毕 再做基本块间数据流
                Flow.createFlow(midCodes);
                Flow.global2local();
                // System.out.println(midCodes);
                Flow.createFlow(midCodes);
                // 常量传播和复制传播会修改中间代码 改变变量的作用范围 所以先做传播 再做使用定义分析
                if (OptimizerSwitch.COPY_SPREAD_OPT) {
                    // 将基本块按照顺序排布
                    // 将函数入口的基本块补上函数定义语句
                    // &global的基本块中删除其余函数的定义语句
                    Flow.adjustBaseblocks();
                    Flow.copySpread();
                    Flow.updateMidCodes();
                }
                // 打印流图情况
                // System.out.println(Flow.flowChart2Str());
                // Flow.calculateSuccessors();
                // Flow.calculatePrecursors();
                //Flow.useDefAnalyze();
                //Flow.activeAnalyze();
                // System.out.println(Flow.successors2Str());
                // System.out.println(Flow.useDef2Str());
                Flow.killGenAnalyze();
                Flow.arriveDefAnalyze();
                Flow.calculateAllUses();
                Flow.defUseChainAnalyze();
                Flow.defUseWebAnalyze();
                //Flow.innerTempUseDefAnalyze(); // 基本块内的临时变量使用定义分析
//
//            // 变量重命名 再次活跃变量分析
                Flow.webRename();

                // 再次扫描中间代码
                // 摘循环 如果循环内部没有副作用语句 直接跳转到循环尾
                // 将程序组织成链表
                if (OptimizerSwitch.LOOP_REMOVE_OPT) {
                    Flow.loopOptimize();
                    Flow.createFlow(midCodes);
                    Flow.adjustBaseblocks();
                }


                if (OptimizerSwitch.GLOBAL_CONST_SPREAD) {
                    Flow.globalConstSpread();
                }

                if (OptimizerSwitch.COPY_SPREAD_OPT) {
                    // 将基本块按照顺序排布
                    // 将函数入口的基本块补上函数定义语句
                    // &global的基本块中删除其余函数的定义语句
                    Flow.copySpread();
                    Flow.updateMidCodes();
                }
            }
            else {
                // 弱分配
                // 应先把基本块内部数据流优化完毕 再做基本块间数据流
                Flow.createFlow(midCodes);
                // 常量传播和复制传播会改变变量的作用范围 所以先做传播 再做使用定义分析
                if (OptimizerSwitch.COPY_SPREAD_OPT) {
                    // 将基本块按照顺序排布
                    // 将函数入口的基本块补上函数定义语句
                    // &global的基本块中删除其余函数的定义语句
                    Flow.adjustBaseblocks();
                    Flow.copySpread();
                    Flow.updateMidCodes();
                }
            }
            Flow.createFlow(midCodes);
            // 重新绘制流图
            Flow.useDefAnalyze();
            Flow.activeAnalyze();
            Flow.conflictAnalyze();
            Flow.initRegAllocPool();

            // System.out.println(Flow.flowChart2Str());

            // 先做复制传播 再死代码删除
            // 部分中间代码经过赋值传播会退化成赋值语句 如 a+1 --> 3+1 ---> 4
            // a + b --> a + 0 --> a
            // 基本块内对代码进行删除 与替换 按照代码的顺序排列基本块
            // 再拼接成原来的代码

            // 死亡代码删除
            if (OptimizerSwitch.DEAD_CODE_DELETE_OPT) {
                Flow.deadCodeDelete();
            }

            Flow.adjustBaseblocks();
            // System.out.println(Flow.flowChart2Str());

            if (OptimizerSwitch.DELETE_MAIN_TAIL_OPT) {
                Flow.deleteMainTail();
                // 基本块中删除了代码 更新全局的中间代码
                Flow.updateMidCodes();
            }

            if (OptimizerSwitch.NO_SIDE_EFFECT_DELETE_OPT) {
                // 对无副作用变量的赋值可以删除
                Flow.clearSideEffect();
                Flow.NoSideEffectDeleteOpt();
            }


        }

        // 打印中间代码
        System.out.println(MidCodeFactory.MidCode2Str());

        MipsFactory.createMips(midCodes);

        fileOutput.output(MipsFactory.mipsToString());

        // 打印汇编代码
        // System.out.println(MipsFactory.mipsToString());
        fileOutput.closeFile();

    }

    private static ArrayList<Token> getTokens(Lexer lexer) {
        ArrayList<Token> tokens = new ArrayList<>();
        try { while (true) { tokens.add(lexer.getToken()); } } catch (EOF e) {}
        return tokens;
    }
}
