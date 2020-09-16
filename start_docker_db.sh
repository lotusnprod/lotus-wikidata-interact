docker volume create onpdb_db
docker run --rm -p 127.0.0.1:10512:5432 --name some-postgres -v $PWD/data/03_for-db-import:/data -e POSTGRES_PASSWORD=password -v onpdb_db:/var/lib/postgresql/data -d postgres
docker exec -i some-postgres psql -U postgres <  schemas/schema_v01.sql