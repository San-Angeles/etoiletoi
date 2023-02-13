package compiler.interp.parsec.spec;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import compiler.interp.parsec.generic.GenericInfixEval;
import compiler.interp.parsec.generic.GenericParser;
import compiler.interp.parsec.records.Tuple;

public interface Parser<A> extends GenericParser<StringBuffer, A> {

    default Optional<Tuple<A, StringBuffer>> parse(String tokens) {
        return parse(new StringBuffer(tokens));
    }

    @Override
    default Parser<List<A>> many() {
        return tokens -> GenericParser.super.many().parse(tokens);
    }

    default Parser<String> asterisk() {
        return tokens -> {
            var buffer = new StringBuffer();
            var remainder = tokens;
            var optional = (Optional<Tuple<A, StringBuffer>>) null;
            while ((optional = parse(remainder)).isPresent()) {
                var tuple = optional.get();
                buffer.append(tuple.first());
                remainder = tuple.second();
            }
            return Optional.of(new Tuple<>(buffer.toString(), remainder));
        };
    }

    @Override
    default Parser<List<A>> some() {
        return tokens -> GenericParser.super.some().parse(tokens);
    }

    default Parser<String> plus() {
        return tokens -> {
            var optional = asterisk().parse(tokens);
            return optional.get().first().length() >= 1
                    ? optional
                    : Optional.empty();
        };
    }

    default <R> Parser<R> trunk(Parser<?> next,
            Function<A, Parser<R>> deform, Function<A, Parser<R>> exact) {
        return tokens -> GenericParser.super.trunk(next,
                x -> deform.apply(x), x -> exact.apply(x)).parse(tokens);
    }

    default <B, R> Parser<R> deform(Parser<B> next,
            Function<A, R> deform, Function<Tuple<A, B>, R> exact) {
        return tokens -> GenericParser.super.deform(next, deform, exact).parse(tokens);
    }

    default <B, R> Parser<R> branch(Parser<B> next,
            Function<A, R> deform, Function<A, R> exact) {
        return tokens -> GenericParser.super.branch(next, deform, exact).parse(tokens);
    }

    default <B> Parser<A> branch(Parser<B> next, Function<A, A> exact) {
        return tokens -> GenericParser.super.branch(next, exact).parse(tokens);
    }

    default <B> Parser<Tuple<A, B>> follow(Parser<B> next) {
        return tokens -> GenericParser.super.follow(next).parse(tokens);
    }

    default <B> Parser<Tuple<A, B>> follow(Supplier<Parser<B>> supplier) {
        return tokens -> GenericParser.super.followLazy(() -> supplier.get()).parse(tokens);
    }

    default <B> Parser<B> map(Function<A, B> transform) {
        return tokens -> GenericParser.super.map(transform).parse(tokens);
    }

    default <B> Parser<A> skip(Parser<B> ignored) {
        return tokens -> GenericParser.super.skip(ignored).parse(tokens);
    }

    default Parser<A> or(Parser<A> other) {
        return tokens -> GenericParser.super.or(other).parse(tokens);
    }

    @Override
    default StringBuffer backtracks(StringBuffer tokens) {
        return new StringBuffer(tokens);
    }

    static <L, I> Parser<I> left(Parser<L> leftIgnored, Parser<I> origin) {
        return leftIgnored.follow(origin).map(Tuple::second);
    }

    static <L, I> Parser<I> leftLazy(Parser<L> leftIgnored, Supplier<Parser<I>> origin) {
        return leftIgnored.follow(origin).map(Tuple::second);
    }

    static <L, I, R> Parser<I> between(Parser<L> leftIgnored, Parser<I> origin,
            Parser<R> rightIgnored) {
        return left(leftIgnored, origin.skip(rightIgnored));
    }

    static <L, I, R> Parser<I> between(Parser<L> leftIgnored, Supplier<Parser<I>> origin,
            Parser<R> rightIgnored) {
        return leftIgnored.follow(origin).skip(rightIgnored).map(Tuple::second);
    }

    static <I, T> Parser<I> between(Parser<I> origin, Parser<T> twosideIgnored) {
        return between(twosideIgnored, origin, twosideIgnored);
    }

    static <L, I, R, V> Parser<V> infix(Parser<L> left, Parser<I> operator, Parser<R> right,
            GenericInfixEval<L, R, V> f) {
        return left.follow(operator).follow(right).map(x -> f.eval(x.first().first(), x.second()));
    }

    static <U, I, V> Parser<V> infix(Parser<U> twoside, Parser<I> operator,
            GenericInfixEval<U, U, V> f) {
        return infix(twoside, operator, twoside, f);
    }

    static <L, I, R, V> Parser<Function<L, V>> extend(Parser<I> operator, Parser<R> right,
            GenericInfixEval<L, R, V> f) {
        return operator.follow(right).map(x -> l -> f.eval(l, x.second()));
    }

    static <L, I, R, V> Parser<Function<L, V>> extend(Parser<I> operator, Supplier<Parser<R>> right,
            GenericInfixEval<L, R, V> f) {
        return operator.follow(right).map(x -> l -> f.eval(l, x.second()));
    }

    static <L, V extends L> Parser<V> reduce(Parser<L> left, Parser<List<Function<L, V>>> fs,
            Function<L, V> identity) {
        return left.follow(fs.map(x -> x.stream().reduce(identity, (s, t) -> t.compose(s))))
                .map(x -> x.second().apply(x.first()));
    }

    static <T> Parser<T> reduce(Parser<T> left, Parser<List<Function<T, T>>> fs) {
        return reduce(left, fs, Function.<T>identity());
    }

}
