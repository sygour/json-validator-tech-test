# Simple JSON validation service

This is the result of a technical test

# API description

```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```

# Building and running the application

To launch the tests

```
sbt test
```

To run the application (it will use 9000 as default port)

```
sbt run
```

# Testing the application with cUrl

Files located in test/data/ can be used to test

## Upload a schema
```
curl --header "Content-Type: application/json" \
  --request POST \
  -d @data/config-schema.json \
  http://localhost:9000/schema/config-schema 
```

## Download a schema
```
curl --request GET http://localhost:9000/schema/config-schema
```

## Validate a Json document against a schema
```
curl --header "Content-Type: application/json" \
  --request POST \
  -d @test/data/config-1.json \
  http://localhost:9000/validate/config-schema
```

