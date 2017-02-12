# sensicity-category-api [![Build Status](https://travis-ci.org/Sensicity/sensicity-category-api.svg?branch=master)](https://travis-ci.org/Sensicity/sensicity-category-api)

Set of microservices used for adding/removing or querying for categories/labels of any type of element.

## Run the project using Docker

```
$ docker-compose up
```

## App testing

For testing the App, on the root folder:

```
$ sbt test
```

*Note: Redis must be enabled and accepting connections from localhost (`redis-server`)** 