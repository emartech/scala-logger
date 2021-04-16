import sbt.Keys._
import sbt._
import sbt.librarymanagement.CrossVersion

object Dependencies {

  val stdDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.3"
    ) ++ versionSpecificStdDependencies(scalaVersion.value)
  )

  val stdTestDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.scalameta"     %% "munit"            % "0.7.23",
      "org.scalameta"     %% "munit-scalacheck" % "0.7.23"
    ).map(_ % Test)
  )

  private def versionSpecificStdDependencies(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 12)) =>
        Seq(
          compilerPlugin("org.scalamacros" % "paradise"       % "2.1.1" cross CrossVersion.full),
          compilerPlugin("org.typelevel"   % "kind-projector" % "0.11.3" cross CrossVersion.full)
        )
      case Some((2, 13)) =>
        Seq(compilerPlugin("org.typelevel" % "kind-projector" % "0.11.3" cross CrossVersion.full))
      case _ => Seq()
    }

  val coreDependencies = Seq(
    libraryDependencies ++= Seq(
      "org.typelevel"       %% "cats-core"                % "2.5.0",
      "org.typelevel"       %% "cats-mtl"                 % "1.1.3",
      "ch.qos.logback"       % "logback-classic"          % "1.2.3",
      "net.logstash.logback" % "logstash-logback-encoder" % "6.6"
    ) ++ magnolia(scalaVersion.value)
  ) ++ stdDependencies ++ stdTestDependencies

  private def magnolia(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((3, _)) => Nil
      case _ =>
        Seq(
          "com.propensive" %% "magnolia"      % "0.17.0",
          "org.scala-lang"  % "scala-reflect" % scalaVersion % Provided
        )
    }

  val akkaHttpContribDependencies = Seq(
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.2.4"
    )
  ) ++ stdDependencies ++ stdTestDependencies

  val catsEffect2Dependencies = Seq(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"         % "2.4.1",
      "org.typelevel" %% "munit-cats-effect-2" % "1.0.1" % "test"
    )
  ) ++ stdDependencies ++ stdTestDependencies

  val catsEffect3Dependencies = Seq(
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats-effect"         % "3.0.2",
      "org.typelevel" %% "munit-cats-effect-3" % "1.0.1" % "test"
    )
  ) ++ stdDependencies ++ stdTestDependencies

}
