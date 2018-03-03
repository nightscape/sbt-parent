
build(scala212Only)

lazy val p1 = project.settings(
  addScala211,
  TaskKey[Unit]("check") := {
    assert(scalaVersion.value == "2.12.4")
    assert(crossScalaVersions.value == Seq("2.12.4", "2.11.12"))
    ()
  }
)

lazy val p2 = project.settings(
  TaskKey[Unit]("check") := {
    assert(scalaVersion.value == "2.12.4")
    assert(crossScalaVersions.value == Seq("2.12.4"))
    ()
  }
)

lazy val root = rootProject(p1, p2)
