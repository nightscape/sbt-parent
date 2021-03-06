# org.hammerlab.sbt:github

[![org.hammerlab.sbt:github](https://img.shields.io/badge/org.hammerlab.sbt:github-5.0.0-green.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.hammerlab.sbt%22%20a%3A%22github%22)

Helpers for setting projects' SCM-metadata keys to point to a GitHub repo:

```scala
addSbtPlugin("org.hammerlab.sbt" % "github" % "5.0.0")
```

Set GitHub user and repo-name:

```scala
github("my-org", "repo-name")
```

If an org-level plugin sets the `githubUser` key:

```scala
github.user := "my-org"
// apply to projects in ThisBuild
github.user * "my-org"
```

then downstream projects may just set the repository portion:

```scala
github.repo := "repo-name"
```

The [`base`](../base) plugin in this repo does this for [Hammer Lab](https://github.com/hammerlab/) projects.
