package org.wildfly.modelgraph.model;

import io.smallrye.mutiny.Uni;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Query;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class IdentityRepository {

    private static final String IDENTITY = "" +
            "MATCH (i:Identity) " +
            "RETURN i";

    @Inject
    Driver driver;

    public Uni<Identity> identity() {
        Query query = new Query(IDENTITY);
        return Uni.createFrom()
                .publisher(driver.rxSession().readTransaction(tx -> tx.run(query).records()))
                .map(record -> Identity.from(record.get("i").asNode()));
    }
}
