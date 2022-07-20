#!/bin/bash

# Build every project using docker
# configure the ports as:
#   - flow:8080
#   - aggregator: 8181
#   - credit-bureau: 8282
#   - ui: 8383
#   - bank1: 8484 (we will have only one bank since we can't broadcast the message locally)
# the connectivity between the images works like this:
#  flow -> credit-bureau -> bank1 -> aggregator -> flow -> aggregator -> flow -> ui
# start the docker-compose with the built images

# every image must name ko.local/loanbroker-<servicename>

# build credit bureau
# no need of any additional env variable or setup
cd credit-bureau
kn func build --image dev.local/loanbroker-credit-bureau
docker run --rm -it -p 8181:8080 dev.local/loanbroker-credit-bureau

# build the banks
cd banks
kn func build --image dev.local/loanbroker-bank
docker run --rm -it -p 8484:8080 --env-file=.env dev.local/loanbroker-bank

# build the aggregator
cd aggregator
mvn clean install -DskipTests
docker run --rm -it -p 8181:8080 -e K_SINK=http://localhost:8080 dev.local/loanbroker-aggregator

# build the flow
cd loanbroker-flow
mvn clean install -DskipTests
docker run --rm -it -p 8080:8080 -e K_SINK=http://localhost:8383 dev.local/loanbroker-aggregator