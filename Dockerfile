FROM gradle:jdk20 as builder
WORKDIR /usr/app
COPY . .
RUN ./gradlew --no-daemon installBotArchive

FROM eclipse-temurin:20-jre-alpine

WORKDIR /usr/app
COPY --from=builder /usr/app/bot/build/installBot .

ENTRYPOINT ["/usr/app/bin/mikmusic"]
