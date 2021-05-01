package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import java.util.Objects;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.*;

public class Identity extends Model {

    public static Identity from(Node node) {
        Identity identity = new Identity();
        mapId(node, identity);

        identity.identifier = node.get(IDENTIFIER).asString(null);
        identity.productName = node.get(PRODUCT_NAME).asString(null);
        identity.productVersion = node.get(PRODUCT_VERSION).asString(null);
        identity.managementVersion = node.get(MANAGEMENT_VERSION).asString(null);

        return identity;
    }

    public String identifier;
    public String productName;
    public String productVersion;
    public String managementVersion;

    public Identity() {
        // Required by JSON-B
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identity identity = (Identity) o;
        return identifier.equals(identity.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier;
    }
}
