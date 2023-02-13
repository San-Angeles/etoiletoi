package compiler.interp.parsec.bundle;

import static compiler.interp.parsec.bundle.CharacterBundle.*;
import static compiler.interp.parsec.bundle.IntegerBundle.*;

import java.util.function.Function;

import compiler.interp.parsec.spec.DetInteger;
import compiler.interp.parsec.spec.Parser;
import compiler.interp.parsec.variety.ArithmeticVariety;

public interface DoubleBundle {

    Function<Integer, Double> intToDouble = x -> (double) x;

    Parser<Double> decimalExact = integer.follow(ddot).follow(integer)
            .map(x -> Double.parseDouble(x.first().first() + "." + x.second()));
    Parser<Double> decimal = decimalExact.or(integer.map(intToDouble));

    ArithmeticVariety<Double, Double> variety = new ArithmeticVariety<Double, Double>() {

        @Override
        public Double fromNumber(Double x) {
            return x;
        }

        @Override
        public Double toNumber(Double x) {
            return x;
        }

        @Override
        public Parser<Double> primary() {
            return decimal;
        }

        @Override
        public Double negate(Double x) {
            return -x;
        }

        @Override
        public Double factorial(Double x) {
            return DetInteger.factorial(toNumber(x).intValue()).doubleValue();
        }

        @Override
        public Double pow(Double x, Double y) {
            return Math.pow(x, y);
        }

        @Override
        public Double mul(Double x, Double y) {
            return x * y;
        }

        @Override
        public Double div(Double x, Double y) {
            return x / y;
        }

        @Override
        public Double mod(Double x, Double y) {
            return x % y;
        }

        @Override
        public Double add(Double x, Double y) {
            return x + y;
        }

        @Override
        public Double sub(Double x, Double y) {
            return x - y;
        }

        @Override
        public Double lsh(Double x, Double y) {
            return (double) (x.intValue() << y.intValue());
        }

        @Override
        public Double rsh(Double x, Double y) {
            return (double) (x.intValue() >> y.intValue());
        }
    };
}
