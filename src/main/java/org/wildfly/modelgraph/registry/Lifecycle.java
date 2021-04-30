package org.wildfly.modelgraph.registry;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.scheduler.Scheduled;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;
import org.wildfly.modelgraph.model.Identity;
import org.wildfly.modelgraph.model.IdentityRepository;

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
    @ConfigProperty(name = "mgt.model.service.uri")
    String modelServiceUri;

    @Inject
    @ConfigProperty(name = "mgt.neo4j.browser.uri")
    String neo4jBrowserUri;

    @Inject
    @ConfigProperty(name = "mgt.neo4j.bolt.uri")
    String neo4jBoltUri;

    @Inject
    @RestClient
    RegistryClient registryClient;

    @Inject
    IdentityRepository identityRepository;

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
        identityRepository.identity(false).subscribe().with(identity -> {
            log.debugf("Got %s from identity repository", identity);
            Registration registration = new Registration(identity, modelServiceUri, neo4jBrowserUri, neo4jBoltUri);
            log.debugf("Register %s", registration);
            registryClient.register(registration).subscribe().with(response -> {
                log.debugf("Registration service returned %d", response.getStatus());
                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    registered.set(true);
                    log.infof("Registered %s", registration);
                } else {
                    Response.StatusType statusInfo = response.getStatusInfo();
                    log.errorf("Unable to register %s: %d %s",
                            identity, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
                }
            }, e -> log.errorf("Unable to register %s: %s", identity, e.getMessage()));
        }, e -> log.errorf("Unable to register: Cannot read identity: %s", e.getMessage()));
    }

    private void unregister() {
        log.debug("unregister()");
        try {
            Identity identity = identityRepository.identity(false)
                    .onFailure().invoke(e -> log.errorf(
                            "Unable to unregister: Cannot read identity: %s", e.getMessage()))
                    .await().atMost(Duration.ofSeconds(2));
            log.debugf("Got %s from identity repository", identity);
            log.debugf("Try to unregister %s", identity);
            Response response = registryClient.unregister(identity.toString());
            log.debugf("Registration service returned %d", response.getStatus());
            Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo.getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()) {
                registered.set(false);
                log.infof("Unregistered %s", identity);
            } else {
                log.errorf("Unable to unregister %s: %d %s",
                        identity, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
            }
        } catch (Exception e) {
            log.errorf("Unable to unregister: %s", e.getMessage());
        }
    }
}
