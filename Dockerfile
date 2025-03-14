FROM registry.access.redhat.com/ubi9/ubi-minimal:9.5
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
