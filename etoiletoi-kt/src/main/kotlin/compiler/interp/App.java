package compiler.interp;

import static compiler.interp.parsec.bundle.CharacterBundle.*;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.Character.UnicodeBlock;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Supplier;

import compiler.interp.parsec.bundle.CharacterBundle;
import compiler.interp.parsec.bundle.DoubleBundle;
import compiler.interp.parsec.bundle.IntegerBundle;
import compiler.interp.parsec.records.Tuple;
import compiler.interp.parsec.spec.DetInteger;
import compiler.interp.parsec.variety.ArithmeticVariety;
import compiler.interp.script.ScriptBundle;

public interface App {

    static void main(String[] args) throws Exception {

        // var rdName = ScriptBundle.name
        // .map(x -> x.toString()).map(x -> (Supplier<String>) () -> {
        // System.out.println("case rd");
        // return x;
        // });
        // var caseX = rdName.skip(CharacterBundle.asterisk);

        // var parser = caseX.or(rdName).map(x -> x.get());
        // showParser(parser.parse("aba"));

        // showParser(ScriptBundle.text.parse("'aa\\'inner\\'bb'"));

        // System.out.println(token('ℤ').parse("ℤ"));

        // System.out.println(new Scanner(System.in).next());
        // System.out.println("\u2124");
        // System.out.println("π");

        // System.out.println(new BigDecimal(1.0));

        // var buff = new StringBuffer("puts: _ -> _ 'hello' ");
        // showParser(ScriptBundle.expr.map(x -> x.get()).parse(buff));

        repl();
    }

    static void repl() {
        // (11 > 2) + 7 -> 8.0
        // 11 < 2 == 0 -> true
        // 11 < 2 == 3 > 7 --> true
        // 1 - 1 ? 2 + 99 : 3 - 77 --> -74.0
        // 1 ? (2 ? (3 ? 4 : 5) : 6) : 7 --> 4
        // 19 & 22 xor 33 | 77 --> 127
        // a = 1 + (b = e) --> 3.718281828459045 (1 + e)
        // e = 2 --> Assignment to constant `e`.
        // (a = 1) && (b = a + 2) --> 3
        // x = 36 adic ustc4z --> 1.862383283E9

        // why call too many
        // x = exp: d -> d -1

        // e ^ (pi * 163 ^ (1/2))
        // var buff = new StringBuffer("exp: d -> d 2");
        // var optional = ScriptBundle.expr.parse(buff);
        // System.out.println(optional.get().first());

        // > exp: d -> d pi * sqrt: d -> d 163
        // 2.6253741264076826E17

        // > exp: d -> d 2 * (acos: d -> d 0) * sqrt: d -> d 163
        // 2.6253741264076826E17

        var scanner = new Scanner(System.in);
        loop: while (true) {
            System.out.printf("> ");
            var line = scanner.nextLine();
            switch (line) {
                case ":q":
                    break loop;
                case ".v":
                    System.out.println(ScriptBundle.Context.variables);
                default:
                    var buff = new StringBuffer(line);
                    showParser(ScriptBundle.expr.map(x -> x.get()).parse(buff));
            }
        }
        scanner.close();
    }

    static <F, S> void showParser(Optional<Tuple<F, S>> optional) {
        if (optional.isEmpty()) {
            System.out.println(optional);
            return;
        }
        showParser(optional.get());
    }

    static <F, S> void showParser(Tuple<F, S> tuple) {
        // System.out.println("match: " + tuple.first());
        // System.out.println("tails: " + tuple.second());
        // System.out.println();
        System.out.println("out: " + tuple.first());
    }

}
