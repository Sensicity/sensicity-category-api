# Category API

Set of microservices used for adding/removing or querying for categories/labels of any type of element.

## API

There are 4 implemented services:

- Add category(s) to identifier
- Remove category(s) from identifier
- Get category(s) from identifier
- Get all identifiers from a category

The API has two modes, an unprotected and a protected one.

The application listens connections at the port 9090.

### Running the project on an unprotected mode using docker-compose:

```
$ docker-compose up -d
```

## Run the project normally

Redis must be running on localhost, for instance, using the command `redis-server`.

If Redis is not running at localhost the following environment variable needs to be set:

  - `CATEGORY_API_REDIS_HOST`
  - `CATEGORY_API_REDIS_DATABASE_NAME`

Additionally, the following environment variable can be set if wanting an extra security layer.
The API will discard all petitions not having the header with name `auth_token`
set to the same value as the one defined in the environment variable. If not setting the value,
the header will no be needed.

  - `CATEGORY_API_AUTH_TOKEN`

Once Redis is loaded, on the root folder:

```
$ sbt run
```

*Note 2: Using Docker is recommended, the Dockerfile is set for the API,
the environment variables can be set pointing to the REDIS database.*
