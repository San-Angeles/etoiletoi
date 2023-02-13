package compiler.interp.parsec.records;

public record Tuple<F, S> (F first, S second) {

    @Override
    public String toString() {
        return String.format("(%s, %s)", first, second);
    }

    public static <A, B> Tuple<A, B> of(A a, B b) {
        return new Tuple<A, B>(a, b);
    }
}