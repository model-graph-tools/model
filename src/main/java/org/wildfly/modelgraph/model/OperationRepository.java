package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.types.Node;
import org.reactivestreams.Publisher;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

import static org.neo4j.driver.Values.parameters;
import static org.wildfly.modelgraph.model.Cypher.regex;
import static org.wildfly.modelgraph.model.DeprecationRepository.mapDeprecation;

@ApplicationScoped
class OperationRepository {

    private static final String OPERATIONS = "" +
            "MATCH (p)<-[:ACCEPTS*0..]-(o:Operation) " +
            "WHERE o.name =~ $regex AND o.global = TRUE " +
            "OPTIONAL MATCH (o)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN o, p, NULL AS address, d, v " +
            "UNION " +
            "MATCH (p)<-[:ACCEPTS*0..]-(o:Operation)<-[:PROVIDES]-(r:Resource) " +
            "WHERE o.name =~ $regex AND o.global = FALSE " +
            "OPTIONAL MATCH (o)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN o, p, r.address AS address, d, v";

    private static final String DEPRECATED = "" +
            "MATCH (p)<-[:ACCEPTS*0..]-(o:Operation) " +
            "MATCH (o)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "WHERE v.ordinal >= $ordinal AND o.global = TRUE " +
            "RETURN o, p, NULL AS address, d, v " +
            "UNION " +
            "MATCH (p)<-[:ACCEPTS*0..]-(o:Operation)<-[:PROVIDES]-(r:Resource) " +
            "MATCH (o)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "WHERE v.ordinal >= $ordinal AND o.global = FALSE " +
            "RETURN o, p, r.address AS address, d, v";

    @Inject
    Driver driver;

    /**
     * Returns flat operations (w/o parameters) which match the given name (case-insensitive).
     */
    Multi<Operation> operations(String name, boolean anemic) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(OPERATIONS, parameters("regex", regex(name)));
                    return operationRecords(tx.run(query), anemic);
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }

    /**
     * Returns deprecated operations (w/o parameters).
     */
    Multi<Operation> deprecated(Version version, boolean anemic) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(DEPRECATED, parameters("ordinal", version.ordinal));
                    return operationRecords(tx.run(query), anemic);
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }

    private Publisher<Operation> operationRecords(RxResult result, boolean anemic) {
        return Multi.createFrom().publisher(result.records())
                .map(record -> {
                    Operation operation = Operation.from(record.get("o").asNode(), anemic);
                    operation.deprecation = mapDeprecation(record, anemic);
                    if (!record.get("address").isNull()) {
                        operation.providedBy = record.get("address").asString();
                    }
                    Parameter p = null;
                    Value v = record.get("p");
                    if (v != null) {
                        Node parameterNode = v.asNode();
                        if (parameterNode.hasLabel("Parameter")) {
                            p = Parameter.from(v.asNode(), anemic);
                        }
                    }
                    return new OperationAndParameter(operation, p);
                })
                .group().by(
                        oap -> oap.operator,
                        oap -> oap.parameter
                )
                .onItem().transformToMulti(grouped -> grouped.collect().asList()
                        .map(parameters -> {
                            for (Optional<Parameter> parameter : parameters) {
                                parameter.ifPresent(p -> grouped.key().addParameter(p));
                            }
                            return grouped.key();
                        })
                        .toMulti())
                .merge();
    }

    private static final class OperationAndParameter {

        private final Operation operator;
        private final Optional<Parameter> parameter;

        OperationAndParameter(Operation operator, Parameter parameter) {
            this.operator = operator;
            this.parameter = Optional.ofNullable(parameter);
        }
    }
}