package compiler.interp.parsec.bundle;

import static compiler.interp.parsec.bundle.CharacterBundle.*;
import static compiler.interp.parsec.spec.Parser.*;

import java.util.Optional;
import java.util.function.Function;

import compiler.interp.parsec.generic.GenericInfixEval;
import compiler.interp.parsec.records.Tuple;
import compiler.interp.parsec.spec.Parser;

public interface IntegerBundle {

    GenericInfixEval<String, String, String> ul = (x, y) -> x + y;
    Parser<Function<String, String>> ulExt = extend(token('_'), digits, ul);

    Parser<Character> zero1 = character(x -> x == '0' || x == '1');
    Parser<String> bin = left(string("0b"), zero1.plus());

    Parser<Character> zero7 = character(x -> x >= '0' && x <= '7');
    Parser<String> oct = left(string("0o"), zero7.plus());

    Parser<Character> AF = character(x -> (x >= 'A' && x <= 'F') || (x >= 'a' && x <= 'f'));
    Parser<String> hex = left(string("0x"), digit.or(AF).plus());

    Parser<String> adicInf = between(string("adic"));

    static Parser<String> adicOf(Integer radix) {
        return (switch (radix) {
            case Integer n when 2 <= n && n <= 10 ->
                character(x -> x >= '0' && x <= '0' + (radix - 1));
            case Integer n when 11 <= n && n <= 36 ->
                ((Function<Integer, Parser<Character>>) (offset -> digit
                        .or(character(x -> (x >= 'A' && x <= 'A' + offset) ||
                                (x >= 'a' && x <= 'a' + offset)))))
                        .apply(radix - 11);
            default -> throw new IllegalArgumentException(
                    "Unexpected value: " + radix + " for radix");
        }).plus();
    }

    static Optional<Tuple<Integer, StringBuffer>> adicTo(StringBuffer tokens) {
        var radix = digits.skip(adicInf).map(Integer::parseInt);
        var optional = radix.parse(tokens);
        if (optional.isEmpty())
            return Optional.empty();
        var tuple = optional.get();
        var base = tuple.first();
        var value = adicOf(base);
        return value.map(x -> Integer.parseInt(x, base)).parse(tuple.second());
    }

    Parser<Integer> integer = bin.map(x -> Integer.parseInt(x, 2))
            .or(oct.map(x -> Integer.parseInt(x, 8)))
            .or(hex.map(x -> Integer.parseInt(x, 16)))
            .or(IntegerBundle::adicTo)
            .or(reduce(digits, ulExt.many()).map(Integer::parseInt));

}
