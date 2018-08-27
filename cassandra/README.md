# Files

To apply schema, just use these commands

* docker cp docker/cassandra/schema.cql cassandra:/
* docker exec cassandra cqlsh -f /schema.cql
