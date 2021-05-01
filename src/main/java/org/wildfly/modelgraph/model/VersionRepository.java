package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.neo4j.driver.reactive.RxResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
class VersionRepository {

    private static final String VERSIONS = "" +
            "MATCH (v:Version) " +
            "RETURN v";

    @Inject
    Driver driver;

    /**
     * Returns versions.
     */
    Multi<Version> versions() {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(VERSIONS);
                    RxResult result = tx.run(query);
                    return Multi.createFrom().publisher(result.records())
                            .map(record -> Version.from(record.get("v").asNode()));
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }
}
