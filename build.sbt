import org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings

lazy val commonSettings = Seq(
  scalaVersion := "2.12.9",
  organization := "com.emarsys",
  scalafmtOnCompile := true
)

// logging tests cannot run in parallel as slf4j sometimes creates a SubstituteLogger
// instead of a proper logback Logger instance
parallelExecution in ThisBuild := false

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
      "-Xfuture",
      "-Ypartial-unification",
      "-language:higherKinds",
      "-language:existentials",
      "-unchecked",
      "-Yno-adapted-args",
      "-Xlint:_,-type-parameter-shadow",
      "-Xsource:2.13",
      "-Ywarn-dead-code",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-value-discard",
      "-Ywarn-extra-implicit",
      "-Ywarn-unused:imports",
      "-opt-warnings"
    ),
    libraryDependencies ++= {
      Seq(
        "org.typelevel"        %% "cats-core"               % "2.0.0",
        "org.typelevel"        %% "cats-mtl-core"           % "0.7.0",
        "org.typelevel"        %% "cats-effect"             % "2.0.0",
        "ch.qos.logback"       % "logback-classic"          % "1.2.3",
        "net.logstash.logback" % "logstash-logback-encoder" % "6.2",
        "org.scalatest"        %% "scalatest"               % "3.0.8" % "test",
        "org.scalacheck"       %% "scalacheck"              % "1.14.0" % "test",
        "com.github.mpilquist" %% "simulacrum"              % "0.19.0",
        "com.propensive"       %% "magnolia"                % "0.11.0"
      )
    }
  )

inThisBuild(
  List(
    licenses := Seq("MIT" -> url("http://opensource.org/licenses/MIT")),
    homepage := Some(url("https://github.com/emartech/scala-logger")),
    developers := List(
      Developer("doczir", "Robert Doczi", "doczi.r@gmail.com", url("https://github.com/doczir")),
      Developer("miklos-martin", "Miklos Martin", "miklos.martin@gmail.com", url("https://github.com/miklos-martin"))
    ),
    scmInfo := Some(
      ScmInfo(url("https://github.com/emartech/scala-logger"), "scm:git:git@github.com:emartech/scala-logger.git")
    ),
    // These are the sbt-release-early settings to configure
    pgpPublicRing := file("./ci/local.pubring.asc"),
    pgpSecretRing := file("./ci/local.secring.asc"),
    releaseEarlyWith := SonatypePublisher
  )
)

addCompilerPlugin("org.spire-math"  %% "kind-projector" % "0.9.6")
addCompilerPlugin("org.scalamacros" % "paradise"        % "2.1.0" cross CrossVersion.full)

addCommandAlias("fmt", "all scalafmtSbt scalafmt test:scalafmt")
