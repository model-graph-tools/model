package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.neo4j.driver.reactive.RxResult;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import static org.neo4j.driver.Values.parameters;
import static org.wildfly.modelgraph.model.Cypher.regex;
import static org.wildfly.modelgraph.model.ModelDescriptionConstants.ADDRESS;

@ApplicationScoped
class CapabilityRepository {

    private static final String CAPABILITIES = "" +
            "MATCH (c:Capability)<-[DECLARES_CAPABILITY]-(r:Resource) " +
            "WHERE c.name =~ $regex " +
            "RETURN r, c";

    @Inject
    Driver driver;

    /**
     * Returns capabilities which match the given name (case-insensitive).
     */
    Multi<Capability> capabilities(String name) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(CAPABILITIES, parameters("regex", regex(name)));
                    RxResult result = tx.run(query);
                    return Multi.createFrom().publisher(result.records())
                            .map(record -> {
                                Capability capability = Capability.from(record.get("c").asNode());
                                String resource = record.get("r").asNode().get(ADDRESS).asString();
                                return new CapabilityAndResource(capability, resource);
                            })
                            .group().by(car -> car.capability, car -> car.resource)
                            .onItem().transformToMulti(grouped -> grouped.collect().asList()
                                    .map(resources -> {
                                        for (String resource : resources) {
                                            grouped.key().addDeclaredBy(resource);
                                        }
                                        return grouped.key();
                                    })
                                    .toMulti())
                            .merge();
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }

    private static final class CapabilityAndResource {

        private final Capability capability;
        private final String resource;

        CapabilityAndResource(Capability capability, String resource) {
            this.capability = capability;
            this.resource = resource;
        }
    }
}
