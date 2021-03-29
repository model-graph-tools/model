#!/bin/bash

# Runs the model service
#
# Parameters
#   1. WildFly version >= 10


VERSION=$1


# Prerequisites
if [[ "$#" -ne 1 ]]; then
  echo "Illegal number of parameters. Please use $0 <wildfly-version>"
  exit 1
fi

java \
  -Dquarkus.http.port=80$VERSION \
  -Dquarkus.neo4j.uri=bolt://localhost:76$VERSION \
  -Dquarkus.neo4j.authentication.password=neo5j \
  -jar target/quarkus-app/quarkus-run.jar
