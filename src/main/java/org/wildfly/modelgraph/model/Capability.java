package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Capability extends NamedModel {

    public static Capability from(Node node) {
        Capability capability = new Capability();
        mapId(node, capability);
        mapName(node, capability);

        return capability;
    }

    // relations
    public Set<String> declaredBy;

    public Capability() {
        // Required by JSON-B
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Capability that = (Capability) o;
        return name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public void addDeclaredBy(String resource) {
        if (declaredBy == null) {
            declaredBy = new HashSet<>();
        }
        declaredBy.add(resource);
    }
}
