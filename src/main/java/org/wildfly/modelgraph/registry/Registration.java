package org.wildfly.modelgraph.registry;

import org.wildfly.modelgraph.model.Identity;

public class Registration {

    public String identifier;
    public String productName;
    public String productVersion;
    public String managementVersion;
    public String modelServiceUri;
    public String neo4jBrowserUri;
    public String neo4jBoltUri;

    public Registration() {
    }

    public Registration(Identity identity, String modelServiceUri, String neo4jBrowserUri, String neo4jBoltUri) {
        this.identifier = identity.identifier;
        this.productName = identity.productName;
        this.productVersion = identity.productVersion;
        this.managementVersion = identity.managementVersion;
        this.modelServiceUri = modelServiceUri;
        this.neo4jBrowserUri = neo4jBrowserUri;
        this.neo4jBoltUri = neo4jBoltUri;
    }

    @Override
    public String toString() {
        return String.format("%s, model service %s, neo4j %s, %s",
                identifier, modelServiceUri, neo4jBrowserUri, neo4jBoltUri);
    }
}
