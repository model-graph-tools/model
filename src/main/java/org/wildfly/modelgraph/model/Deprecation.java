package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;

import java.util.Objects;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.REASON;

public class Deprecation {

    public static Deprecation from(Relationship relationship, Node node) {
        Deprecation deprecation = new Deprecation();

        deprecation.reason = relationship.get(REASON).asString();
        deprecation.since = Version.from(node);

        return deprecation;
    }

    public String modelType;
    public String reason;
    public Version since;

    public Deprecation() {
        modelType = getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deprecation that = (Deprecation) o;
        return reason.equals(that.reason) && since.equals(that.since);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason, since);
    }
}
