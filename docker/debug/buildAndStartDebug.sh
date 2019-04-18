#!/bin/bash
fileName="anime-checker-1.0.jar"
filePath="../../target/$fileName"
if [[ ! -f "$filePath" ]]
then
    cd ../../
    mvn clean install
    cd docker/debug
fi
cp "$filePath" "$fileName"
docker build -t nasirov/anime-checker:debug .
rm "$fileName"
docker-compose up