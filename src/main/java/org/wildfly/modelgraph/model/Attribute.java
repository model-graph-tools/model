package org.wildfly.modelgraph.model;

import java.util.HashSet;
import java.util.Set;

import org.neo4j.driver.types.Node;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.*;

public class Attribute extends Parameter {

    public static Attribute from(Node node, boolean anemic) {
        Attribute attribute = new Attribute();
        if (!anemic) {
            mapId(node, attribute);
        }
        mapName(node, attribute);
        mapParameter(node, attribute);

        attribute.accessType = node.get(ACCESS_TYPE).asString(null);
        attribute.alias = node.get(ALIAS).asString(null);
        attribute.attributeGroup = node.get(ATTRIBUTE_GROUP).asString(null);
        attribute.defaultValue = node.get(DEFAULT).asString(null);
        attribute.restartRequired = node.get(RESTART_REQUIRED).asString(null);
        attribute.storage = node.get(STORAGE).asString(null);

        return attribute;
    }

    // properties
    public String accessType;
    public String alias;
    public String attributeGroup;
    public String defaultValue;
    public String restartRequired;
    public String storage;

    // relations
    public Set<Attribute> attributes;
    public String definedIn;

    public Attribute() {
        // Required by JSON-B
    }

    void addAttribute(Attribute attribute) {
        if (attributes == null) {
            attributes = new HashSet<>();
        }
        attributes.add(attribute);
    }
}
