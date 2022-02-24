package Error;

import java.util.ArrayList;
import java.util.Arrays;

public enum ErrorType {
    INVALID_CHAR,
    NAME_REDIFNED,
    NAME_UNDIFNED,
    PARAS_NUM_MISMATCH,
    PARAS_TYPE_MISMATCH,
    VOIDFUNC_RETURN,
    FUNC_NORETURN,
    CONST_VARIED,
    LACK_SEMI,
    LACK_RPARENT,
    LACK_RBRACK,
    PRINTF_NUM_MISMATCH,
    BREAK_CONTINUE_NO_LOOP;

    static private ArrayList<String> errorCode = new ArrayList<>(
            Arrays.asList("a","b","c","d","e","f","g","h","i","j","k","l","m")
    );

    @Override
    public String toString() {
        return errorCode.get(super.ordinal());
    }
}
