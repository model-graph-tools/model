package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import java.util.Objects;

public abstract class Model {

    static void mapId(Node node, Model model) {
        model.id = String.valueOf(node.id());
    }

    public String id;
    public String modelType;

    public Model() {
        modelType = getClass().getSimpleName();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Model)) {
            return false;
        }
        Model model = (Model) o;
        return Objects.equals(id, model.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
