package org.wildfly.modelgraph.model;

final class Cypher {

    static String regex(String value) {
        return "(?i).*" + value.toLowerCase() + ".*";
    }

    private Cypher() {
    }
}
