package Grammer;

import Grammer.GrammarClass.CompUnit;
import Lexer.Token;
import MyException.EOF;

import java.util.ArrayList;

public class Grammar {

    private static ArrayList<Token> tokens;
    private static int pointer = 0;
    public static Token token; // 全局量 表示目前读到的token
    public static int loop = 0; // 全局量 表示循环的层数
    public static int dim = 0; // 全局量 表示当前解析的层数

    public static String belong = "&global"; // 记录当前所在的函数名称

    public static String rename = "@"; // 块内的变量要重命名
    public static int autoRenameIndex = 0; // 块内的变量重命名序号

    private static int pointer_mark;
    private static Token token_mark;

//    public static Stack<Node> stack = new Stack<>();

    public static void init(ArrayList<Token> tokens) {
        Grammar.tokens = tokens;
        Grammar.tokens.add(token);
    }

    public static void mark() {
        pointer_mark = pointer;
        token_mark = token;
    }

    public static void restore() {
        while (pointer != pointer_mark) {
            retract();
        }
    }

    public static void nextToken() throws EOF {
        if (pointer >= tokens.size()) {
            throw new EOF();
        }
        token = tokens.get(pointer++);
        OutputList.addToList(token);
    }

    public static void retract() {
        pointer -= 1;
        if (pointer-1 >= 0) {
            token = tokens.get(pointer - 1);
        }
        else { token = null; }
        while (OutputList.pop() instanceof GrammarType);
    }

//    public static void analyze() throws EOF {
//        // 分析新的元素要压栈 始终分析栈顶元素 每个元素结束的时候要出栈
//        stack.push(new CompUnit());
//        while (!stack.empty()) {
//            Node peekNode = stack.peek();
//            peekNode.analyze();
//        }
//    }

    public static void analyze() throws EOF {
        CompUnit compUnit = new CompUnit();
        compUnit.analyze();
        // compUnit.semanticCheck(); // 纯语法树检查重定义 且未区分func与非func
        compUnit.makeTable();
    }
}
