#!/bin/bash

docker build --platform=linux/amd64 -t "proxy_control_builder" .
docker run -v ./:/app --rm --platform=linux/amd64 "proxy_control_builder"
echo "DONE"
