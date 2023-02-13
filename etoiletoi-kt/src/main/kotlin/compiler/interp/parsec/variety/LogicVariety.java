package compiler.interp.parsec.variety;

import static compiler.interp.parsec.bundle.CharacterBundle.*;
import static compiler.interp.parsec.spec.Parser.*;

import java.math.BigDecimal;
import java.util.function.Function;

import compiler.interp.parsec.generic.GenericInfixEval;
import compiler.interp.parsec.spec.InfixEval;
import compiler.interp.parsec.spec.Parser;

public abstract class LogicVariety<T, N> extends ArithmeticVariety<T, N> {

    @SuppressWarnings("unchecked")
    public T fromBoolean(Boolean x) {
        return (T) x;
    }

    public abstract Boolean toBoolean(T x);

    @Override
    public Parser<T> expr() {
        return condExpr;
    }

    public Boolean eq(T x, T y) {
        return x.equals(y);
    }

    public abstract Boolean lt(T x, T y);

    public Boolean gt(T x, T y) {
        return !le(x, y);
    }

    public Boolean le(T x, T y) {
        return lt(x, y) || eq(x, y);
    }

    public Boolean ge(T x, T y) {
        return gt(x, y) || eq(x, y);
    }

    public Parser<Character> ltInf = between(token('<'));
    public Parser<Character> gtInf = between(token('>'));

    public Parser<String> eqInf = between(string("=="));
    public Parser<String> neInf = between(string("!="));
    public Parser<String> leInf = between(string("<="));
    public Parser<String> geInf = between(string(">="));

    public Parser<Boolean> relExact = infix(shiftExpr, ltInf, this::lt)
            .or(infix(shiftExpr, gtInf, this::gt))
            .or(infix(shiftExpr, eqInf, this::eq))
            .or(infix(shiftExpr, leInf, this::le))
            .or(infix(shiftExpr, geInf, this::ge));
    public Parser<T> relExpr = relExact.map(this::fromBoolean).or(shiftExpr);

    public Parser<Function<T, T>> eqExt = extend(eqInf, relExpr, (x, y) -> fromBoolean(eq(x, y)));
    public Parser<Function<T, T>> neExt = extend(neInf, relExpr, (x, y) -> fromBoolean(!eq(x, y)));
    public Parser<T> eqExpr = reduce(relExpr, eqExt.or(neExt).many());

    public Parser<T> equalExpr() {
        return eqExpr;
    }

    public abstract T applyInt(T x, T y, InfixEval<Integer> operator);

    public T bitAnd(T x, T y) {
        return applyInt(x, y, (a, b) -> a & b);
    }

    public T bitXor(T x, T y) {
        return applyInt(x, y, (a, b) -> a ^ b);
    }

    public T bitOr(T x, T y) {
        return applyInt(x, y, (a, b) -> a | b);
    }

    public Parser<Character> bitAndInf = between(token('&'));
    public Parser<Function<T, T>> bitAndExt = extend(bitAndInf, equalExpr(), this::bitAnd);
    public Parser<T> bitAndExpr = reduce(equalExpr(), bitAndExt.many());

    public Parser<String> bitXorInf = between(string("xor"));
    public Parser<Function<T, T>> bitXorExt = extend(bitXorInf, bitAndExpr, this::bitXor);
    public Parser<T> bitXorExpr = reduce(bitAndExpr, bitXorExt.many());

    public Parser<Character> bitOrInf = between(token('|'));
    public Parser<Function<T, T>> bitOrExt = extend(bitOrInf, bitXorExpr, this::bitOr);
    public Parser<T> bitOrExpr = reduce(bitXorExpr, bitOrExt.many());

    public T and(T x, T y) {
        return toBoolean(x) ? y : x;
    }

    public T or(T x, T y) {
        return toBoolean(x) ? x : y;
    }

    public T cond(T test, T left, T right) {
        return toBoolean(test) ? left : right;
    }

    public Parser<String> andInf = between(string("&&"));
    public Parser<Function<T, T>> andExt = andInf.follow(bitOrExpr)
            .map(x -> l -> and(l, x.second()));

    public Parser<T> andExpr = reduce(bitOrExpr, andExt.many());

    public Parser<String> orInf = between(string("||"));
    public Parser<Function<T, T>> orExt = extend(orInf, andExpr, this::or);
    public Parser<T> orExpr = reduce(andExpr, orExt.many());

    public Parser<T> condFlow() {
        return orExpr;
    }

    public Parser<T> condExpr = orExpr.skip(between(hook))
            .follow(this::condFlow).skip(between(colon)).follow(this::condFlow)
            .map(x -> cond(x.first().first(), x.first().second(), x.second()))
            .or(orExpr);

}
