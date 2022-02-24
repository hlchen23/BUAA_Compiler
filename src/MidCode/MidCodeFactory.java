package MidCode;

import MidCode.MidCodeElement.*;

import java.util.ArrayList;
import java.util.Stack;

public class MidCodeFactory {

    private static ArrayList<MidCode> midCodes = new ArrayList<>();
    private static final String autoVar = "#T"; // 自动生成临时变量
    private static int autoVarIndex = 0;
    private static final String autoLabel = "label_"; // 自动生成 (非函数) 标签
    private static int autoLabelIndex = 0;

    // 方便break与continue使用
    private static Stack<String> loop_begins = new Stack<>();
    private static Stack<String> loop_ends = new Stack<>();

    public static String createMidCode(
            Opt opt,
            String ... args
    ) {
        // 返回中间代码的dst
        switch (opt) {
            case ADD:
            case SUB:
            case MULT:
            case DIV:
            case MOD:
            case EQL:
            case NEQ:
            case LSS:
            case LEQ:
            case GRE:
            case GEQ:
                if (args.length == 2) {
                    String auto = createAutoVar();
                    midCodes.add(new VAR(auto));
                    if (opt == Opt.ADD) {midCodes.add(new ADD(args[0],args[1],auto));}
                    else if (opt == Opt.SUB) {midCodes.add(new SUB(args[0],args[1],auto));}
                    else if (opt == Opt.MULT) {midCodes.add(new MULT(args[0],args[1],auto));}
                    else if (opt == Opt.DIV) {midCodes.add(new DIV(args[0],args[1],auto));}
                    else if (opt == Opt.MOD) {midCodes.add(new MOD(args[0],args[1],auto));}
                    else if (opt == Opt.EQL) {midCodes.add(new EQL(args[0],args[1],auto));}
                    else if (opt == Opt.NEQ) {midCodes.add(new NEQ(args[0],args[1],auto));}
                    else if (opt == Opt.LSS) {midCodes.add(new LSS(args[0],args[1],auto));}
                    else if (opt == Opt.LEQ) {midCodes.add(new LEQ(args[0],args[1],auto));}
                    else if (opt == Opt.GRE) {midCodes.add(new GRE(args[0],args[1],auto));}
                    else if (opt == Opt.GEQ) {midCodes.add(new GEQ(args[0],args[1],auto));}
                    return auto;
                }
                else {
                    if (opt == Opt.ADD) {midCodes.add(new ADD(args[0],args[1],args[2]));}
                    else if (opt == Opt.SUB) {midCodes.add(new SUB(args[0],args[1],args[2]));}
                    else if (opt == Opt.MULT) {midCodes.add(new MULT(args[0],args[1],args[2]));}
                    else if (opt == Opt.DIV) {midCodes.add(new DIV(args[0],args[1],args[2]));}
                    else if (opt == Opt.MOD) {midCodes.add(new MOD(args[0],args[1],args[2]));}
                    else if (opt == Opt.EQL) {midCodes.add(new EQL(args[0],args[1],args[2]));}
                    else if (opt == Opt.NEQ) {midCodes.add(new NEQ(args[0],args[1],args[2]));}
                    else if (opt == Opt.LSS) {midCodes.add(new LSS(args[0],args[1],args[2]));}
                    else if (opt == Opt.LEQ) {midCodes.add(new LEQ(args[0],args[1],args[2]));}
                    else if (opt == Opt.GRE) {midCodes.add(new GRE(args[0],args[1],args[2]));}
                    else if (opt == Opt.GEQ) {midCodes.add(new GEQ(args[0],args[1],args[2]));}
                    return null;
                }
            case CONST:
                midCodes.add(new CONST(args[0]));
                return null;
            case VAR:
                midCodes.add(new VAR(args[0]));
                return null;
            case PARA:
                midCodes.add(new PARA_DECLARE(args[0]));
                midCodes.add(new PARA(args[0]));
                return null;
            case CONST_ARRAY:
                if (args.length == 2) {
                    midCodes.add(new CONST_ARRAY(args[0],Integer.valueOf(args[1]))); }
                else {
                    // length == 3
                    midCodes.add(new CONST_ARRAY(args[0],
                            Integer.valueOf(args[1]),Integer.valueOf(args[2]))); }
                return null;
            case VAR_ARRAY:
                if (args.length == 2) {
                    midCodes.add(new VAR_ARRAY(args[0],Integer.valueOf(args[1]))); }
                else {
                    // length == 3
                    midCodes.add(new VAR_ARRAY(args[0],
                            Integer.valueOf(args[1]),Integer.valueOf(args[2]))); }
                return null;
            case PARA_ARRAY:
                if (args.length == 1) {
                    midCodes.add(new PARA_ARRAY_DECLARE(args[0]));
                    midCodes.add(new PARA_ARRAY(args[0])); }
                else {
                    // length == 2
                    midCodes.add(new PARA_ARRAY_DECLARE(args[0],Integer.valueOf(args[1])));
                    midCodes.add(new PARA_ARRAY(args[0],Integer.valueOf(args[1]))); }
                return null;
            case ASSIGN:
                if (args.length == 1) {
                    String auto = createAutoVar();
                    midCodes.add(new VAR(auto));
                    midCodes.add(new ASSIGN(args[0],auto));
                    return auto;
                }
                else {
                    midCodes.add(new ASSIGN(args[0], args[1]));
                    return null;
                }
            case NEG:
                if (args.length == 1) {
                    String auto = createAutoVar();
                    midCodes.add(new VAR(auto));
                    midCodes.add(new NEG(args[0],auto));
                    return auto;
                }
                else {
                    midCodes.add(new NEG(args[0],args[1]));
                    return null;
                }
            case NOT:
                if (args.length == 1) {
                    String auto = createAutoVar();
                    midCodes.add(new VAR(auto));
                    midCodes.add(new NOT(args[0],auto));
                    return auto;
                }
                else {
                    midCodes.add(new NOT(args[0],args[1]));
                    return null;
                }
            case START_PROGRAM:
                midCodes.add(new START_PROGRAM());
                return null;
            case END_PROGRAM:
                midCodes.add(new END_PROGRAM());
                return null;
            case FUN:
                MidCode fun = new FUN(args[0]);
                if (args[1].equals("void")) {
                    ((FUN) fun).type = FUN.Type.VOID;
                }
                else if (args[1].equals("int")) {
                    ((FUN) fun).type = FUN.Type.INT;
                }
                // 末尾的}的行号
                ((FUN) fun).tailNo = Integer.valueOf(args[2]);
                midCodes.add(fun);
                return null;
            case RETURN:
                // return 要记录行号进去
                if (args.length == 2) {
                    MidCode ret = new RETURN(args[0]);
                    // 行号
                    ((RETURN) ret).lineNo = Integer.valueOf(args[1]);
                    midCodes.add(ret);
                }
                else if (args.length == 1){
                    MidCode ret = new RETURN();
                    ((RETURN) ret).lineNo = Integer.valueOf(args[0]);
                    midCodes.add(ret);
                }
                else {
                    // 在void函数中要补一个return确保安全 这个一定不会出现错误
                    MidCode ret = new RETURN();
                    midCodes.add(ret);
                }
                // return语句右面也得有一个自动标签
                midCodes.add(new SUPPLEMENT_LABEL(MidCodeFactory.createAutoLabel()));
                return null;
            case CALL_INT:
                midCodes.add(new CALL_INT(args[0]));
                return null;
            case END_CALL_INT:
                // 真正的call函数指令
                if (args.length == 1) {
                    String auto = createAutoVar();
                    midCodes.add(new VAR(auto));
                    midCodes.add(new END_CALL_INT(args[0],auto));
                    return auto;
                }
                else {
                    midCodes.add(new END_CALL_INT(args[0],args[1]));
                    return null;
                }
            case CALL_VOID:
                midCodes.add(new CALL_VOID(args[0]));
                return null;
            case END_CALL_VOID:
                midCodes.add(new END_CALL_VOID(args[0]));
                return null;
            case PUSH:
                midCodes.add(new PUSH(args[0]));
                return null;
            case GETINT:
                midCodes.add(new GETINT(args[0]));
                return null;
            case PRINT_INT:
                midCodes.add(new PRINT_INT(args[0]));
                return null;
            case PRINT_STR:
                midCodes.add(new PRINT_STR(args[0]));
                return null;
            case BEZ:
                BEZ bez = new BEZ(args[0],args[1]);
                // 不跳转
                String noJump = createAutoLabel();
                bez.setDst_1(noJump);
                midCodes.add(bez);
                midCodes.add(new SUPPLEMENT_LABEL(noJump));
                return null;
            case LABEL:
                midCodes.add(new LABEL(args[0]));
                return null;
            case SUPPLEMENT_LABEL:
                midCodes.add(new SUPPLEMENT_LABEL(args[0]));
                return null;
            case IF:
                midCodes.add(new IF(args[0]));
                return null;
            case ELSE:
                midCodes.add(new ELSE(args[0]));
                return null;
            case END_IF:
                midCodes.add(new END_IF(args[0]));
                return null;
            case GOTO:
                String noGoto = createAutoLabel();
                midCodes.add(new GOTO(args[0]));
                midCodes.add(new SUPPLEMENT_LABEL(noGoto));
                return null;
            case BEGIN_WHILE:
                midCodes.add(new BEGIN_WHILE(args[0]));
                return null;
            case END_WHILE:
                midCodes.add(new END_WHILE(args[0]));
                return null;
            case BREAK:
                String noBreak = createAutoLabel();
                midCodes.add(new BREAK(args[0]));
                midCodes.add(new SUPPLEMENT_LABEL(noBreak));
                return null;
            case CONTINUE:
                String noContinue = createAutoLabel();
                midCodes.add(new CONTINUE(args[0]));
                midCodes.add(new SUPPLEMENT_LABEL(noContinue));
                return null;
            case ARRAY_ADDR_INIT:
                midCodes.add(new ARRAY_ADDR_INIT(args[0]));
                return null;
            case CONST_ARRAY_ADDR_INIT:
                midCodes.add(new CONST_ARRAY_ADDR_INIT(args[0]));
                return null;
            case LOAD_ARRAY:
                if (args.length == 2) {
                    String auto = createAutoVar();
                    midCodes.add(new VAR(auto));
                    midCodes.add(new LOAD_ARRAY(args[0],args[1],auto));
                    return auto;
                }
                else {
                    // length == 3
                    midCodes.add(new LOAD_ARRAY(args[0],args[1],args[2]));
                    return null;
                }
            case STORE_ARRAY:
                midCodes.add(new STORE_ARRAY(args[0],args[1],args[2]));
                return null;
            default:
                return null;
        }
    }

    public static String createAutoVar() {
        return autoVar + (autoVarIndex++);
    }

    public static String createAutoLabel() {
        return autoLabel + (autoLabelIndex++);
    }

    public static String MidCode2Str() {
        String retStr = "";
        for (MidCode midCode : midCodes) {
            retStr += midCode.toString();
            if (midCode.isDelete) {
                retStr += "# ############################## DEAD CODE! #################################\r\n";
            }
            // retStr += "DELETED: " + midCode.isDelete + "\r\n";
        }
        return retStr;
    }

    public static ArrayList<MidCode> getMidCodes() {
        return midCodes;
    }

    public static void enterLoop(String loop_begin, String loop_end) {
        loop_begins.push(loop_begin);
        loop_ends.push(loop_end);
    }

    public static void leaveLoop() {
        loop_begins.pop();
        loop_ends.pop();
    }

    public static String getLoopBegin() {
        return loop_begins.peek();
    }

    public static String getLoopEnd() {
        return loop_ends.peek();
    }
}
