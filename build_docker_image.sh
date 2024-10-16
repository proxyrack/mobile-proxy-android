#!/bin/bash

docker build --platform=linux/amd64 -t "proxy_control_builder" -f ./Dockerfile .