package Error;

import Grammer.Grammar;
import Lexer.Token;
import SymTable.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class MyError {

    public static ArrayList<MyError> myErrors = new ArrayList<>();

    private Token token;
    private int lineNo;
    private ErrorType errorType;
    private String msg;

    public MyError() {

    }

    public MyError(Token token, ErrorType errorType) {
        this.token = token;
        this.errorType = errorType;
        this.lineNo = token.getLineNo();
    }

    public static void addErrors(MyError myError) {
        myErrors.add(myError);
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public void setErrorType(ErrorType errorType) {
        this.errorType = errorType;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return lineNo + " " + errorType + "\r\n";
    }

    public static void sort() {
        Collections.sort(myErrors,new Comparator<MyError>() {
            @Override
            public int compare(MyError e1, MyError e2) {
                if (e1.lineNo <= e2.lineNo) {
                    return -1;
                }
                return 1;
            }
        });
    }

    public static String errors2String(boolean log) {
        String str = "";
        if (log) {
            for (MyError e : myErrors) {
                str += String.format("%-128s","Line " + e.lineNo + ": " + e.msg) +String.format("%8s",e.toString());
            }
        }
        else {
            for (MyError e : myErrors) {
                str += e.toString();
            }
        }
        return str;
    }

    public static void add_lack_rbrack() {
        MyError error = new MyError();
        error.setErrorType(ErrorType.LACK_RBRACK);
        error.setToken(Grammar.token);
        error.setMsg(String.format("Expect ] but get %s.",Grammar.token.getRawString()));
        Grammar.retract();
        error.setLineNo(Grammar.token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_continue_no_loop(Token token) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.BREAK_CONTINUE_NO_LOOP);
        error.setToken(token);
        error.setMsg("Use \"continue\" in no loop.");
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_break_no_loop(Token token) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.BREAK_CONTINUE_NO_LOOP);
        error.setToken(token);
        error.setMsg("Use \"break\" in no loop.");
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_lack_semi() {
        MyError error = new MyError();
        error.setErrorType(ErrorType.LACK_SEMI);
        error.setToken(Grammar.token);
        error.setMsg(String.format("Expect ; but get %s.",Grammar.token.getRawString()));
        Grammar.retract();
        error.setLineNo(Grammar.token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_lack_rparent() {
        // 传本应该是) 但是读到的token
        MyError error = new MyError();
        error.setErrorType(ErrorType.LACK_RPARENT);
        error.setToken(Grammar.token);
        error.setMsg(String.format("Expect ) but get %s.",Grammar.token.getRawString()));
        Grammar.retract();
        error.setLineNo(Grammar.token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_printf_num_mismatch(Token printf_token,int need,int give) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.PRINTF_NUM_MISMATCH);
        error.setToken(printf_token);
        error.setMsg(String.format("Expect %d exps but get %d exps in printf.",need,give));
        error.setLineNo(printf_token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_reDef(Token token) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.NAME_REDIFNED);
        error.setToken(token);
        error.setMsg(String.format("name %s redefined!",token.getRawString()));
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_unDef(Token token) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.NAME_UNDIFNED);
        error.setToken(token);
        error.setMsg(String.format("name %s undefined!",token.getRawString()));
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_const_varied(Token token) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.CONST_VARIED);
        error.setToken(token);
        error.setMsg(String.format("can't vary const %s",token.getRawString()));
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_params_num_mismatch(Token token,int need,int give) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.PARAS_NUM_MISMATCH);
        error.setToken(token);
        error.setMsg(String.format(
                "params num mismatch in func %s, where %d are needed but %d are given.",
                token.getRawString(),need,give));
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_params_type_mismatch(Token token, DataType need, DataType give) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.PARAS_TYPE_MISMATCH);
        error.setToken(token);
        error.setMsg(String.format(
                "params type mismatch in func %s where type %s is needed but type %s is given.",
                token.getRawString(),need,give));
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    public static void add_voidFunc_return(Token token) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.VOIDFUNC_RETURN);
        error.setToken(token);
        error.setMsg("mismatch return in a void function!");
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    // 数据流专用
    public static void add_voidFunc_return(int lineNo) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.VOIDFUNC_RETURN);
        error.setMsg("mismatch return in a void function!");
        error.setLineNo(lineNo);
        MyError.addErrors(error);
    }

    public static void add_func_noReturn(Token token) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.FUNC_NORETURN);
        error.setToken(token);
        error.setMsg("no valid return in a int function!");
        error.setLineNo(token.getLineNo());
        MyError.addErrors(error);
    }

    // 数据流专用
    public static void add_func_noReturn(int lineNo) {
        MyError error = new MyError();
        error.setErrorType(ErrorType.FUNC_NORETURN);
        error.setMsg("no valid return in a int function!");
        error.setLineNo(lineNo);
        MyError.addErrors(error);
    }
}
