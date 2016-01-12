# wykopml

[![Build Status](https://travis-ci.org/blstream/wykopml.svg?branch=master)](https://travis-ci.org/blstream/wykopml)

Playground to experiment with akka-streams, akka-http and Spark Mllib

## Getting Started

First create Cassandra `wykop` schema. 
```cql
CREATE KEYSPACE wykop WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'}  AND durable_writes = true;
```

### Fetching data

You can use system-wide `sbt` launcher (preferred, take a look at http://www.scala-sbt.org/download.html) or use builtin `./sbt` script.

When running updater it will auto create Cassandra tables.

First you need to scrap some data for further analysis:

`./sbt updater/run`

At first run it will only download index of elements. When run for the second time it would fetch comments and votes for items already in the index.


### Creating model

Right now only one model creation is available using:

`./sbt "prediction/runMain wykopml.TrainALSModelUsingVotes"`

Created model and username mapping will be saved to `prediction/.model_votes/`

### Using model

`./sbt "prediction/runMain wykopml.TrainALSModelUsingVotes .model_votes [username]"`

It will print 10 best matches.