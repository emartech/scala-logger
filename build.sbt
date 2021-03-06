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
    publish / skip := true,
    commands += analyse
  )
  .aggregate(
    core,
    catsEffect2,
    catsEffect3,
    akkaHttpContrib
  )

lazy val analyse = Command.command("analyse") { state =>
  Project.extract(state).appendWithSession(
    Seq(crossPaths := false, useCoursier := false),
    state
  )
}

lazy val core =
  project
    .in(file("core"))
    .settings(stdSettings("scala-logger", withScala3 = true))
    .settings(coreDependencies)
    .settings(
      // logging tests cannot run in parallel as slf4j sometimes creates a SubstituteLogger
      // instead of a proper logback Logger instance
      parallelExecution := false
    )

lazy val catsEffect2 =
  project
    .in(file("cats-effect-2"))
    .settings(stdSettings("scala-logger-ce2", withScala3 = true))
    .settings(catsEffect2Dependencies)
    .dependsOn(core % "compile->compile;test->test")

lazy val catsEffect3 =
  project
    .in(file("cats-effect-3"))
    .settings(stdSettings("scala-logger-ce3", withScala3 = true))
    .settings(catsEffect3Dependencies)
    .dependsOn(core % "compile->compile;test->test")

lazy val akkaHttpContrib =
  project
    .in(file("akka-http-contrib"))
    .settings(stdSettings("scala-logger-akka-http-contrib", withScala3 = false))
    .settings(akkaHttpContribDependencies)
    .dependsOn(core)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
