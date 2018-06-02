val commonScalaFlags = Seq(
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
  "-Ywarn-unused:locals",
  "-Ywarn-unused:params",
  "-Ywarn-unused:patvars",
  "-Ywarn-unused:privates",
  "-Ywarn-value-discard",
)

val devMode = sys.props.get("devMode").getOrElse("true").toBoolean

lazy val frontend = crossProject.in(file(".")).
  settings(
    // Basic build information
    organization := "xw",
    name := "frontend",
    version := "0.0.1-SNAPSHOT",

    // Scala configuration
    scalaVersion := "2.12.6",
    scalacOptions ++= commonScalaFlags,
    Test / scalacOptions += "-Yrangepos",
    // Hide all this away where IntelliJ can't find it because it breaks
    // IntelliJ's Scala worksheets.
    Compile / compile / scalacOptions ++= compileOnly,
    Test / compile / scalacOptions ++= compileOnly,

    // Formatting: make everything pretty
    scalafmtOnCompile := true,
  )

lazy val frontendJS = frontend.js.
  disablePlugins(AssemblyPlugin, RevolverPlugin).
  settings(
    // Consistent naming
    artifactPath in Compile in fastOptJS :=
      (crossTarget in fastOptJS).value / ((moduleName in fastOptJS).value + ".js"),
    artifactPath in Compile in fullOptJS :=
      (crossTarget in fullOptJS).value / ((moduleName in fullOptJS).value + ".js"),
  )

lazy val frontendJVM = frontend.jvm.
  enablePlugins(SbtTwirl).
  settings(
    // Dependencies
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http" % "10.1.1",
      "com.typesafe.akka" %% "akka-http-testkit" % "10.1.1" % Test,
      "com.typesafe.akka" %% "akka-stream" % "2.5.12",
      "org.log4s" %% "log4s" % "1.6.1",
      "org.slf4j" % "slf4j-simple" % "1.7.25",
      "org.specs2" %% "specs2-core" % "4.2.0" % Test,
      "org.specs2" %% "specs2-scalacheck" % "4.2.0" % Test,
      "rocks.heikoseeberger" %% "accessus" % "2.0.0",
    ),

    // Depend on the client stuff
    Compile / resources += (frontendJS / Compile / packageMinifiedJSDependencies).value,
    if (devMode) Compile / resources += (frontendJS / Compile / fastOptJS).value.data
    else Compile / resources += (frontendJS / Compile / fullOptJS).value.data,

    // Assembly
    assembly / assemblyJarName := "frontend.jar",
    assembly / test := (()),

    // Twirl
    TwirlKeys.templateImports += "xw.frontend._"
  )

    
