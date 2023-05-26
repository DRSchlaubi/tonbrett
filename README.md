# tonbrett

Tonbrett (literal german translation of Soundboard) is a Discord Soundboard with a Web UI made
using [Mikbot](https://github.com/DRSchlaubi/mikbot) and 
[Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform)

![image](https://github.com/kordlib/kord/assets/16060205/08b70fd0-fbc5-4701-b2f9-e2098ebbd527)

# Commands

| Name          | Description                     |
|---------------|---------------------------------|
| /sound add    | Creates a new sound             |
| /sound remove | Deletes a sound                 |
| /sound update | Updates a sound                 |
| /sound play   | Shows the URL to the web player |
| /join         | Makes the bot join your channel |

# Requirements
- [Docker](https://docs.docker.com/engine/install/)
- [Docker Compose](https://docs.docker.com/compose/install/)
- [Lavalink](https://github.com/lavalink-devs/Lavalink#server-configuration)

# Setup
1. Download [docker-compose.yml](https://github.com/DRSchlaubi/tonbrett/blob/main/docker-compose.yaml)
2. Follow Lavalink setup steps from [here](https://github.com/DRSchlaubi/mikbot/tree/main/music#setup)
3. Create a reverse proxy for container port `8080` with websocket support
   [(Example)](https://www.nginx.com/blog/websocket-nginx/)

# Project Structure

| Path                       | Description                                           |
|----------------------------|-------------------------------------------------------|
| [common](common)           | Common entities and Routes between web player and bot |
| [client](client)           | Multiplatform client for API                          |
| [app](app)                 | Root projects for App                                 |
| [app:shared](app/shared)   | Multiplatform UI code for App                         |
| [app:web](app/web)         | Web launcher for app using Kotlin/JS                  |
| [app:android](app/android) | Android launcher for the app                          |
| [app:desktop](app/desktop) | Desktop launcher for the app using JLink              |
| [app:ios](app/ios)         | iOS/iPadOS launcher for the app using Kotlin/Native   |
