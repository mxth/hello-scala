name := "hello-scala"

version := "0.1"

scalaVersion := "2.12.10"

val http4sVersion = "0.21.0-M6"
val circeVersion  = "0.12.2"

// Only necessary for SNAPSHOT releases
resolvers += Resolver.sonatypeRepo("snapshots")

libraryDependencies ++= Seq(
  "org.http4s"    %% "http4s-dsl"           % http4sVersion,
  "org.http4s"    %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"    %% "http4s-blaze-client"  % http4sVersion,
  "org.http4s"    %% "http4s-circe"         % http4sVersion,
  "io.circe"      %% "circe-core"           % circeVersion,
  "io.circe"      %% "circe-generic"        % circeVersion,
  "io.circe"      %% "circe-parser"         % circeVersion,
  "io.circe"      %% "circe-shapes"         % circeVersion,
  "io.circe"      %% "circe-generic-extras" % circeVersion,
  "dev.zio"       %% "zio"                  % "1.0.0-RC17",
  "dev.zio"       %% "zio-interop-cats"     % "2.0.0.0-RC10",
  "com.pauldijou" %% "jwt-core"             % "4.2.0"
)

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding",
  "UTF-8",
  "-feature",
  "-unchecked",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-Xfuture",
  "-Ypartial-unification",
  "-Ywarn-dead-code",
  "-Ywarn-macros:after",
  "-Ywarn-value-discard",
  "-Ywarn-unused",
)

addCommandAlias(
  "fmt",
  ";scalafmtSbt;scalafmt;test:scalafmt"
)

addCommandAlias(
  "wip",
  ";fmt;test:compile"
)

addCommandAlias(
  "check",
  ";scalafmtCheck;test:scalafmtCheck;scalafmtSbtCheck"
)
