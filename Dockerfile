FROM quay.io/quarkus/ubi9-quarkus-micro-image:2.0
WORKDIR /work/
RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root --chmod=0755 target/*-runner /work/application
COPY --chown=1001:root --chmod=0755 docker-entrypoint.sh /work/entrypoint.sh

ENV TZ=Europe/Berlin

EXPOSE 8080
USER 1001

ENTRYPOINT ["/work/entrypoint.sh"]