docker run \
  --name postgres \
  -e POSTGRES_PASSWORD=1 \
  -e POSTGRES_DB=em_bank_rest \
  -p 5432:5432 \
  postgres:18-alpine