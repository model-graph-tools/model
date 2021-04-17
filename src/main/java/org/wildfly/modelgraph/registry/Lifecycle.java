package org.wildfly.modelgraph.registry;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.wildfly.modelgraph.model.Version;
import org.wildfly.modelgraph.model.VersionRepository;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationScoped
public class Lifecycle {

    private static final Logger log = Logger.getLogger(Lifecycle.class);

    @Inject
    @ConfigProperty(name = "mgt.model.uri")
    String modelUri;

    @Inject
    @ConfigProperty(name = "mgt.neo4j.browser.uri")
    String neo4jBrowser;

    @Inject
    @RestClient
    RegistryClient registryClient;

    @Inject
    VersionRepository versionRepository;

    private final AtomicBoolean registered = new AtomicBoolean(false);

    void onStop(@Observes ShutdownEvent ev) {
        unregister();
    }

    @Scheduled(every = "10s")
    void tryToRegister() {
        if (!registered.get()) {
            register();
        }
    }

    private void register() {
        log.debug("register()");
        versionRepository.version().subscribe().with(version -> {
            log.debugf("Got version %s from version repository", version);
            Registration registration = new Registration(version.toString(), modelUri, neo4jBrowser);
            log.debugf("Try to register %s %s %s", version, modelUri, neo4jBrowser);
            registryClient.register(registration).subscribe().with(response -> {
                log.debugf("Registration service returned %d", response.getStatus());
                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    registered.set(true);
                    log.infof("Registered %s", registration.version);
                } else {
                    Response.StatusType statusInfo = response.getStatusInfo();
                    log.errorf("Unable to register %s: %d %s",
                            version, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
                }
            }, e -> log.errorf("Unable to register %s: %s", version, e.getMessage()));
        }, e -> log.errorf("Unable to register: Cannot read version: %s", e.getMessage()));
    }

    private void unregister() {
        log.debug("unregister()");
        try {
            Version version = versionRepository.version()
                    .onFailure().invoke(e -> log.errorf(
                            "Unable to unregister: Cannot read version: %s", e.getMessage()))
                    .await().atMost(Duration.ofSeconds(2));
            log.debugf("Got version %s from version repository", version);
            log.debugf("Try to unregister version %s", version);
            Response response = registryClient.unregister(version.toString());
            log.debugf("Registration service returned %d", response.getStatus());
            Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo.getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()) {
                registered.set(false);
                log.infof("Unregistered %s", version);
            } else {
                log.errorf("Unable to unregister %s: %d %s",
                        version, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
            }
        } catch (Exception e) {
            log.errorf("Unable to unregister: %s", e.getMessage());
        }
    }
}
