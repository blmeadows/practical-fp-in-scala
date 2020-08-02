name := "practical-fp-in-scala"

version := "0.1"

ThisBuild / scalaVersion := "2.13.0"

libraryDependencies ++= Seq(
  compilerPlugin(
    "org.typelevel" %% "kind-projector" % "0.11.0" cross CrossVersion.full
  ),
  compilerPlugin(
    "org.augustjune" %% "context-applied" % "0.1.2"
  ),
  "org.typelevel"  %% "cats-core"           % "2.1.0",
  "org.typelevel"  %% "cats-effect"         % "2.1.0",
  "dev.profunktor" %% "console4cats"        % "0.8.1",
  "org.manatki"    %% "derevo-cats"         % "0.10.5",
  "org.manatki"    %% "derevo-cats-tagless" % "0.10.5",
  "co.fs2"         %% "fs2-core"            % "2.2.2",
  "com.olegpy"     %% "meow-mtl-core"       % "0.4.0",
  "com.olegpy"     %% "meow-mtl-effects"    % "0.4.0",
  "io.estatico"    %% "newtype"             % "0.4.3",
  "eu.timepit"     %% "refined"             % "0.9.12",
  "org.http4s"     %% "http4s-dsl"          % "0.21.3",
  "io.circe"       %% "circe-core"          % "0.13.0",
  "dev.profunktor" %% "redis4cats-effects"  % "0.10.2",
  "org.typelevel"  %% "squants"             % "1.6.0",
  "com.github.julien-truffaut" %% "monocle-core"   % "2.0.1",
  "com.github.julien-truffaut" %% "monocle-macro"  % "2.0.1",
  "com.github.cb372"           %% "cats-retry"     % "1.1.1",
  "io.chrisdavenport"          %% "log4cats-slf4j" % "1.0.0"
  )

scalacOptions += "-Ymacro-annotations"
