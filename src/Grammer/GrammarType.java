package Grammer;

public enum GrammarType {
    CompUnit,
    Decl, // 不输出
    ConstDecl,
    BType, // 不输出
    ConstDef,
    ConstInitVal,
    VarDecl,
    VarDef,
    InitVal,
    FuncDef,
    MainFuncDef,
    FuncType,
    FuncFParams,
    FuncFParam,
    Block,
    BlockItem, // 不输出
    Stmt,
    Exp,
    Cond,
    LVal,
    PrimaryExp,
    Number,
    UnaryExp,
    UnaryOp,
    FuncRParams,
    MulExp,
    AddExp,
    RelExp,
    EqExp,
    LAndExp,
    LOrExp,
    ConstExp;

    @Override
    public String toString() {
        return "<" + super.toString() + ">";
    }
}
