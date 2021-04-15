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

    private static final Logger LOGGER = Logger.getLogger(Lifecycle.class);

    @Inject
    @ConfigProperty(name = "quarkus.http.host")
    String host;

    @Inject
    @ConfigProperty(name = "quarkus.http.port")
    Integer port;

    @Inject
    @ConfigProperty(name = "mgt.neo4j.browser")
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
        versionRepository.version().subscribe().with(version -> {
            String service = "http://" + host + ":" + port;
            Registration registration = new Registration(version.toString(), service, neo4jBrowser);
            registryClient.register(registration).subscribe().with(response -> {
                if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
                    LOGGER.infof("Registered %s", registration.version);
                    registered.set(true);
                } else {
                    Response.StatusType statusInfo = response.getStatusInfo();
                    LOGGER.errorf("Unable to register %s: %d %s",
                            version, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
                }
            }, e -> LOGGER.errorf("Unable to register %s: %s", version, e.getMessage()));
        }, e -> LOGGER.errorf("Unable to register: Cannot read version: %s", e.getMessage()));
    }

    private void unregister() {
        try {
            Version version = versionRepository.version()
                    .onFailure().invoke(e -> LOGGER.errorf(
                            "Unable to unregister: Cannot read version: %s", e.getMessage()))
                    .await().atMost(Duration.ofSeconds(2));
            Response response = registryClient.unregister(version.toString());
            Response.StatusType statusInfo = response.getStatusInfo();
            if (statusInfo.getStatusCode() == Response.Status.NO_CONTENT.getStatusCode()) {
                LOGGER.infof("Unregistered %s", version);
                registered.set(false);
            } else {
                LOGGER.errorf("Unable to unregister %s: %d %s",
                        version, statusInfo.getStatusCode(), statusInfo.getReasonPhrase());
            }
        } catch (Exception e) {
            LOGGER.errorf("Unable to unregister: %s", e.getMessage());
        }
    }
}
