# sensicity-category-api [![Build Status](https://travis-ci.org/Sensicity/sensicity-category-api.svg?branch=master)](https://travis-ci.org/Sensicity/sensicity-category-api)

Set of microservices used for adding/removing or querying for categories/labels of any type of element.

## API

There are 4 implemented services:

- Add category(s) to identifier
- Remove category(s) from identifier
- Get category(s) from identifier
- Get all identifiers from a category

## Run the project using Docker

```
$ docker-compose up
```

The application listens connections at the port 9090.

## App testing

Redis must be running on localhost, for instance, using the command `redis-server`.

If Redis is not running at localhost the following environment variable needs to be set:

  - `SENSICITY_CATEGORY_REDIS_HOST`

Once Redis is loaded, on the root folder:

```
$ sbt test
```

*Note: SBT must be installed*
