FROM eclipse-temurin:24-jdk as builder
WORKDIR /usr/app
COPY . .
RUN ./gradlew --no-daemon installBotDist

FROM eclipse-temurin:24-jre-alpine

WORKDIR /usr/app
COPY --from=builder /usr/app/bot/build/install/bot-bot .

ENTRYPOINT ["/usr/app/bin/mikmusic"]
