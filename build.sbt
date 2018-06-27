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

disablePlugins(RevolverPlugin)

ThisBuild / scalafmtOnCompile := true

val webPackagePrefix = "web/"

lazy val common = crossProject.
  disablePlugins(RevolverPlugin).
  enablePlugins(BuildInfoPlugin).
  settings(compilerFlags).
  settings(
    name := "frontend-common",

    buildInfoPackage := "xw.frontend",
    buildInfoKeys := Seq(
      "webPackagePrefix" → webPackagePrefix,
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
    libraryDependencies += "com.vmunier" %% "scalajs-scripts" % "1.1.2",
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

    libraryDependencies ++= Seq(
      "com.github.japgolly.scalajs-react" %%% "core" % "1.2.0",
      "com.pepegar" %%% "hammock-circe" % "0.8.4",
      "com.pepegar" %%% "hammock-core" % "0.8.4",
    ),
    // Bundler doesn't support source maps, so use the old approach.
    jsDependencies ++= Seq(
      "org.webjars.npm" % "react" % "16.2.0"
        / "umd/react.development.js"
        minified "umd/react.production.min.js"
        commonJSName "React",

      "org.webjars.npm" % "react-dom" % "16.2.0"
        / "umd/react-dom.development.js"
        minified  "umd/react-dom.production.min.js"
        dependsOn "umd/react.development.js"
        commonJSName "ReactDOM",

      "org.webjars.npm" % "react-dom" % "16.2.0"
        / "umd/react-dom-server.browser.development.js"
        minified  "umd/react-dom-server.browser.production.min.js"
        dependsOn "umd/react-dom.development.js"
        commonJSName "ReactDOMServer"
    )
  )

lazy val server = project.
  enablePlugins(SbtWeb).
  dependsOn(`static-resources`).
  settings(compilerFlags).
  settings(
    name := "frontend-server",

    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.12",
      "de.heikoseeberger" %% "akka-http-circe" % "1.21.0",
      "org.log4s" %% "log4s" % "1.6.1",
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "org.specs2" %% "specs2-core" % "4.2.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.2.0" % Test,
      "rocks.heikoseeberger" %% "accessus" % "2.0.0",
    ),

    assembly / assemblyJarName := "frontend.jar",
    assembly / test := (()),

    scalaJSProjects := Seq(client),
    WebKeys.packagePrefix in Assets := webPackagePrefix,
    pipelineStages in Assets := Seq(scalaJSPipeline),
    managedClasspath in Runtime += (packageBin in Assets).value,
    managedClasspath in Test += (packageBin in Assets).value,
  )

