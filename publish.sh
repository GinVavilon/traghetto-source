#!/bin/bash

tasks=($(./gradlew tasks --all | grep -oP 'publish.*PublicationToMavenRepository'))

for task in "${tasks[@]}"; do
    echo "Run task: $task"
    ./gradlew "$task"
    echo wait
    sleep 300
done
echo Complete.
