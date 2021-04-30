package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import java.util.HashSet;
import java.util.Set;

public class Capability extends NamedModel {

    public static Capability from(Node node, boolean anemic) {
        Capability capability = new Capability();
        if (!anemic) {
            mapId(node, capability);
        }
        mapName(node, capability);

        return capability;
    }

    // relations
    public Set<String> declaredBy;

    public Capability() {
        // Required by JSON-B
    }

    public void addDeclaredBy(String resource) {
        if (declaredBy == null) {
            declaredBy = new HashSet<>();
        }
        declaredBy.add(resource);
    }
}
