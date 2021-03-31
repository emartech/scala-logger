import BuildHelper._
import Dependencies._

ThisBuild / organization := "com.emarsys"
ThisBuild / licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / homepage := Some(url("https://github.com/emartech/scala-logger"))
ThisBuild / developers := List(
  Developer("doczir", "Robert Doczi", "doczi.r@gmail.com", url("https://github.com/doczir")),
  Developer("miklos-martin", "Miklos Martin", "miklos.martin@gmail.com", url("https://github.com/miklos-martin"))
)

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala-logger",
    skip in publish := true
  )
  .aggregate(
    core,
    akkaHttpContrib
  )

lazy val core =
  project
    .in(file("core"))
    .settings(stdSettings("scala-logger"))
    .settings(coreDependencies)
    .settings(
      // logging tests cannot run in parallel as slf4j sometimes creates a SubstituteLogger
      // instead of a proper logback Logger instance
      parallelExecution := false
    )

lazy val akkaHttpContrib =
  project
    .in(file("akka-http-contrib"))
    .settings(stdSettings("scala-logger-akka-http-contrib"))
    .settings(akkaHttpContribDependencies)
    .dependsOn(core)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
