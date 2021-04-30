package org.wildfly.modelgraph.model;

import org.neo4j.driver.Record;
import org.neo4j.driver.Value;

final class DeprecationRepository {

    static Deprecation mapDeprecation(Record record, boolean anemic) {
        Value deprecated = record.get("d");
        Value version = record.get("v");
        if (deprecated.isNull() || version.isNull()) {
            return null;
        }
        return Deprecation.from(deprecated.asRelationship(), version.asNode(), anemic);
    }

    private DeprecationRepository() {
    }
}
