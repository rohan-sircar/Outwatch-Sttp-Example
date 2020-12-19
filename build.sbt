//organization := "wow.doge"
//name := "Outwatch-example"
//version := "0.1.0"

scalaVersion := "2.13.4"
resolvers += "jitpack" at "https://jitpack.io"

libraryDependencies ++= Seq(
  "com.github.outwatch.outwatch" %%% "outwatch" % "61deece8",
  "com.github.outwatch.outwatch" %%% "outwatch-util" % "master-SNAPSHOT",
  "com.github.cornerman.colibri" %%% "colibri-monix" % "master-SNAPSHOT",
  "com.github.outwatch.outwatch" %%% "outwatch-monix" % "master-SNAPSHOT",
  "org.scalatest" %%% "scalatest" % "3.2.0" % Test,
  "org.typelevel" %%% "cats-core" % "2.1.1",
  "org.typelevel" %%% "cats-effect" % "2.1.4",
  "io.monix" %%% "monix" % "3.2.2",
  "io.monix" %%% "monix-bio" % "1.1.0",
  "com.softwaremill.sttp.client" %%% "core" % "2.2.5",
  "com.softwaremill.sttp.client" %%% "monix" % "2.2.5",
  "com.softwaremill.sttp.client" %%% "circe" % "2.2.5"
)

enablePlugins(ScalaJSBundlerPlugin)
useYarn := true // makes scalajs-bundler use yarn instead of npm
requireJsDomEnv in Test := true
scalaJSUseMainModuleInitializer := true
scalaJSLinkerConfig ~= (_.withModuleKind(
  ModuleKind.CommonJSModule
)) // configure Scala.js to emit a JavaScript module instead of a top-level script

scalacOptions ++=
  Seq(
    "-encoding",
    "UTF-8",
    "-deprecation",
    "-feature",
    "-language:existentials",
    "-language:experimental.macros",
    "-language:higherKinds",
    "-language:implicitConversions",
    "-unchecked",
    "-Xlint",
    "-Ywarn-numeric-widen",
    "-Ymacro-annotations",
    //silence warnings for by-name implicits
    "-Wconf:cat=lint-byname-implicit:s",
    //give errors on non exhaustive matches
    "-Wconf:msg=match may not be exhaustive:e",
    "-explaintypes" // Explain type errors in more detail.
  )

// hot reloading configuration:
// https://github.com/scalacenter/scalajs-bundler/issues/180
addCommandAlias(
  "dev",
  "; compile; fastOptJS::startWebpackDevServer; devwatch; fastOptJS::stopWebpackDevServer"
)
addCommandAlias("devwatch", "~; fastOptJS; copyFastOptJS")

version in webpack := "4.43.0"
version in startWebpackDevServer := "3.11.0"
webpackDevServerExtraArgs := Seq("--progress", "--color")
webpackDevServerPort := 8080
webpackConfigFile in fastOptJS := Some(
  baseDirectory.value / "webpack.config.dev.js"
)

webpackBundlingMode in fastOptJS := BundlingMode
  .LibraryOnly() // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance

// when running the "dev" alias, after every fastOptJS compile all artifacts are copied into
// a folder which is served and watched by the webpack devserver.
// this is a workaround for: https://github.com/scalacenter/scalajs-bundler/issues/180
lazy val copyFastOptJS =
  TaskKey[Unit]("copyFastOptJS", "Copy javascript files to target directory")
copyFastOptJS := {
  val inDir = (crossTarget in (Compile, fastOptJS)).value
  val outDir = (crossTarget in (Compile, fastOptJS)).value / "dev"
  val files = Seq(
    name.value.toLowerCase + "-fastopt-loader.js",
    name.value.toLowerCase + "-fastopt.js",
    name.value.toLowerCase + "-fastopt.js.map"
  ) map { p => (inDir / p, outDir / p) }
  IO.copy(
    files,
    overwrite = true,
    preserveLastModified = true,
    preserveExecutable = true
  )
}
