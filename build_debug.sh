#!/bin/bash

docker run -v ./:/app --rm --platform=linux/amd64 "proxy_control_builder" assembleDebug