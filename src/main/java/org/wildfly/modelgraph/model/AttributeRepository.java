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
import static org.wildfly.modelgraph.model.DeprecationRepository.mapDeprecation;
import static org.wildfly.modelgraph.model.ModelDescriptionConstants.ADDRESS;

@ApplicationScoped
class AttributeRepository {

    private static final String ATTRIBUTES = "" +
            "MATCH (a:Attribute)<-[:HAS_ATTRIBUTE]-(r:Resource) " +
            "WHERE a.name =~ $regex " +
            "OPTIONAL MATCH (a)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN r, a, d, v";

    private static final String DEPRECATED = "" +
            "MATCH (r:Resource)-[:HAS_ATTRIBUTE]->(a:Attribute)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "WHERE v.ordinal >= $ordinal " +
            "RETURN r, a, d, v";

    @Inject
    Driver driver;

    /**
     * Returns flat attributes (w/o relations to other attributes) which match the given name (case-insensitive).
     */
    Multi<Attribute> attributes(String name) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(ATTRIBUTES, parameters("regex", regex(name)));
                    RxResult result = tx.run(query);
                    return Multi.createFrom().publisher(result.records())
                            .map(record -> {
                                Attribute attribute = Attribute.from(record.get("a").asNode());
                                attribute.definedIn = record.get("r").asNode().get(ADDRESS).asString();
                                attribute.deprecation = mapDeprecation(record);
                                return attribute;
                            });
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }

    /**
     * Returns deprecated attributes (w/o relations to other attributes).
     */
    Multi<Attribute> deprecated(Version version) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(DEPRECATED, parameters("ordinal", version.ordinal));
                    RxResult result = tx.run(query);
                    return Multi.createFrom().publisher(result.records())
                            .map(record -> {
                                Attribute attribute = Attribute.from(record.get("a").asNode());
                                attribute.definedIn = record.get("r").asNode().get(ADDRESS).asString();
                                attribute.deprecation = mapDeprecation(record);
                                return attribute;
                            });
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }
}
