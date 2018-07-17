import com.typesafe.sbt.web.PathMapping
import com.typesafe.sbt.web.pipeline.Pipeline

ThisBuild / organization := "xw"
name := "frontend"
ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.6"

def compilerFlags: Seq[Setting[_]] = {
  val common = Seq(
    "-deprecation",
    "-encoding", "utf-8",
    "-explaintypes",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xcheckinit",
    "-Xfuture",
    "-Xlint:adapted-args",
    "-Xlint:by-name-right-associative",
    "-Xlint:constant",
    "-Xlint:delayedinit-select",
    "-Xlint:doc-detached",
    "-Xlint:inaccessible",
    "-Xlint:infer-any",
    "-Xlint:missing-interpolator",
    "-Xlint:nullary-override",
    "-Xlint:nullary-unit",
    "-Xlint:option-implicit",
    "-Xlint:package-object-classes",
    "-Xlint:poly-implicit-overload",
    "-Xlint:private-shadow",
    "-Xlint:stars-align",
    "-Xlint:type-parameter-shadow",
    "-Xlint:unsound-match",
    "-Yno-adapted-args",
    "-Yno-imports",
    "-Yno-predef",
    "-Ypartial-unification",
  )

  val test = Seq(
    "-Yrangepos",
  )

  // Hide all this away where IntelliJ can't find it because it breaks
  // IntelliJ's Scala worksheets.
  val compileOnly = Seq(
    "-Xfatal-warnings",
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
    "-Ywarn-value-discard",
  )

  Seq(
    scalacOptions := common,
    Test / scalacOptions ++= test,
    Compile / compile / scalacOptions ++= compileOnly,
    Test / compile / scalacOptions ++= compileOnly,
  )
}

// TODO: It's time to reify the dependencies
ThisBuild / dependencyOverrides ++= Seq(
  // circe-core 0.9.3 depends on cats-core 1.0.1
  "org.typelevel" %% "cats-core" % "1.1.0",
  "org.typelevel" %% "cats-kernel" % "1.1.0",
)

disablePlugins(RevolverPlugin)

ThisBuild / scalafmtOnCompile := true

val digestIndex = "digest-index.json"
val webPackageDirectory = "web"

lazy val common = crossProject.
  disablePlugins(RevolverPlugin).
  enablePlugins(BuildInfoPlugin).
  settings(compilerFlags).
  settings(
    name := "frontend-common",

    buildInfoPackage := "xw.frontend",
    buildInfoKeys := Seq(
      "digestIndex" → digestIndex,
      "webPackageDirectory" → webPackageDirectory,
    ),
  )
lazy val commonJS = common.js
lazy val commonJVM = common.jvm

// This mostly exists to isolate the disabling of compilation flags.
lazy val `static-resources` = project.
  disablePlugins(RevolverPlugin).
  enablePlugins(SbtTwirl).
  dependsOn(commonJVM).
  settings(compilerFlags).
  settings(
    name := "frontend-resources",
    TwirlKeys.templateImports += "xw.frontend._",
    Compile / compile / scalacOptions --= Seq(
      "-Ywarn-unused:imports",
    ),
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-parser" % "0.9.3",
      "com.vmunier" %% "scalajs-scripts" % "1.1.2",
      "org.specs2" %% "specs2-core" % "4.2.0" % Test,
      "org.typelevel" %% "cats-core" % "1.1.0",
    ),
  )

lazy val client = project.
  disablePlugins(RevolverPlugin).
  enablePlugins(ScalaJSPlugin, ScalaJSWeb).
  dependsOn(commonJS).
  settings(compilerFlags).
  settings(
    name := "frontend-client",
    scalaJSUseMainModuleInitializer := true,
    emitSourceMaps in fullOptJS := false,
  )

// For some reason, the paths of the digest files have an extra slash
// prepended to them. This isn't canonicalised by either the ZIP reading or
// getResource, and it's much nicer to fix them up here than it is to write
// tests to work around it.
val fixDigestPaths = taskKey[Pipeline.Stage]("Fix sbt-digest paths")
ThisBuild / fixDigestPaths := { mappings: Seq[PathMapping] ⇒
  mappings.map { case mapping @ (source, destination) ⇒
    if (destination.startsWith("/")) (source, destination.tail)
    else mapping
  }
}

lazy val server = project.
  enablePlugins(SbtWeb).
  dependsOn(`static-resources`).
  settings(compilerFlags).
  settings(
    name := "frontend-server",

    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.github.pureconfig" %% "pureconfig" % "0.9.1",
      "com.typesafe.akka" %% "akka-http" % "10.1.3",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.3" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.13",
      "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
      "eu.timepit" %% "refined" % "0.9.0",
      "eu.timepit" %% "refined-pureconfig" % "0.9.0",
      "io.circe" %% "circe-testing" % "0.9.3",
      "org.log4s" %% "log4s" % "1.6.1",
      "org.specs2" %% "specs2-core" % "4.2.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.2.0" % Test,
      "rocks.heikoseeberger" %% "accessus" % "2.0.0",
    ),

    assembly / assemblyJarName := "frontend.jar",
    assembly / test := (()),

    scalaJSProjects := Seq(client),
    WebKeys.packagePrefix in Assets := webPackageDirectory + "/",
    DigestKeys.indexPath := Some(digestIndex),
    pipelineStages in Assets := Seq(scalaJSPipeline, digest, gzip, fixDigestPaths),
    managedClasspath in Runtime += (packageBin in Assets).value,
    managedClasspath in Test += (packageBin in Assets).value,
  )

