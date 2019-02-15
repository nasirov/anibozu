#!/bin/bash
file="../../target/anime-checker-1.0.jar"
if [[ ! -f "$file" ]]
then
    cd ../../
    mvn clean install
    cd docker/debug
fi
cp ../../target/anime-checker-1.0.jar anime-checker-1.0.jar
docker build -t nasirov/anime-checker:debug .
rm anime-checker-1.0.jar
docker-compose up