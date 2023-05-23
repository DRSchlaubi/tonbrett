#!/usr/bin/env bash
brew install cocoapods
brew install openjdk@20

# cd into actual project root
cd ../../../
./gradlew app:ios:podinstall
