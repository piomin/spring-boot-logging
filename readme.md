## Logging with Spring Boot and Elastic Stack  [![Twitter](https://img.shields.io/twitter/follow/piotr_minkowski.svg?style=social&logo=twitter&label=Follow%20Me)](https://twitter.com/piotr_minkowski)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.piomin/logstash-logging-spring-boot-starter/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.piomin/logstash-logging-spring-boot-starter)
[![CircleCI](https://circleci.com/gh/piomin/spring-boot-logging.svg?style=svg)](https://circleci.com/gh/piomin/spring-boot-logging)

[![SonarCloud](https://sonarcloud.io/images/project_badges/sonarcloud-black.svg)](https://sonarcloud.io/dashboard?id=piomin_spring-boot-logging)
[![Bugs](https://sonarcloud.io/api/project_badges/measure?project=piomin_spring-boot-logging&metric=bugs)](https://sonarcloud.io/dashboard?id=piomin_spring-boot-logging)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=piomin_spring-boot-logging&metric=coverage)](https://sonarcloud.io/dashboard?id=piomin_spring-boot-logging)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=piomin_spring-boot-logging&metric=ncloc)](https://sonarcloud.io/dashboard?id=piomin_spring-boot-logging)

## Main purpose

This library is created for logging incoming HTTP requests and outgoing HTTP responses and send these logs automatically to Logstash.

## Articles
 
Detailed description can be found here:
1. [Logging with Spring Boot and Elastic Stack](https://piotrminkowski.com/2019/05/07/logging-with-spring-boot-and-elastic-stack/)
2. [Using logstash-logging-spring-boot-starter for logging with Spring Boot and Logstash](https://piotrminkowski.com/2019/10/02/using-logstash-logging-spring-boot-starter-for-logging-with-spring-boot-and-logstash/)

## Features
In short, let’s begin from a brief review of main features provided by logstash-logging-spring-boot-starter:
          
1. It is able to log all incoming HTTP requests and outgoing HTTP responses with full body, and send those logs to Logstash with the proper tags
2. It is able to calculate and store an execution time for each request
3. It generates and propagates correlationId for downstream services calling with Spring RestTemplate or OpenFeign
4. It is auto-configurable Spring Boot library – you don’t have to do anything more than including it as a dependency to your application to make it work

## Getting started
The current version of library is `1.4.1`.\
For logging with Spring WebMvc:
```
<dependency>
  <groupId>com.github.piomin</groupId>
  <artifactId>logstash-logging-spring-boot-starter</artifactId>
  <version>1.4.1</version>
</dependency>
```

For logging with Spring WebFlux:
```
<dependency>
  <groupId>com.github.piomin</groupId>
  <artifactId>reactive-logstash-logging-spring-boot-starter</artifactId>
  <version>1.4.1</version>
</dependency>
```

By default, the library is enabled, but tries to locate Logback configuration inside your application to settings for Logstash appender. If such appender won’t be found, the library uses Spring Boot default logging configuration, which does not include Logstash appender. To force it use auto-configured appender definition inside library we have to set property logging.logstash.enabled to `true`.
```
logging.logstash:
  enabled: true
  url: 192.168.99.100:5000
```

## Manual add jar to pom.xml

Add `reactive-logstash-logging-spring-boot-starter-1.4.1.pom` to `${basedir}/dependencies`

Add `pom.xml` to `${basedir}/dependencies` and rename to `reactive-logstash-logging-spring-boot-starter-1.4.0.RELEASE.pom`

Add this script to `pom.xml` in plugins section.

```
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-install-plugin</artifactId>
  <version>2.5.1</version>
  <configuration>
      <groupId>com.github.piomin</groupId>
      <artifactId>reactive-logstash-logging-spring-boot-starter</artifactId>
      <version>1.4.1</version>
      <packaging>jar</packaging>
      <file>${basedir}/dependencies/reactive-logstash-logging-spring-boot-starter-1.3.1.RELEASE.jar</file>
      <generatePom>false</generatePom>
      <pomFile>${basedir}/dependencies/reactive-logstash-logging-spring-boot-starter-1.3.1.RELEASE.pom</pomFile>
  </configuration>
  <executions>
      <execution>
      <id>install-jar-lib</id>
      <goals>
        <goal>install-file</goal>
      </goals>
      <phase>validate</phase>
      </execution>
  </executions>
  </plugin>
```