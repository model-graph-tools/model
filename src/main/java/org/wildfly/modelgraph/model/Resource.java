package org.wildfly.modelgraph.model;

import org.neo4j.driver.types.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.wildfly.modelgraph.model.ModelDescriptionConstants.*;

@SuppressWarnings("WeakerAccess")
public class Resource extends NamedModel {

    public static Resource from(Node node) {
        Resource resource = new Resource();
        mapId(node, resource);
        mapName(node, resource);

        resource.singleton = node.get(SINGLETON).isNull() ? null : node.get(SINGLETON).asBoolean();
        resource.address = node.get(ADDRESS).asString();
        resource.description = node.get(DESCRIPTION).asString(null);
        String cd = node.get(CHILD_DESCRIPTIONS).asString(null);
        if (cd != null) {
            String[] segments = cd.split("\\^");
            if (segments.length != 0) {
                for (String segment : segments) {
                    String[] keyValue = segment.split("\\|");
                    if (keyValue.length == 2) {
                        if (resource.childDescriptions == null) {
                            resource.childDescriptions = new HashMap<>();
                        }
                        resource.childDescriptions.put(keyValue[0], keyValue[1]);
                    }
                }
            }
        }
        return resource;
    }

    // properties
    public Boolean singleton;
    public String address;
    public String description;
    public Map<String, String> childDescriptions;
    public Deprecation deprecation;

    // relations
    public Resource parent;
    public List<Resource> children;
    public List<Attribute> attributes;
    public List<Operation> operations;
    public List<Capability> capabilities;

    public Resource() {
        // Required by JSON-B
    }
}
