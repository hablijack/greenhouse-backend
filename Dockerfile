FROM registry.access.redhat.com/ubi9/ubi-minimal:9.5

# Install Eclipse Temurin Java 25
RUN microdnf install -y curl tar \
    && curl -L "https://github.com/adoptium/temurin25-binaries/releases/download/jdk-25.0.2%2B7/OpenJDK25U-jdk_aarch64_linux_25.0.2_7.tar.gz" -o temurin25.tar.gz \
    && mkdir -p /opt/java \
    && tar -xzf temurin25.tar.gz -C /opt/java --strip-components=1 \
    && rm temurin25.tar.gz \
    && microdnf clean all

ENV JAVA_HOME=/opt/java
ENV PATH=$JAVA_HOME/bin:$PATH
WORKDIR /work/

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work
COPY --chown=1001:root target/*-runner /work/application

ENV TZ=Europe/Berlin

EXPOSE 8080
USER 1001

CMD ["./application", \
     "-Dquarkus.http.host=0.0.0.0", \
     "-Dquarkus.datasource.jdbc.url=jdbc:postgresql://${DB_HOST}:5432/greenhouse", \
     "-Dquarkus.datasource.username=${DB_USER}", \
     "-Dquarkus.datasource.password=${DB_PASSWORD}", \
     "-Dtelegram.bot.token=${TELEGRAM_TOKEN}", \
     "-Dtelegram.bot.chatid=${TELEGRAM_CHATID}" \
     ]
