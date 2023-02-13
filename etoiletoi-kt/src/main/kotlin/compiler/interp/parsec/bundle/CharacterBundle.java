package compiler.interp.parsec.bundle;

import java.util.Optional;
import java.util.function.Predicate;

import compiler.interp.parsec.records.Tuple;
import compiler.interp.parsec.spec.Parser;

public interface CharacterBundle {

    static Parser<Character> character(Predicate<Character> pred) {
        return tokens -> {
            var character = (Character) null;
            return tokens.length() > 0 && pred.test(character = tokens.charAt(0))
                    ? Optional.of(new Tuple<>(character, tokens.deleteCharAt(0)))
                    : Optional.empty();
        };
    }

    static Parser<String> string(String string) {
        return tokens -> {
            var length = string.length();
            var total = tokens.toString();
            return tokens.length() >= length && total.startsWith(string)
                    ? Optional.of(new Tuple<>(string, tokens.delete(0, length)))
                    : Optional.empty();
        };
    }
    
    static Parser<String> string(Predicate<Character> start, Predicate<Character> part) {
        return character(start).follow(character(part).asterisk())
                .map(x -> x.first() + x.second());
    }

    static Parser<Character> token(char one) {
        return character(x -> x == one);
    }

    static <I> Parser<I> between(Parser<I> origin) {
        return Parser.between(origin, spaces);
    }

    Parser<Character> any = character(x -> true);
    Parser<Character> space = token(' ');
    Parser<String> spaces = space.asterisk();
    Parser<String> spaceEx = space.plus();

    Parser<Character> inline = character(x -> x != '\n');
    Parser<Character> line = token('\n');
    Parser<String> lines = line.asterisk();

    Parser<Character> digit = character(Character::isDigit);
    Parser<String> digits = digit.plus();

    Parser<Character> letter = character(Character::isLetter);
    Parser<String> letters = letter.plus();

    Parser<Character> ddot = token('.');
    Parser<Character> plus = token('+');
    Parser<Character> minus = token('-');
    Parser<Character> asterisk = token('*');
    Parser<Character> times = token('×');
    Parser<Character> slash = token('/');
    Parser<Character> backslash = token('\\');
    Parser<Character> division = token('÷');
    Parser<Character> percent = token('%');
    Parser<String> modulo = string("mod");
    Parser<Character> comma = token(',');
    Parser<Character> circumflex = token('^');
    Parser<String> exponent = string("**");
    Parser<Character> exclaim = token('!');
    Parser<Character> sim = token('~');
    Parser<Character> grave = token('`');
    Parser<Character> hook = token('?');
    Parser<Character> colon = token(':');
    Parser<Character> vertical = token('|');
    Parser<Character> parenLeft = token('(');
    Parser<Character> parenRight = token(')');
    Parser<Character> bracketLeft = token('[');
    Parser<Character> bracketRight = token(']');
    Parser<Character> curlieLeft = token('{');
    Parser<Character> curlieRight = token('}');
    Parser<Character> singleQuotes = token('\'');
    Parser<Character> doubleQuotes = token('"');

    Parser<Character> pi = token('π');
}
