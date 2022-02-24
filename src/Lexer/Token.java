package Lexer;

public class Token {
    private String rawString;
    private TokenType tokenType;
    private int lineNo;
    private int value; // <number>.
    private int count; // <FormatString>

    private boolean valid = true;

    public Token() {

    }

    public Token(String rawString,
                 TokenType tokenType,
                 int lineNo,
                 int value) {
        this.rawString = rawString;
        this.tokenType = tokenType;
        this.lineNo = lineNo;
        this.value = value;
    }

    @Override
    public String toString() {
        return "======Token======\n" +
                "rawString: " + rawString + "\n" +
                "tokenType: " + tokenType + "\n" +
                "lineNo: " + lineNo + "\n" +
                "value: " + value + "\n" +
                "valid: " + valid + "\n" +
                "======EndToken======";
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public void setRawString(String rawString) {
        this.rawString = rawString;
    }

    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setLineNo(int lineNo) {
        this.lineNo = lineNo;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getLineNo() {
        return lineNo;
    }

    public String getRawString() {
        return rawString;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public int getValue() {
        return value;
    }

    public boolean isValid() {
        return valid;
    }

    public int getCount() {
        return count;
    }
}
