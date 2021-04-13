package org.wildfly.modelgraph.registry;

public class ModelService {

    public String version;
    public String url;

    public ModelService() {
    }

    public ModelService(String version, String url) {
        this.version = version;
        this.url = url;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s", version, url);
    }
}
