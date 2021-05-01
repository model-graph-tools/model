package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;
import org.neo4j.driver.Record;
import org.neo4j.driver.Value;
import org.neo4j.driver.reactive.RxResult;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Path;
import org.neo4j.driver.types.Path.Segment;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;
import static org.neo4j.driver.Values.parameters;
import static org.wildfly.modelgraph.model.Cypher.regex;
import static org.wildfly.modelgraph.model.DeprecationRepository.mapDeprecation;
import static org.wildfly.modelgraph.model.Failure.*;
import static org.wildfly.modelgraph.model.ModelDescriptionConstants.NAME;

@ApplicationScoped
class ResourceRepository {

    private static final String ATTRIBUTES = "" +
            "MATCH g=(r:Resource)-[:HAS_ATTRIBUTE]->(a:Attribute)-[:ALTERNATIVE|CONSISTS_OF|IS_SENSITIVE|REFERENCES_CAPABILITY|REQUIRES*0..1]-(x) " +
            "WHERE r.address = $address " +
            "OPTIONAL MATCH (a)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN g, d, v";

    private static final String CAPABILITIES = "" +
            "MATCH (r:Resource)-->(c:Capability) " +
            "WHERE r.address = $address " +
            "RETURN c";

    private static final String CHILDREN = "" +
            "MATCH (child:Resource)-[:CHILD_OF*1]->(parent:Resource) " +
            "WHERE parent.address = $address " +
            "OPTIONAL MATCH (child)-[d:DEPRECATED_SINCE]->(v:Version)" +
            "RETURN child, d, v";

    private static final String DEPRECATED = "" +
            "MATCH (r:Resource)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "WHERE v.ordinal >= $ordinal " +
            "RETURN r, d, v";

    private static final String NONE_GLOBAL_OPERATIONS = "" +
            "MATCH g=(r:Resource)-[:PROVIDES]->(o:Operation) " +
            "WHERE r.address = $address AND o.global = FALSE " +
            "OPTIONAL MATCH (o)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN g, d, v " +
            "UNION " +
            "MATCH g=(r:Resource)-[:PROVIDES]->(o:Operation)-[:ACCEPTS*0..1]->(p:Parameter)-[:ALTERNATIVE|CONSISTS_OF|IS_SENSITIVE|REFERENCES_CAPABILITY|REQUIRES*0..1]-(x) " +
            "WHERE r.address = $address AND o.global = FALSE " +
            "OPTIONAL MATCH (p)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN g, d, v";

    private static final String OPERATIONS = "" +
            "MATCH g=(r:Resource)-[:PROVIDES]->(o:Operation) " +
            "WHERE r.address = $address " +
            "OPTIONAL MATCH (o)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN g, d, v " +
            "UNION " +
            "MATCH g=(r:Resource)-[:PROVIDES]->(o:Operation)-[:ACCEPTS*0..1]->(p:Parameter)-[:ALTERNATIVE|CONSISTS_OF|IS_SENSITIVE|REFERENCES_CAPABILITY|REQUIRES*0..1]-(x) " +
            "WHERE r.address = $address " +
            "OPTIONAL MATCH (p)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN g, d, v";

    private static final String PARENTS = "" +
            "MATCH g=(child:Resource)-[:CHILD_OF*0..]->(root:Resource) " +
            "WHERE child.address = $address AND NOT (root)-[:CHILD_OF]->() " +
            "OPTIONAL MATCH (child)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN nodes(g), d, v";

    private static final String PARENTS_REVERSE = "" +
            "MATCH g=(child:Resource)-[:CHILD_OF*0..]->(root:Resource) " +
            "WHERE child.address = $address AND NOT (root)-[:CHILD_OF]->() " +
            "OPTIONAL MATCH (child)-[d:DEPRECATED_SINCE]->(v:Version) " +
            "RETURN reverse(nodes(g)), d, v";

    private static final String RESOURCES = "MATCH (r:Resource) " +
            "WHERE r.name =~ $regex " +
            "OPTIONAL MATCH (r)-[d:DEPRECATED_SINCE]->(v:Version)" +
            "RETURN r, d, v";

    @Inject
    Driver driver;

    /**
     * Returns flat resources (w/o attributes or operations) which match the given name (case-insensitive).
     */
    Multi<Resource> resources(String name) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(RESOURCES, parameters("regex", regex(name)));
                    RxResult result = tx.run(query);
                    return Multi.createFrom().publisher(result.records())
                            .map(record -> {
                                Resource resource = Resource.from(record.get("r").asNode());
                                resource.deprecation = mapDeprecation(record);
                                return resource;
                            });
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }

    /**
     * Returns the resource with all parents for the given address.
     */
    Uni<Resource> resource(String address, String skip) {
        Query query = new Query(PARENTS, parameters("address", address));
        return Uni.createFrom()
                .publisher(driver.rxSession().readTransaction(tx -> tx.run(query).records()))
                .ifNoItem().after(TIMEOUT).failWith(timeout())
                .onItem().ifNull().failWith(notFound(address))
                .map(record -> {
                    Resource resource = null;
                    if (record.size() != 0) {
                        Value value = record.get(0);
                        if (!value.isEmpty()) {
                            List<Resource> resources = value.asList(Value::asNode).stream()
                                    .map((Node node) -> Resource.from(node))
                                    .collect(toList());
                            if (!resources.isEmpty()) {
                                Iterator<Resource> iterator = resources.iterator();
                                Iterator<Resource> parentIterator = resources.iterator();
                                parentIterator.next(); // 1st parent
                                while (iterator.hasNext() && parentIterator.hasNext()) {
                                    iterator.next().parent = parentIterator.next();
                                }
                                resource = resources.get(0);
                                resource.deprecation = mapDeprecation(record);
                            } else {
                                throwNotFound(address);
                            }
                        } else {
                            throwNotFound(address);
                        }
                    } else {
                        throwNotFound(address);
                    }
                    return resource;
                })
                .onItem().ifNull().failWith(notFound(address));
    }

    /**
     * Returns the root resource down to the resource with the given address as list
     */
    Uni<List<Resource>> subtree(String address) {
        Query query = new Query(PARENTS_REVERSE, parameters("address", address));
        return Uni.createFrom()
                .publisher(driver.rxSession().readTransaction(tx -> tx.run(query).records()))
                .ifNoItem().after(TIMEOUT).failWith(timeout())
                .map(record -> record.get(0).asList(Value::asNode).stream()
                        .map(node -> {
                            Resource resource = Resource.from(node);
                            resource.deprecation = mapDeprecation(record);
                            return resource;
                        })
                        .collect(toList()));
    }

    /**
     * Reads the child resources for the given address.
     */
    Multi<Resource> children(String address) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(CHILDREN, parameters("address", address));
                    RxResult result = tx.run(query);
                    return Multi.createFrom().publisher(result.records())
                            .map(record -> {
                                Resource resource = Resource.from(record.get("child").asNode());
                                resource.deprecation = mapDeprecation(record);
                                return resource;
                            });
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }

    /**
     * Returns deprecated resources (w/o attributes or operations).
     */
    Multi<Resource> deprecated(Version version) {
        return Multi.createFrom().resource(
                driver::rxSession,
                session -> session.readTransaction(tx -> {
                    Query query = new Query(DEPRECATED, parameters("ordinal", version.ordinal));
                    RxResult result = tx.run(query);
                    return Multi.createFrom().publisher(result.records())
                            .map(record -> {
                                Resource resource = Resource.from(record.get("r").asNode());
                                resource.deprecation = mapDeprecation(record);
                                return resource;
                            });
                }))
                .withFinalizer(session -> {
                    return Uni.createFrom().publisher(session.close());
                });
    }

    // ------------------------------------------------------ assign methods

    /**
     * Reads and adds the child resources to the given resource.
     */
    Uni<Resource> assignChildren(Resource resource) {
        Query query = new Query(CHILDREN, parameters("address", resource.address));
        return Multi.createFrom()
                .publisher(driver.rxSession().readTransaction(tx -> tx.run(query).records()))
                .collect().asList()
                .map(records -> {
                    List<Resource> children = records.stream()
                            .map(record -> {
                                Resource child = Resource.from(record.get("child").asNode());
                                child.deprecation = mapDeprecation(record);
                                return child;
                            })
                            .collect(toList());
                    if (!children.isEmpty()) {
                        resource.children = children.stream()
                                .sorted(Comparator.comparing(r -> r.name))
                                .collect(toList());
                    }
                    return resource;
                });
    }

    /**
     * Reads and assigns the attributes to the given resource.
     */
    Uni<Resource> assignAttributes(Resource resource) {
        Query query = new Query(ATTRIBUTES, parameters("address", resource.address));
        return Multi.createFrom()
                .publisher(driver.rxSession().readTransaction(tx -> tx.run(query).records()))
                .collect().asList()
                .map(records -> {
                    Map<Long, Attribute> attributes = new HashMap<>();
                    List<Attribute> resourceAttributes = new ArrayList<>();
                    for (Record record : records) {
                        Path path = record.get("g").asPath();

                        // collect all attributes
                        for (Node node : path.nodes()) {
                            if (node.hasLabel("Attribute")) {
                                Attribute attribute = Attribute.from(node);
                                attribute.deprecation = mapDeprecation(record);
                                attributes.put(node.id(), attribute);
                            }
                        }

                        // find resource (top-level) attributes
                        if (path.length() == 1) {
                            Attribute attribute = attributes.get(path.end().id());
                            if (attribute != null) {
                                resourceAttributes.add(attribute);
                            }
                        }

                        // resolve complex attributes
                        if (stream(path.spliterator(), false)
                                .skip(1)
                                .allMatch(segment -> "CONSISTS_OF".equals(segment.relationship().type()))) {
                            for (Segment segment : path) {
                                if ("HAS_ATTRIBUTE".equals(segment.relationship().type())) {
                                    continue;
                                }
                                Attribute start = attributes.get(segment.start().id());
                                Attribute end = attributes.get(segment.end().id());
                                if (start != null && end != null) {
                                    start.addAttribute(end);
                                }
                            }
                        }

                        // resolve alternatives, requires and capability references
                        if (path.length() > 1) {
                            Segment segment = Iterators.last(path);
                            if (segment != null) {
                                resolveRelations(segment, attributes);
                            }
                        }
                    }
                    resource.attributes = resourceAttributes.stream()
                            .sorted(Comparator.comparing(a -> a.name))
                            .collect(toList());
                    return resource;
                });
    }

    /**
     * Reads and assigns the operations and parameters to the given resource.
     */
    Uni<Resource> assignOperations(Resource resource, boolean skipGlobalOperations) {
        Query query = new Query(skipGlobalOperations ? NONE_GLOBAL_OPERATIONS : OPERATIONS,
                parameters("address", resource.address));
        return Multi.createFrom()
                .publisher(driver.rxSession().readTransaction(tx -> tx.run(query).records()))
                .collect().asList()
                .map(records -> {
                    Map<Long, Operation> operations = new HashMap<>();
                    Map<Long, Parameter> parameters = new HashMap<>();
                    for (Record record : records) {
                        Path path = record.get("g").asPath();

                        // collect all parameters
                        for (Node node : path.nodes()) {
                            if (node.hasLabel("Parameter")) {
                                Parameter parameter = Parameter.from(node);
                                parameter.deprecation = mapDeprecation(record);
                                parameters.put(node.id(), parameter);
                            }
                        }

                        // collect operations and parameters
                        Iterator<Segment> iterator = path.iterator();
                        Node node = iterator.next().end();
                        Operation operation = operations.get(node.id());

                        if (operation == null) {
                            operation = Operation.from(node);
                            operation.deprecation = mapDeprecation(record);
                            operations.put(node.id(), operation);
                        }
                        if (iterator.hasNext()) {
                            node = iterator.next().end();
                            Parameter parameter = parameters.get(node.id());
                            if (parameter != null) {
                                operation.addParameter(parameter);
                            }
                        }

                        // resolve complex parameters
                        if (stream(path.spliterator(), false)
                                .skip(2)
                                .allMatch(segment -> "CONSISTS_OF".equals(segment.relationship().type()))) {
                            for (Segment segment : path) {
                                if ("PROVIDES".equals(segment.relationship().type()) ||
                                        "ACCEPTS".equals(segment.relationship().type())) {
                                    continue;
                                }
                                Parameter start = parameters.get(segment.start().id());
                                Parameter end = parameters.get(segment.end().id());
                                if (start != null && end != null) {
                                    start.addParameter(end);
                                }
                            }
                        }

                        // resolve alternatives, requires and capability references
                        if (path.length() > 2) {
                            Segment segment = Iterators.last(path);
                            if (segment != null) {
                                resolveRelations(segment, parameters);
                            }
                        }
                    }
                    resource.operations = operations.values().stream()
                            .sorted(Comparator.comparing(o -> o.name))
                            .collect(toList());
                    return resource;
                });
    }

    /**
     * Reads and assigns the capabilities to the given resource.
     */
    Uni<Resource> assignCapabilities(Resource resource) {
        Query query = new Query(CAPABILITIES, parameters("address", resource.address));
        return Multi.createFrom()
                .publisher(driver.rxSession().readTransaction(tx -> tx.run(query).records()))
                .collect().asList()
                .map(records -> {
                    resource.capabilities = records.stream()
                            .map(record -> Capability.from(record.get("c").asNode()))
                            .sorted(Comparator.comparing(c -> c.name))
                            .collect(toList());
                    return resource;
                });
    }

    private <T extends Parameter> void resolveRelations(Segment segment, Map<Long, T> models) {
        String type = segment.relationship().type();
        switch (type) {
            case "ALTERNATIVE":
            case "REQUIRES": {
                T start = models.get(segment.start().id());
                T end = models.get(segment.end().id());
                if (start != null && end != null) {
                    if ("ALTERNATIVE".equals(type)) {
                        start.addAlternative(end.name);
                        end.addAlternative(start.name);
                    } else {
                        start.addRequires(end.name);
                    }
                }
                break;
            }
            case "REFERENCES_CAPABILITY": {
                T start = models.get(segment.start().id());
                if (start != null) {
                    start.capability = segment.end().get(NAME).asString();
                }
                break;
            }
        }
    }
}
