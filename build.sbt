resolvers += Resolver.bintrayRepo("kamon-io", "snapshots")

lazy val root = (project in file("."))
  .settings(
    organization := "io.kamon",
    name := "kamon-meta-reporter")
  .settings(
    libraryDependencies ++= Seq(
      "io.kamon"      %% "kamon-core"   % "1.2.0-M1",
      "org.nanohttpd" %  "nanohttpd"    % "2.3.1",
      "com.grack"     %  "nanojson"     % "1.1"
    ))