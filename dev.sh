#!/bin/bash

# Starts the model service in dev mode
#
# Parameters
#   1. WildFly version >= 10


VERSION=$1


# Prerequisites
if [[ "$#" -ne 1 ]]; then
  echo "Illegal number of parameters. Please use $0 <wildfly-version>"
  exit 1
fi
if ! [[ $VERSION =~ ^[0-9]+$ ]] ; then
  echo "Illegal version. Must be numeric and >= 10."
  exit 1
fi
if [[ "$VERSION" -lt "10" ]]; then
  echo "Illegal version. Must be numeric and >= 10."
  exit 1
fi


./mvnw \
  -Ddebug=50$VERSION \
  -Dquarkus.http.port=80$VERSION \
  -Dquarkus.neo4j.uri=bolt://localhost:76$VERSION \
  -Dquarkus.log.category.\"org.wildfly.modelgraph\".level=DEBUG \
  -Dmgt.api.service.uri=http://localhost:9911 \
  -Dmgt.model.service.uri=http://localhost:80$VERSION \
  -Dmgt.neo4j.browser.uri=http://localhost:74$VERSION/browser \
  quarkus:dev
