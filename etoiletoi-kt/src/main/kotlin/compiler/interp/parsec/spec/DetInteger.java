package compiler.interp.parsec.spec;

import java.math.BigInteger;

import compiler.interp.parsec.generic.GenericUnaryEval;

public interface DetInteger {

    final BigInteger[] factorials = {
            BigInteger.ONE, 
            BigInteger.ONE, 
            BigInteger.TWO, 
            BigInteger.valueOf(6), 
            BigInteger.valueOf(24), 
            BigInteger.valueOf(120), 
            BigInteger.valueOf(720), 
            BigInteger.valueOf(5040), 
            BigInteger.valueOf(40320), 
            BigInteger.valueOf(362880), 
            BigInteger.valueOf(3628800)
    };
    final int factsMaxIndex = factorials.length - 1;
    final BigInteger factsMax = factorials[factsMaxIndex];

    static BigInteger factorial(int x) {
        return x > factsMaxIndex
                ? factsMax.multiply(pochhammer(x, x - factsMaxIndex))
                : factorials[x];
    }

    static BigInteger pochhammer(int x, int n) {
        if (n == 1)
            return BigInteger.valueOf(x);
        else if (n == x)
            n = x - 1;
        var result = BigInteger.valueOf(x);
        int last = x - n + 1;
        while (x-- > last)
            result = result.multiply(BigInteger.valueOf(x));
        return result;
    }

    static BigInteger factorialByLoop(int n) {
        var result = BigInteger.valueOf(n);
        while (n-- > 1)
            result = result.multiply(BigInteger.valueOf(n));
        return result;
    }

    GenericUnaryEval<Integer, BigInteger> factorial = DetInteger::factorial;
}
