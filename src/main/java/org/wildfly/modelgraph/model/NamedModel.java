package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.NAME;

public abstract class NamedModel extends Model {

    static void mapName(Node node, NamedModel model) {
        model.name = node.get(NAME).asString();
    }

    public String name;
}
