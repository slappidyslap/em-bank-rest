#!/usr/bin/env bash

set -e
mvn clean
mvn package -Dmaven.test.skip=true