package org.hammerlab.sbt.plugin

import org.hammerlab.sbt.Lib
import org.hammerlab.sbt.plugin.Deps.autoImport._
import org.hammerlab.sbt.plugin.JS.autoImport.scalajs.css
import org.hammerlab.sbt.plugin.Versions.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt._
import sbtcrossproject.{ CrossClasspathDependency, CrossProject }
import scalajsbundler.sbtplugin.ScalaJSBundlerPlugin.autoImport.npmDependencies
import scalajscrossproject.ScalaJSCrossPlugin

object JS
  extends Plugin(
    Deps,
    ScalaJSPlugin,
    ScalaJSCrossPlugin,
    Versions
  ) {

  override def trigger = noTrigger

  object autoImport {
    val scalacss = css.core

    object scalajs {

      object css extends Lib("com.github.japgolly.scalacss" ^^ "core" ^ "0.5.3") {
        val  core = dep
        val react = group ^^ "ext-react"
        override val global: SettingsDefinition =
          super.global ++
          Seq(
            versions +=
              react → version.value
          )
      }

      val dom = "org.scala-js" ^^ "scalajs-dom"

      object react extends Lib("com.github.japgolly.scalajs-react" ^^ "core" ^ "1.3.1") {
        val  core = dep
        val extra = group ^^ "extra"
        val jsVersion = SettingKey[String]("reactJSVersion", "Version of react JS libraries")
        val webjars =
          jsDependencies ++= Seq(
            "org.webjars.npm" % "react" % react.jsVersion.value
              /        "umd/react.development.js"
              minified "umd/react.production.min.js"
              commonJSName "React",

            "org.webjars.npm" % "react-dom" % react.jsVersion.value
              /         "umd/react-dom.development.js"
              minified  "umd/react-dom.production.min.js"
              dependsOn "umd/react.development.js"
              commonJSName "ReactDOM",

            "org.webjars.npm" % "react-dom" % react.jsVersion.value
              /         "umd/react-dom-server.browser.development.js"
              minified  "umd/react-dom-server.browser.production.min.js"
              dependsOn "umd/react-dom.development.js"
              commonJSName "ReactDOMServer"
          )

        override val global: SettingsDefinition =
          super.global ++
          Seq(
            jsVersion := "16.5.1",
            versions +=
              extra → version.value
          )

        override def settings: SettingsDefinition =
          Seq(
            Deps.autoImport.dep(
              css,
              react
            ),
            react.npm
          ) ++
          scalajs

        val npm =
          npmDependencies in Compile ++= Seq(
            "react"     → jsVersion.value,
            "react-dom" → jsVersion.value
          )
      }

      object diode extends Lib("io.suzaku" ^^ "diode" ^ "1.1.3") {
        val core = dep
        val react = group ^^ "diode-react"
        override val global: SettingsDefinition =
          super.global ++
          Seq(
            versions +=
                react → "1.1.3.120"
          )
      }

      val stubs = libraryDependencies += "org.scala-js" %% "scalajs-stubs" % scalaJSVersion % "provided"
    }

    implicit def makeScalaJSProject(s: scalajs.type): Seq[Setting[_]] =
      Seq(
        deps += scalajs.dom,
        scalaJSUseMainModuleInitializer := true
      )

    implicit class CrossProjectConfigOps(val p: CrossProject) extends AnyVal {
      def  andTest: CrossClasspathDependency = new CrossClasspathDependency(p, Some("compile->compile;test->test"))
      def testtest: CrossClasspathDependency = new CrossClasspathDependency(p, Some("test->test"))
      def     test: CrossClasspathDependency = new CrossClasspathDependency(p, Some("compile->test"))
    }
  }

  import autoImport._

  override def globalSettings =
    scalajs.react.global ++
    scalajs.diode.global ++
    scalajs.  css.global

  override def projectSettings =
    Seq(
      versions(
        scalajs.dom → "0.9.6"
      )
    ) ++
    scalajs.react.project ++
    scalajs.diode.project ++
    scalajs.  css.project
}
