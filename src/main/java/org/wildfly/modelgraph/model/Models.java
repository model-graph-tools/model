package org.wildfly.modelgraph.model;

import java.util.List;

public class Models {

    public List<Resource> resources;
    public List<Attribute> attributes;
    public List<Operation> operations;
    public List<Capability> capabilities;

    public Models() {
        // Required by JSON-B
    }
}
