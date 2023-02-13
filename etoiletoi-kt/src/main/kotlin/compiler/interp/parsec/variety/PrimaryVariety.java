package compiler.interp.parsec.variety;

import static compiler.interp.parsec.bundle.CharacterBundle.*;
import static compiler.interp.parsec.spec.Parser.*;

import compiler.interp.parsec.spec.Parser;

public abstract class PrimaryVariety<T> {

    public abstract Parser<T> primary();
    public abstract Parser<T> expr();

    public Parser<T> parenExpr = between(parenLeft, this::expr, parenRight);
    public Parser<T> primaryExpr = left(spaces, parenExpr.or(primary()));

}
