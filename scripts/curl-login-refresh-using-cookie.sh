#!/usr/bin/env bash

set -e

curl -i \
  -v \
  -c cookies.txt \
  --header 'Content-Type: application/json' \
  --location 'http://localhost:8080/api/v1/auth/login' \
  --data-raw '
    {
      "email": "eld@test.com",
      "password": "123"
    }'

curl -i \
  -X POST \
  -b cookies.txt \
  --header 'Content-Type: application/json' \
  --location 'http://localhost:8080/api/v1/auth/refresh'