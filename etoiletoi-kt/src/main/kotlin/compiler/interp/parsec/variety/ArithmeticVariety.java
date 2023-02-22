package compiler.interp.parsec.variety;

import static compiler.interp.parsec.bundle.CharacterBundle.*;
import static compiler.interp.parsec.spec.Parser.*;

import java.util.function.Function;

import compiler.interp.parsec.spec.Parser;

public abstract class ArithmeticVariety<T, N> extends PrimaryVariety<T> {

    @SuppressWarnings("unchecked")
    public T fromNumber(N x) {
        return (T) x;
    }

    public abstract N toNumber(T x);

    public abstract T negate(T x);
    public abstract T factorial(T x);
    public abstract T pow(T x, T y);
    public abstract T mul(T x, T y);
    public abstract T div(T x, T y);
    public abstract T mod(T x, T y);
    public abstract T add(T x, T y);
    public abstract T sub(T x, T y);
    public abstract T lsh(T x, T y);
    public abstract T rsh(T x, T y);

    @Override
    public Parser<T> expr() {
        return shiftExpr;
    }

    public Parser<T> negateExact = minus.follow(() -> this.unaryExpr).map(x -> negate(x.second()));
    public Parser<T> factorialExpr = primaryExpr.skip(exclaim).map(this::factorial)
            .or(primaryExpr); // primaryExpr.branch(exclaim, this::factorial);
    public Parser<T> unaryExpr = negateExact.or(factorialExpr);

    public Parser<String> powInf = between(circumflex).map(String::valueOf).or(between(exponent));
    public Parser<Function<T, T>> powExt = extend(powInf, unaryExpr, this::pow);
    public Parser<T> powExpr = reduce(unaryExpr, powExt.many());

    public Parser<Character> mulInf = between(times).or(between(asterisk));
    public Parser<Character> divInf = between(slash).or(between(division));
    public Parser<String> modInf = between(modulo).or(between(percent).map(String::valueOf));
    public Parser<Function<T, T>> mulExt = extend(mulInf, powExpr, this::mul);
    public Parser<Function<T, T>> divExt = extend(divInf, powExpr, this::div);
    public Parser<Function<T, T>> modExt = extend(modInf, powExpr, this::mod);
    public Parser<T> mulExpr = reduce(powExpr, mulExt.or(divExt).or(modExt).many());

    public Parser<Character> addInf = between(plus);
    public Parser<Character> subInf = between(minus);
    public Parser<Function<T, T>> addExt = extend(addInf, mulExpr, this::add);
    public Parser<Function<T, T>> subExt = extend(subInf, mulExpr, this::sub);
    public Parser<T> addExpr = reduce(mulExpr, addExt.or(subExt).many());

    public Parser<String> lshInf = between(string(">>"));
    public Parser<String> rshInf = between(string("<<"));
    public Parser<Function<T, T>> lshExt = extend(lshInf, addExpr, this::lsh);
    public Parser<Function<T, T>> rshExt = extend(rshInf, addExpr, this::rsh);
    public Parser<T> shiftExpr = reduce(addExpr, lshExt.or(rshExt).many());

}
