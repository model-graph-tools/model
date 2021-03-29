package org.wildfly.modelgraph.model;

final class Iterators {

    static <T> T last(Iterable<T> elements) {
        T last = null;
        for (T element : elements) {
            last = element;
        }
        return last;
    }

    private Iterators() {
    }
}
