package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

public abstract class Model {

    static void mapId(Node node, Model model) {
        model.id = String.valueOf(node.id());
    }

    public String id;
    public String modelType;

    public Model() {
        modelType = getClass().getSimpleName();
    }
}
