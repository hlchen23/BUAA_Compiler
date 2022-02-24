package Grammer;

import Lexer.Token;

import java.util.LinkedList;

public class OutputList {

    // 输出队列插入回滚操作频繁 用LinkedList
    static LinkedList<Object> list = new LinkedList<>();

    public static void addToList(Token token) {
        // 词法分析的输出
        list.add(token);
    }

    public static void addToList(GrammarType grammarType) {
        // 语法分析的输出
        list.add(grammarType);
    }

    public static Object pop() {
        // 回滚时删除元素
        return list.removeLast();
    }

    public static String list2String() {
        String retStr = "";
        for (Object obj : list) {
            if (obj instanceof Token) {
                retStr += ((Token) obj).getTokenType() + " " + ((Token) obj).getRawString() + "\r\n";
            }
            else if (obj instanceof GrammarType) {
                retStr += obj + "\r\n";
            }
        }
        return retStr;
    }
}
