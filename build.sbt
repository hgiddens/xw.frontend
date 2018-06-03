ThisBuild / organization := "xw"
name := "frontend"
ThisBuild / version := "0.0.1-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.6"

def compilerFlags: Seq[Setting[_]] = {
  val lbs = Seq(
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

  val tls = lbs ++ Seq(
    "-Xlint:strict-unsealed-patmat",
    "-Xstrict-patmat-analysis",
    "-Yinduction-heuristics",
    "-Ykind-polymorphism",
    "-Yliteral-types",
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
    scalacOptions := (if (scalaOrganization.value == "org.typelevel") tls else lbs),
    Test / scalacOptions ++= test,
    Compile / compile / scalacOptions ++= compileOnly,
    Test / compile / scalacOptions ++= compileOnly,
  )
}

// Scala.js can't be used with TLS, so only use it in pure Scala contexts.
val typelevelScala = Seq(
  scalaVersion := "2.12.4-bin-typelevel-4",
  scalaOrganization := "org.typelevel",
)

disablePlugins(RevolverPlugin)

ThisBuild / scalafmtOnCompile := true

lazy val predef = project.
  disablePlugins(RevolverPlugin).
  settings(compilerFlags).
  settings(
    name := "frontend-predef"
  )

lazy val resources = project.
  dependsOn(predef).
  disablePlugins(RevolverPlugin).
  enablePlugins(SbtTwirl).
  settings(typelevelScala).
  settings(compilerFlags).
  settings(
    name := "frontend-resources",
    TwirlKeys.templateImports += "xw.frontend._",
    Compile / compile / scalacOptions --= Seq(
      "-Ywarn-unused:imports",
    )
  )

lazy val server = project.
  dependsOn(resources).
  settings(typelevelScala).
  settings(compilerFlags).
  settings(
    name := "frontend-server",
    assembly / assemblyJarName := "frontend.jar",
    assembly / test := (()),
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.12",
      "org.log4s" %% "log4s" % "1.6.1",
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "org.specs2" %% "specs2-core" % "4.2.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.2.0" % Test,
      "rocks.heikoseeberger" %% "accessus" % "2.0.0",
)
  )
