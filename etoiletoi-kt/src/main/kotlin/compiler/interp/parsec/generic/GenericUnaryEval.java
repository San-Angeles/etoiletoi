package compiler.interp.parsec.generic;

public interface GenericUnaryEval<T, V> {
    V eval(T target);
}
