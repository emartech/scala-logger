import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

val v2_12 = "2.12.12"
val v2_13 = "2.13.3"

lazy val commonSettings = Seq(
  crossScalaVersions := List(v2_13, v2_12),
  organization := "com.emarsys",
  scalafmtOnCompile := true
)

// logging tests cannot run in parallel as slf4j sometimes creates a SubstituteLogger
// instead of a proper logback Logger instance
parallelExecution in ThisBuild := false

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val `scala-logger` = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "scala-logger",
    scalacOptions ++= Seq(
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
    ),
    scalacOptions ++= versionSpecificScalacOptions(scalaVersion.value),
    javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
    libraryDependencies ++= {
      Seq(
        "org.typelevel"        %% "cats-core"               % "2.2.0",
        "org.typelevel"        %% "cats-mtl"                % "1.0.0",
        "org.typelevel"        %% "cats-effect"             % "2.2.0",
        "ch.qos.logback"       % "logback-classic"          % "1.2.3",
        "net.logstash.logback" % "logstash-logback-encoder" % "6.4",
        "org.scalatest"        %% "scalatest"               % "3.2.2" % "test",
        "org.scalatestplus"    %% "scalacheck-1-14"         % "3.2.2.0" % "test",
        "org.scalacheck"       %% "scalacheck"              % "1.15.1" % "test",
        "com.github.mpilquist" %% "simulacrum"              % "0.19.0",
        "com.propensive"       %% "magnolia"                % "0.17.0",
        "org.scala-lang"       % "scala-reflect"            % scalaVersion.value
      )
    },
    libraryDependencies ++= versionSpecificLibraryDependencies(scalaVersion.value)
  )

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

addCompilerPlugin("org.typelevel" % "kind-projector" % "0.11.0" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")

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
