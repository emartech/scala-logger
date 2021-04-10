import sbt.Keys._
import sbt.librarymanagement.CrossVersion

object BuildHelper {
  val scala212          = "2.12.13"
  val scala213          = "2.13.5"
  val scala30           = "3.0.0-RC2"
  val targetJavaVersion = "1.8"

  private val stdOptions = Seq(
    "-deprecation",
    "-encoding",
    "UTF-8",
    "-feature",
    "-unchecked",
    "-Xfatal-warnings"
  )

  private val std2xOptions = Seq(
    "-language:higherKinds",
    "-language:existentials",
    "-explaintypes",
    "-Yrangepos",
    "-Xlint:_,-type-parameter-shadow",
    "-Ywarn-value-discard",
    "-Ywarn-numeric-widen",
    "-Ywarn-unused:imports",
    "-Ywarn-dead-code",
    "-Ywarn-extra-implicit",
    "-opt-warnings",
    s"-target:jvm-$targetJavaVersion"
  )

  private def versionSpecificOptions(scalaVersion: String) =
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, 13)) => Seq("-Ymacro-annotations") ++ std2xOptions
      case Some((2, 12)) =>
        Seq(
          "-Xfuture",
          "-Xsource:2.13",
          "-Yno-adapted-args",
          "-Ywarn-inaccessible",
          "-Ywarn-infer-any",
          "-Ywarn-nullary-override",
          "-Ywarn-nullary-unit",
          "-Ypartial-unification"
        ) ++ std2xOptions
      case Some((3, 0)) =>
        Seq("-Ykind-projector")
      case _ => Seq.empty
    }

  def stdSettings(prjName: String) = Seq(
    name := prjName,
    crossScalaVersions := Seq(scala212, scala213, scala30),
    scalaVersion := scala213,
    scalacOptions := stdOptions ++ versionSpecificOptions(scalaVersion.value),
    javacOptions ++= Seq("-source", targetJavaVersion, "-target", targetJavaVersion)
  )
}
