#!/bin/bash

# Builds the model image
#
# Parameters
#   1. version (optional)
#
# See https://stackoverflow.com/a/40771884
# for the ${TAG}${VERSION:+:$VERSION} syntax


VERSION=$1
TAG=quay.io/modelgraphtools/model


mvn package -Pnative -Dquarkus.native.container-build=true
docker build \
  --file src/main/docker/Dockerfile.native-distroless \
  --tag ${TAG}${VERSION:+:$VERSION} \
  .
