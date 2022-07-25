#!/bin/bash

# Build every project using docker
# configure the ports as:
#   - flow:8080
#   - aggregator: 8181
#   - credit-bureau: 8282
#   - ui: 8383
#   - bank1: 8484 (we will have only one bank since we can't broadcast the message locally)
# the connectivity between the images works like this:
#  flow -> credit-bureau -> flow -> bank1 -> aggregator -> flow -> aggregator -> flow -> ui
# start the docker-compose with the built images

# every image must name ko.local/loanbroker-<servicename>

# build the flow
cd loanbroker-flow
mvn clean install -DskipTests
docker run --rm -it -p 8080:8080 -e K_SINK=http://localhost:8383 dev.local/loanbroker-aggregator

# build credit bureau
# no need of any additional env variable or setup
cd credit-bureau
kn func build --image dev.local/loanbroker-credit-bureau
docker run --rm -it -p 8181:8080 dev.local/loanbroker-credit-bureau

# build the aggregator
cd aggregator
mvn clean install -DskipTests
docker run --rm -it -p 8282:8080 -e K_SINK=http://localhost:8080 dev.local/loanbroker-aggregator

# build the UI
cd loanbroker-ui
mvn clean install -DskipTests
docker run --rm -it -p 8383:8080 dev.local/loanbroker-ui

# build the banks
cd banks
kn func build --image dev.local/loanbroker-bank
docker run --rm -it -p 8484:8080 --env-file=bank1.env dev.local/loanbroker-bank
docker run --rm -it -p 8585:8080 --env-file=bank2.env dev.local/loanbroker-bank
docker run --rm -it -p 8686:8080 --env-file=bank3.env dev.local/loanbroker-bank
