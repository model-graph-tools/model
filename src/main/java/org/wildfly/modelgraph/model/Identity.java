package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.*;

public class Identity extends Model {

    public static Identity from(Node node, boolean anemic) {
        Identity identity = new Identity();
        if (!anemic) {
            mapId(node, identity);
        }

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
    public String toString() {
        return identifier;
    }
}
