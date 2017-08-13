FROM java:8-jdk-alpine

# Prepares SBT environment
ENV SBT_VERSION 0.13.16
ENV SBT_HOME /usr/local/sbt
ENV PATH ${PATH}:${SBT_HOME}/bin

# Install sbt
RUN apk add --no-cache --update bash && \
    wget -q -O - "http://dl.bintray.com/sbt/native-packages/sbt/$SBT_VERSION/sbt-$SBT_VERSION.tgz" | gunzip | tar -x && \
    cp -a sbt-launcher-packaging-$SBT_VERSION/* /usr/local && rm -rf sbt-launcher-packaging-$SBT_VERSION && \
    echo -ne "- with sbt $SBT_VERSION\n" >> /root/.built

# Adds the project to the dockerfile
ADD . /api/

# Change the working directory to the application one
WORKDIR /api

# EXPOSES THE PORT 9090
EXPOSE 9090

# Pre-compiles the application code
RUN sbt compile

# Runs the application as soon as the container becames initialized
CMD sbt run
