package org.wildfly.modelgraph.model;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServiceUnavailableException;
import java.time.Duration;

import static java.time.temporal.ChronoUnit.SECONDS;

final class Failure {

    static final Duration TIMEOUT = Duration.of(2, SECONDS);

    static void throwNotFound(String address) throws NotFoundException {
        throw notFound(address);
    }

    static NotFoundException notFound(String address) {
        return new NotFoundException("No resource found for address '" + address + "'");
    }

    static ServiceUnavailableException timeout() {
        return new ServiceUnavailableException("Request timed out");
    }

    private Failure() {
    }
}
