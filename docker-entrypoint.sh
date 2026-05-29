#!/bin/sh
exec ./application \
  -Dquarkus.http.host=0.0.0.0 \
  -Dquarkus.datasource.jdbc.url="jdbc:postgresql://${DB_HOST}:5432/greenhouse" \
  -Dquarkus.datasource.username="${DB_USER}" \
  -Dquarkus.datasource.password="${DB_PASSWORD}" \
  -Dtelegram.bot.token="${TELEGRAM_TOKEN}" \
  -Dtelegram.bot.chatid="${TELEGRAM_CHATID}" \
  -Dai.llm.base-url="${AI_LLM_BASE_URL}" \
  -Dai.llm.timeout="${AI_LLM_TIMEOUT:-60000}" \
  -Dai.llm.max-retries="${AI_LLM_MAX_RETRIES:-3}"
