# org.hammerlab.sbt:maven

[![Maven Central](https://img.shields.io/badge/maven%20central-4.0.0-green.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.hammerlab.sbt%22%20a%3A%maven%22)

```scala
addSbtPlugin("org.hammerlab.sbt" % "maven" % "4.0.0")
```

[Settings for publishing to Maven repositories (especially Maven Central)](src/main/scala/org/hammerlab/sbt/plugin/Maven.scala):

- necessary POM settings
- add Sonatype "releases" and "snapshots" resolvers

`sbt +publish` will publish a snapshot to Sonatype for each Scala cross-version, and `sbt +publishSigned sonatypeRelease` will publish to Sonatype-staging and then release to Maven Central.

Additionally, `sbt mavenLocal` wraps `publishM2`, but only operates on `-SNAPSHOT`-versioned projects.