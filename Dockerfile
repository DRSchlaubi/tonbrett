FROM openjdk:22-rc-jdk as builder
WORKDIR /usr/app
COPY . .
RUN ./gradlew --no-daemon installBotArchive

FROM openjdk:22-rc-jdk

WORKDIR /usr/app
COPY --from=builder /usr/app/bot/build/installBot .

ENTRYPOINT ["/usr/app/bin/mikmusic"]
