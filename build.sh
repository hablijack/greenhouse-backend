#!/usr/bin/bash

export GRAALVM_HOME=/opt/graalvm/
./mvnw package -Pnative -Dmaven.test.skip
docker build -f ./src/main/docker/Dockerfile.native -t hablijack/greenhouse-backend:latest .
