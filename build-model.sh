#!/bin/bash

# Builds the model service

mvn package -Pnative -Dquarkus.native.container-build=true
docker build \
  --file src/main/docker/Dockerfile.native-distroless \
  --tag modelgraphtools/model \
  .
