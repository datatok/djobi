# Java application to run jobs

A Java program to run data ETL workload using Apache Spark framework. (ETL: load / transform / save).

## Usage

### CLI

The ``djobi-cli`` project build a java CLI to run djobi as a Spark application:

``./djobi --args date=yesterday ./pipelines/test``

### Server

@todo !

## Contributing

Djobi follows git flow pattern: https://danielkummer.github.io/git-flow-cheatsheet/ &
https://github.com/commitizen/cz-cli .

### Requirement

* Java 11
* Gradle 6
* IDE: Intellij IDEA CE
* Docker & docker-compose

### Setup



### Testing



### Versioning

All stable releases will follow the [semantic versioning](http://semver.org/) guidelines.

Releases will be numbered with the following format:

`<major>.<minor>.<patch>`

Based on the following guidelines:

* A new *major* release indicates a large change where backwards compatibility is broken.
* A new *minor* release indicates a normal change that maintains backwards compatibility.
* A new *patch* release indicates a bugfix or small change which does not affect compatibility.

## Gradle

### compile

`` gradle assemble -x :djobi-core:signArchives -x :djobi-tests:signArchives``

### create release

``gradle -Prelease.version=$(cat VERSION) clean djobi_assemble -x test``
