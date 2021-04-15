package org.wildfly.modelgraph.registry;

public class Registration {

    public String version;
    public String service;
    public String browser;

    public Registration() {
    }

    public Registration(String version, String service, String browser) {
        this.version = version;
        this.service = service;
        this.browser = browser;
    }
}
