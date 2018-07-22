lazy val commonSettings = Seq(
  scalaVersion := "2.12.6",
  organization := "com.emarsys"
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)
  .settings(
    name := "scala-logger",
    version := "0.1.0",
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding",
      "UTF-8",
      "-explaintypes",
      "-unchecked",
      "-feature",
      "-language:implicitConversions",
      "-language:postfixOps",
      "-language:higherKinds",
      "-Ywarn-dead-code",
      "-Ywarn-extra-implicit",
      "-Ywarn-inaccessible",
      "-Ywarn-infer-any",
      "-Ywarn-nullary-override",
      "-Ywarn-nullary-unit",
      "-Ywarn-numeric-widen",
      "-Ywarn-unused:implicits",
      "-Ywarn-unused:imports",
      "-Ywarn-unused:locals",
      "-Ywarn-unused:params",
      "-Ywarn-unused:patvars",
      "-Ywarn-unused:privates",
      "-Yno-adapted-args",
      "-Ypartial-unification",
      "-Xlint"
    ),
    libraryDependencies ++= {
      val catsV = "1.1.0"
      Seq(
        "org.typelevel"        %% "cats-core"               % catsV,
        "org.typelevel"        %% "cats-mtl-core"           % "0.3.0",
        "ch.qos.logback"       % "logback-classic"          % "1.2.3",
        "net.logstash.logback" % "logstash-logback-encoder" % "5.1",
        "com.chuusai"          %% "shapeless"               % "2.3.3",
        "org.scalatest"        %% "scalatest"               % "3.0.5" % "test"
      )
    }
  )

credentials += Credentials(
  "Sonatype Nexus Repository Manager",
  "nexus.service.emarsys.net",
  sys.env("NEXUS_USERNAME"),
  sys.env("NEXUS_PASSWORD")
)

publishTo := Some("releases" at "https://nexus.service.emarsys.net/repository/emartech/")
addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.6")
