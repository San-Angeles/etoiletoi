package compiler.interp.parsec.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import compiler.interp.parsec.records.Tuple;

public interface GenericParser<Tokens, A> {

    Optional<Tuple<A, Tokens>> parse(Tokens tokens);

    default GenericParser<Tokens, List<A>> many() {
        return tokens -> {
            var result = new ArrayList<A>();
            var remainder = backtracks(tokens);
            var optional = (Optional<Tuple<A, Tokens>>) null;
            while ((optional = parse(remainder)).isPresent()) {
                var tuple = optional.get();
                result.add(tuple.first());
                remainder = tuple.second();
            }
            return Optional.of(new Tuple<>(result, remainder));
        };
    }

    default GenericParser<Tokens, List<A>> some() {
        return tokens -> {
            var optional = many().parse(backtracks(tokens));
            return optional.get().first().size() >= 1
                    ? optional
                    : Optional.empty();
        };
    }

    default <B, R> GenericParser<Tokens, R> trunk(
            GenericParser<Tokens, B> next,
            Function<A, GenericParser<Tokens, R>> deform,
            Function<A, GenericParser<Tokens, R>> exact) {
        return tokens -> {
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var phase = tupleThis.second();
            var optionalNext = next.parse(backtracks(phase));
            if (optionalNext.isEmpty())
                return deform.apply(tupleThis.first()).parse(phase);
            var tupleNext = optionalNext.get();
            return exact.apply(tupleThis.first()).parse(tupleNext.second());
        };
    }

    default <B, R> GenericParser<Tokens, R> deform(
            GenericParser<Tokens, B> next,
            Function<A, R> deform,
            Function<Tuple<A, B>, R> exact) {
        return tokens -> {
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var phase = tupleThis.second();
            var optionalNext = next.parse(backtracks(phase));
            if (optionalNext.isEmpty()) {
                var result = deform.apply(tupleThis.first());
                return Optional.of(new Tuple<>(result, phase));
            }
            var tupleNext = optionalNext.get();
            var result = new Tuple<>(tupleThis.first(), tupleNext.first());
            return Optional.of(new Tuple<>(exact.apply(result), tupleNext.second()));
        };
    }

    default <B, R> GenericParser<Tokens, R> branch(
            GenericParser<Tokens, B> next,
            Function<A, R> deform,
            Function<A, R> exact) {
        return tokens -> {
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var phase = tupleThis.second();
            var optionalNext = next.parse(backtracks(phase));
            if (optionalNext.isEmpty()) {
                var result = deform.apply(tupleThis.first());
                return Optional.of(new Tuple<>(result, phase));
            }
            var tupleNext = optionalNext.get();
            var result = exact.apply(tupleThis.first());
            return Optional.of(new Tuple<>(result, tupleNext.second()));
        };
    }

    default <B> GenericParser<Tokens, A> branch(
            GenericParser<Tokens, B> next,
            Function<A, A> exact) {
        return tokens -> {
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var phase = tupleThis.second();
            var optionalNext = next.parse(backtracks(phase));
            if (optionalNext.isEmpty()) {
                var result = tupleThis.first();
                return Optional.of(new Tuple<>(result, phase));
            }
            var tupleNext = optionalNext.get();
            var result = exact.apply(tupleThis.first());
            return Optional.of(new Tuple<>(result, tupleNext.second()));
        };
    }


    

    default <B> GenericParser<Tokens, Tuple<A, B>> follow(
            GenericParser<Tokens, B> next) {
        return tokens -> {
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var optionalNext = next.parse(tupleThis.second());
            if (optionalNext.isEmpty())
                return Optional.empty();
            var tupleNext = optionalNext.get();

            var result = new Tuple<>(tupleThis.first(), tupleNext.first());
            return Optional.of(new Tuple<>(result, tupleNext.second()));
        };
    }

    default <B> GenericParser<Tokens, Tuple<A, B>> followLazy(
            Supplier<? extends GenericParser<Tokens, B>> supplier) {
        return tokens -> {
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var optionalNext = supplier.get().parse(tupleThis.second());
            if (optionalNext.isEmpty())
                return Optional.empty();
            var tupleNext = optionalNext.get();

            var result = new Tuple<>(tupleThis.first(), tupleNext.first());
            return Optional.of(new Tuple<>(result, tupleNext.second()));
        };
    }

    default <B> GenericParser<Tokens, B> map(Function<A, B> transform) {
        return tokens -> {
            var optional = this.parse(backtracks(tokens));
            if (optional.isEmpty())
                return Optional.empty();
            var tuple = optional.get();
            var result = transform.apply(tuple.first());
            return Optional.of(new Tuple<>(result, tuple.second()));
        };
    }

    default Tokens backtracks(Tokens tokens) {
        return tokens;
    }

    default GenericParser<Tokens, A> or(GenericParser<Tokens, A> other) {
        return tokens -> {
            var alien = backtracks(tokens);
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isPresent())
                return optionalThis;
            return other.parse(alien);
        };
    }

    default GenericParser<Tokens, A> orLazy(Supplier<? extends GenericParser<Tokens, A>> supplier) {
        return tokens -> {
            var alien = backtracks(tokens);
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isPresent())
                return optionalThis;
            return supplier.get().parse(alien);
        };
    }

    default <B> GenericParser<Tokens, A> skip(
            GenericParser<Tokens, B> ignored) {
        return tokens -> {
            var optionalThis = this.parse(backtracks(tokens));
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var optionalNext = ignored.parse(tupleThis.second());
            if (optionalNext.isEmpty())
                return Optional.empty();
            var tupleNext = optionalNext.get();

            return Optional.of(new Tuple<>(tupleThis.first(), tupleNext.second()));
        };
    }

    default <B> GenericParser<Tokens, A> skipLazy(
            Supplier<? extends GenericParser<Tokens, B>> supplier) {
        return tokens -> {
            var optionalThis = this.parse(tokens);
            if (optionalThis.isEmpty())
                return Optional.empty();
            var tupleThis = optionalThis.get();

            var optionalNext = supplier.get().parse(tupleThis.second());
            if (optionalNext.isEmpty())
                return Optional.empty();
            var tupleNext = optionalNext.get();

            return Optional.of(new Tuple<>(tupleThis.first(), tupleNext.second()));
        };
    }
}
