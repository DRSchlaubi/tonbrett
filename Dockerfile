FROM eclipse-temurin:27-jdk as builder
WORKDIR /usr/app
COPY . .
RUN ./gradlew --no-daemon installBotDist

FROM eclipse-temurin:27-jre-alpine

WORKDIR /usr/app
COPY --from=builder /usr/app/bot/build/install/bot-bot .

ENTRYPOINT ["/usr/app/bin/mikmusic"]
