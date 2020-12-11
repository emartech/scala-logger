import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

val v2_12 = "2.12.12"
val v2_13 = "2.13.3"

lazy val commonSettings = Seq(
  crossScalaVersions := List(v2_13, v2_12),
  scalaVersion := v2_13,
  organization := "com.emarsys",
  scalafmtOnCompile := true,
  scalacOptions ++= commonScalacOptions,
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  scalacOptions ++= versionSpecificScalacOptions(scalaVersion.value),
)

// logging tests cannot run in parallel as slf4j sometimes creates a SubstituteLogger
// instead of a proper logback Logger instance
parallelExecution in ThisBuild := false

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .settings(
    organization := "com.emarsys",
    crossScalaVersions := Nil,
  )
  .aggregate(`scala-logger`, `akka-http-contrib`)

lazy val `scala-logger` = (project in file("core"))
  .settings(commonSettings: _*)
  .settings(
    name := "scala-logger",
    libraryDependencies ++= {
      Seq(
        "org.typelevel"        %% "cats-core"               % "2.2.0",
        "org.typelevel"        %% "cats-mtl"                % "1.0.0",
        "org.typelevel"        %% "cats-effect"             % "2.2.0",
        "ch.qos.logback"       % "logback-classic"          % "1.2.3",
        "net.logstash.logback" % "logstash-logback-encoder" % "6.4",
        "org.scalatest"        %% "scalatest"               % "3.2.2" % "test",
        "org.scalatestplus"    %% "scalacheck-1-14"         % "3.2.2.0" % "test",
        "org.scalacheck"       %% "scalacheck"              % "1.14.3" % "test",
        "com.github.mpilquist" %% "simulacrum"              % "0.19.0",
        "com.propensive"       %% "magnolia"                % "0.17.0",
        "org.scala-lang"       % "scala-reflect"            % scalaVersion.value
      )
    },
    libraryDependencies ++= versionSpecificLibraryDependencies(scalaVersion.value),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)
  )

lazy val `akka-http-contrib` = (project in file("akka-http-contrib"))
  .settings(commonSettings: _*)
  .settings(
    name := "scala-logger-akka-http-contrib",
    libraryDependencies ++= {
      Seq(
        "com.typesafe.akka" %% "akka-http" % "10.2.1"
      )
    },
    libraryDependencies ++= versionSpecificLibraryDependencies(scalaVersion.value),
    addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.2" cross CrossVersion.full)

  ).dependsOn(`scala-logger`)


inThisBuild(
  List(
    licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("https://github.com/emartech/scala-logger")),
    developers := List(
      Developer("doczir", "Robert Doczi", "doczi.r@gmail.com", url("https://github.com/doczir")),
      Developer("miklos-martin", "Miklos Martin", "miklos.martin@gmail.com", url("https://github.com/miklos-martin"))
    )
  )
)


addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-explaintypes",
  "-Yrangepos",
  "-feature",
  "-language:higherKinds",
  "-language:existentials",
  "-unchecked",
  "-Xlint:_,-type-parameter-shadow",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-extra-implicit",
  "-Ywarn-unused:imports",
  "-opt-warnings",
  "-target:jvm-1.8"
)

def versionSpecificScalacOptions(scalaV: String) =
  if (scalaV == v2_12)
    Seq(
      "-Xfuture",
      "-Xsource:2.13",
      "-Yno-adapted-args",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ypartial-unification"
    )
  else
    Seq(
      "-Ymacro-annotations"
    )

def versionSpecificLibraryDependencies(scalaV: String) =
  if (scalaV == v2_12)
    Seq(
      compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
    )
  else Seq()

lazy val noPublishSettings = Seq(
  publish := {},
  publishLocal := {},
  publishArtifact := false
)
