package compiler.interp.parsec.generic;

public interface GenericInfixEval<L, R, V> {
    V eval(L left, R right);

    default V reverse(R right, L left) {
        return eval(left, right);
    }
}
