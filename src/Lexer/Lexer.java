package Lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import Error.MyError;
import Error.ErrorType;
import MyException.EOF;

public class Lexer {

    private String source = "";
    private String path = "testfile.txt";

    private int lineNo = 1; // 当前解析行号
    private int pointer = 0; // 字符流指针
    private char ch;

    private HashMap<String,TokenType> reservedTable;
    private HashMap<String,TokenType> symbolTable;
    private HashSet<Character> alpha;
    private HashSet<Character> alnum;
    private HashSet<Character> num;

    public Lexer() throws IOException {
        _readSource();
        _initializeReservedTable();
        _initializeSymbolTable();
        _initializeAlphaNum();
        _clearNotes();
    }

    private void _readSource() throws IOException {
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        while (bufferedReader.ready()) {
            // 可以统一使用\n作为换行符
            source += bufferedReader.readLine() + "\n"; }
        source += "    ";
        bufferedReader.close();
        fileReader.close();
    }

    private void _initializeReservedTable() {
        reservedTable = new HashMap<>();
        reservedTable.put("main",   TokenType.MAINTK);
        reservedTable.put("const",  TokenType.CONSTTK);
        reservedTable.put("int",    TokenType.INTTK);
        reservedTable.put("break",  TokenType.BREAKTK);
        reservedTable.put("continue",TokenType.CONTINUETK);
        reservedTable.put("if",     TokenType.IFTK);
        reservedTable.put("else",   TokenType.ELSETK);
        reservedTable.put("while",  TokenType.WHILETK);
        reservedTable.put("getint", TokenType.GETINTTK);
        reservedTable.put("printf", TokenType.PRINTFTK);
        reservedTable.put("return", TokenType.RETURNTK);
        reservedTable.put("void",   TokenType.VOIDTK);
    }

    private void _initializeSymbolTable() {
        symbolTable = new HashMap<>();
        symbolTable.put("!",TokenType.NOT);
        symbolTable.put("&&",TokenType.AND);
        symbolTable.put("||",TokenType.OR);
        symbolTable.put("+",TokenType.PLUS);
        symbolTable.put("-",TokenType.MINU);
        symbolTable.put("*",TokenType.MULT);
        symbolTable.put("/",TokenType.DIV);
        symbolTable.put("%",TokenType.MOD);
        symbolTable.put("<",TokenType.LSS);
        symbolTable.put("<=",TokenType.LEQ);
        symbolTable.put(">",TokenType.GRE);
        symbolTable.put(">=",TokenType.GEQ);
        symbolTable.put("==",TokenType.EQL);
        symbolTable.put("!=",TokenType.NEQ);
        symbolTable.put("=",TokenType.ASSIGN);
        symbolTable.put(";",TokenType.SEMICN);
        symbolTable.put(",",TokenType.COMMA);
        symbolTable.put("(",TokenType.LPARENT);
        symbolTable.put(")",TokenType.RPARENT);
        symbolTable.put("[",TokenType.LBRACK);
        symbolTable.put("]",TokenType.RBRACK);
        symbolTable.put("{",TokenType.LBRACE);
        symbolTable.put("}",TokenType.RBRACE);
    }

    private void _initializeAlphaNum() {
        alpha = new HashSet<>(
                Arrays.asList(
                        '_',
                        'a','b','c','d','e','f','g',
                        'h','i','j','k','l','m','n',
                        'o','p','q','r','s','t',
                        'u','v','w','x','y','z',
                        'A','B','C','D','E','F','G',
                        'H','I','J','K','L','M','N',
                        'O','P','Q','R','S','T',
                        'U','V','W','X','Y','Z'
                )
        );
        alnum = new HashSet<>(
                Arrays.asList(
                        '_',
                        'a','b','c','d','e','f','g',
                        'h','i','j','k','l','m','n',
                        'o','p','q','r','s','t',
                        'u','v','w','x','y','z',
                        'A','B','C','D','E','F','G',
                        'H','I','J','K','L','M','N',
                        'O','P','Q','R','S','T',
                        'U','V','W','X','Y','Z',
                        '0','1','2','3','4',
                        '5','6','7','8','9'
                )
        );
        num = new HashSet<>(
                Arrays.asList(
                        '0','1','2','3','4',
                        '5','6','7','8','9'
                )
        );
    }

    private void _getCh() throws EOF {
        if (pointer >= source.length()) {
            throw new EOF();
        }
        ch = source.charAt(pointer++);
        if (ch == '\n') {
            lineNo += 1;
        }
    }

    private void _passNull() throws EOF {
        while (
                (ch == '\n')
                || (ch == '\r')
                || (ch == '\t')
                || (ch == '\b')
                || (ch == '\f')
                || (ch == ' ')
        ) {
            _getCh();
        }
    }

    private boolean isValidChar() throws EOF {
        // '空格','!'
        // 40~126
        // (,),*,+,逗号,-,.,/,0-9
        // :,;,<,=,>,?,@,[,\,],^,_,`
        // a-z,A-Z
        // {,|,},~
        // \n必须一起出现 \不合法
        // %d合法 %不合法
        // \在合法范围 %不在
        if (ch == '%') {
            _getCh();
            if (ch == 'd') {
                // 合法
                _retract();
                return true;
            }
            else {
                _retract();
                return false;
            }
        }
        else if (((ch >= '(') && (ch <= '~'))
                || (ch == ' ') || (ch == '!')) {
            if (ch == '\\') {
                _getCh();
                if (ch == 'n') {
                    _retract();
                    return true;
                }
                else {
                    _retract();
                    return false;
                }
            }
            else {
                return true;
            }
        }
        else {
            return false;
        }
    }

    private void _retract() {
        // 结尾回滚一个字符
        if (ch == '\n') {
            lineNo -= 1;
        }
        pointer -= 1;
        if (pointer-1 >= 0) {
            ch = source.charAt(pointer - 1);
        }
    }

    private void _clearNotes() {
        // 字符串""之间的// /*注意判别
        // 不涉及注释方面的错误
        // clear注释
        // 洗注释的时候注意换行 块注释
        String sourceOld = source;
        source = "";

        // 只可能在其中一种状态
        boolean lineNotes = false;
        boolean blockNotes = false;
        boolean inStr = false; // 是否在字符串状态

        for (int i = 0; i < sourceOld.length(); i++) {
            if (inStr) {
                if (sourceOld.charAt(i) == '"') {
                    inStr = false;
                }
                // 无条件保留字符
                source += sourceOld.charAt(i);
            }
            else if (lineNotes) {
                // 检测行尾解除行注释
                if (sourceOld.charAt(i) == '\n') {
                    lineNotes = false;
                    source += sourceOld.charAt(i);
                }
            }
            else if (blockNotes) {
                // 检测 "*/" 解除块注释
                if (sourceOld.charAt(i) == '*') {
                    if (i + 1 < sourceOld.length() && sourceOld.charAt(i+1) == '/') {
                        blockNotes = false;
                        i++;
                    }
                }
                else if (sourceOld.charAt(i) == '\n') {
                    source += '\n';
                }
            }
            else {
                // 非注释状态,非字符串状态 保留字符
                // 检测 "//" "/*" 并进入注释状态
                // 检测 " 并进入字符串状态
                if (sourceOld.charAt(i) == '/') {
                    // 存在 i+1 个
                    if (i + 1 < sourceOld.length()) {
                        // 行注释
                        if (sourceOld.charAt(i + 1) == '/') {
                            lineNotes = true;
                            i++;
                        }
                        // 块注释
                        else if (sourceOld.charAt(i + 1) == '*') {
                            blockNotes = true;
                            i++;
                        } else {
                            source += sourceOld.charAt(i);
                        }
                    } else {

                        source += sourceOld.charAt(i);
                    }
                }
                else if (sourceOld.charAt(i) == '"') {
                    inStr = true;
                    source += sourceOld.charAt(i);
                }
                else {
                    source += sourceOld.charAt(i);
                }
            }
        }
    }

    public Token getToken() throws EOF {
        String rawString = "";
        Token token = new Token();
        _getCh();
        _passNull();
        // reserved, identity
        if (alpha.contains(ch)) {
            token.setLineNo(lineNo);
            do {
                rawString += ch;
                _getCh();
            } while (alnum.contains(ch));
            token.setRawString(rawString);
            if (reservedTable.containsKey(rawString)) {
                token.setTokenType(reservedTable.get(rawString));
            } else {
                token.setTokenType(TokenType.IDENFR);
            }
        }
        // IntConst
        else if (num.contains(ch)) {
            token.setLineNo(lineNo);
            do {
                rawString += ch;
                _getCh();
            } while (num.contains(ch));
            token.setRawString(rawString);
            token.setTokenType(TokenType.INTCON);
            token.setValue(Integer.valueOf(rawString));
        }
        // FormatString
        else if (ch == '"') {
            int count = 0;
            MyError error = new MyError();
            token.setLineNo(lineNo);
            error.setLineNo(lineNo);

            rawString += ch;
            _getCh();
            boolean invalidChar = false;
            while (ch != '"') {
                if (!isValidChar()) {
                    invalidChar = true;
                    token.setValid(false);
                    error.setErrorType(ErrorType.INVALID_CHAR);
                    error.setMsg(String.format("Invalid char %c.",ch));
                }
                rawString += ch;
                if (ch == '%') {count++;} // 记录合法的%d的个数
                _getCh();
            }
            rawString += ch;
            _getCh();
            if (invalidChar) {
                // 保证字符串内部出现多个非法字符时应当只报一个错误
                MyError.addErrors(error);
            }
            token.setCount(count);
            token.setRawString(rawString);
            token.setTokenType(TokenType.STRCON);
        }
        else if (ch == '&') {
            token.setLineNo(lineNo);
            rawString += ch;
            _getCh();
            if (ch == '&') {
                rawString += ch;
                _getCh();
                token.setRawString(rawString);
                token.setTokenType(TokenType.AND);
            }
        }
        else if (ch == '|') {
            token.setLineNo(lineNo);
            rawString += ch;
            _getCh();
            if (ch == '|') {
                rawString += ch;
                _getCh();
                token.setRawString(rawString);
                token.setTokenType(TokenType.OR);
            }
        }
        else if (ch == '<') {
            token.setLineNo(lineNo);
            rawString += ch;
            _getCh();
            if (ch == '=') {
                rawString += ch;
                _getCh();
                token.setRawString(rawString);
                token.setTokenType(TokenType.LEQ);
            }
            else {
                token.setRawString(rawString);
                token.setTokenType(TokenType.LSS);
            }
        }
        else if (ch == '>') {
            token.setLineNo(lineNo);
            rawString += ch;
            _getCh();
            if (ch == '=') {
                rawString += ch;
                _getCh();
                token.setRawString(rawString);
                token.setTokenType(TokenType.GEQ);
            }
            else {
                token.setRawString(rawString);
                token.setTokenType(TokenType.GRE);
            }
        }
        else if (ch == '=') {
            token.setLineNo(lineNo);
            rawString += ch;
            _getCh();
            if (ch == '=') {
                rawString += ch;
                _getCh();
                token.setRawString(rawString);
                token.setTokenType(TokenType.EQL);
            }
            else {
                token.setRawString(rawString);
                token.setTokenType(TokenType.ASSIGN);
            }
        }
        else if (ch == '!') {
            token.setLineNo(lineNo);
            rawString += ch;
            _getCh();
            if (ch == '=') {
                rawString += ch;
                _getCh();
                token.setRawString(rawString);
                token.setTokenType(TokenType.NEQ);
            }
            else {
                token.setRawString(rawString);
                token.setTokenType(TokenType.NOT);
            }
        }
        else {
            token.setLineNo(lineNo);
            rawString += ch;
            _getCh();
            token.setRawString(rawString);
            token.setTokenType(symbolTable.get(rawString));
        }

        // 回滚
        _retract();
        return token;
    }
}
