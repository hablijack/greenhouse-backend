FROM quay.io/quarkus/ubi-quarkus-graalvmce-builder-image:jdk-21 as builder
USER root
RUN mkdir -p /app
WORKDIR /app
COPY . .
RUN ./mvnw package -B -DskipTests -Dmaven.test.skip=true -Dquarkus.native.additional-build-args=-march=native -Pnative

FROM quay.io/quarkus/quarkus-micro-image:2.0
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --from=builder --chown=1001:root /app/target/*-runner /work/application
EXPOSE 8080
USER 1001
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
