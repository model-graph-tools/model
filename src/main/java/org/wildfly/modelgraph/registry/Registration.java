package org.wildfly.modelgraph.registry;

public class Registration {

    public String version;
    public String url;

    public Registration() {
    }

    public Registration(String version, String url) {
        this.version = version;
        this.url = url;
    }

    @Override
    public String toString() {
        return String.format("%s @ %s", version, url);
    }
}
