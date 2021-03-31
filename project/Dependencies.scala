import sbt.Keys._
import sbt._

object Dependencies {

  val stdDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.3",
      compilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full)
    ) ++ versionSpecificStdDependencies(scalaVersion.value)
  )

  val stdTestDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.scalatest"     %% "scalatest"       % "3.2.6",
      "org.scalatestplus" %% "scalacheck-1-14" % "3.2.2.0",
      "org.scalacheck"    %% "scalacheck"      % "1.15.3"
    ).map(_ % Test)
  )

  private def versionSpecificStdDependencies(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full)
        )
      case _ => Seq()
    }

  val coreDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.typelevel"       %% "cats-core"                % "2.4.2",
      "org.typelevel"       %% "cats-mtl"                 % "1.1.2",
      "org.typelevel"       %% "cats-effect"              % "2.3.3",
      "org.typelevel"       %% "simulacrum"               % "1.0.1",
      "com.propensive"      %% "magnolia"                 % "0.17.0",
      "ch.qos.logback"       % "logback-classic"          % "1.2.3",
      "net.logstash.logback" % "logstash-logback-encoder" % "6.6",
      "org.scala-lang"       % "scala-reflect"            % scalaVersion.value
    )
  ) ++ stdDependencies ++ stdTestDependencies

  val akkaHttpContribDependencies = Seq(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.4"
    )
  ) ++ stdDependencies ++ stdTestDependencies

}
