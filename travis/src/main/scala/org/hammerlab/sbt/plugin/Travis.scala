package org.hammerlab.sbt.plugin

import java.lang.System.getenv

import org.hammerlab.sbt.plugin.Root.autoImport.isRoot
import org.hammerlab.sbt.plugin.Test.autoImport.test_?
import org.hammerlab.sbt.plugin.Travis.autoImport.travis_?
import org.hammerlab.sbt.plugin.Versions.noopSettings
import org.scoverage.coveralls.CoverallsPlugin.coveralls
import sbt.Keys._
import sbt._
import scoverage.ScoverageKeys._

object Travis
  extends Plugin(
    Root,
    Scala,
    Test,
    Versions
  ) {

  noopSettings ++=
    Seq(
      test_? :=
        Def.settingDyn[Boolean] {
          val default = test_?.value
          // Always run tests for non-root modules in Travis (otherwise coverage gets messed up, for example)
          if (travis_?.value && !isRoot.value)
            Def.setting(true)
          else
            Def.setting(default)
        }
        .value,
      coverageReport :=
        Def.taskDyn[Unit] {
          val default = coverageReport.taskValue
          if (travis_?.value && !isRoot.value) {
            streams.value.log.info(s"${name.value}: running coverageReport")
            Def.task(default.value)
          } else
            Def.task {
              streams.value.log.info(s"${name.value}: skipping coverageReport (travis: ${travis_?.value}, root: ${isRoot.value})")

              ()
            }
        }
        .value
    )

  val travisScalaEnv = "TRAVIS_SCALA_VERSION"
  def travisScalaVersion = getenv(travisScalaEnv)

  object autoImport {
    val travisCoverageScalaVersion = settingKey[Option[String]]("Scala version to measure/report test-coverage for")
    val coverageTest = taskKey[Unit](s"Wrapper for test and, if $travisScalaEnv matches travisCoverageScalaVersion, coverageReport")
    val travis_? = settingKey[Boolean]("True when running in Travis CI")
  }

  import autoImport._

  def disableCoverallsEnv = System.getProperty("coveralls.disable") != null

  /**
   * Command for building and submitting a coverage report in Travis, *only* for the build corresponding to a specific
   * Scala version (indicated by travisCoverageScalaVersion).
   */
  val travisReport = taskKey[Unit](s"Wrapper for coverageAggregate and coveralls, iff $travisScalaEnv matches `travisCoverageScalaVersion`")

  def travisReportTask = Def.taskDyn {
    val log = streams.value.log
    val extracted = Project.extract(state.value)
    implicit val pr = extracted.currentRef
    implicit val bs = extracted.structure

    val tcsv = travisCoverageScalaVersion.value

    if (disableCoverallsEnv) {
      log.info(s"Coveralls reporting disabled by -Dcoveralls.disable")
      Def.task {}
    } else if (coverageEnabled.value) {
      log.info(s"Running coveralls")
      coveralls
    } else {
      log.info(
        s"Skipping coverage reporting for scala version '${Option(travisScalaVersion).getOrElse("")}' (env var: $travisScalaEnv); reporting enabled for $tcsv (sbt key: travisCoverageScalaVersion)"
      )
      Def.task {}
    }
  }

  globals(travis_? := (getenv("TRAVIS") != null))

  projects(
    travisCoverageScalaVersion :=
      crossScalaVersions
        .value
        .sortWith {
          (ls, rs) ⇒
            def ints(s: String) =
              s
                .split("\\.")
                .map(_.toInt)

            val (l, r) = (ints(ls), ints(rs))

            l
              .zip(r)
              .find { case (l, r) ⇒ l != r }
              .map {
                case (l, r) ⇒ l < r
              }
              .getOrElse(
                l.length < r.length
              )
        }
        .lastOption,

    coverageTest := Def.sequential(
      test in sbt.Test,
      Def.taskDyn[Unit] {
        if (coverageEnabled.value) {
          streams.value.log.info(s"${name.value}: generating coverage reports")
          coverageReport
        } else {
          streams.value.log.debug(s"${name.value}: skipping coverageReport generation")
          Def.task {}
        }
      },
      Def.taskDyn[Unit] {
        if (coverageEnabled.value && isRoot.value) {
          streams.value.log.info(s"${name.value}: aggregating coverage reports")
          coverageAggregate
        } else {
          streams.value.log.debug(s"${name.value}: skipping coverageReport aggregation")
          Def.task {}
        }
      }
    ).value,

    travisReport := travisReportTask.value,

    coverageEnabled := (
      if (travisCoverageScalaVersion.value.contains(travisScalaVersion) && !disableCoverallsEnv)
        true
      else
        coverageEnabled.value
    )
  )
}
