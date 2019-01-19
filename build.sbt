// import bintray.BintrayPlugin

resolvers += Resolver.bintrayRepo("kamon-io", "snapshots")

organization := "io.kamon"

name := "kamon-meta-reporter"

crossScalaVersions := Seq("2.11.11", "2.12.7")

libraryDependencies ++= Seq(
  "io.kamon"      %% "kamon-core"   % "1.2.0-M1",
  "org.nanohttpd" %  "nanohttpd"    % "2.3.1",
  "com.grack"     %  "nanojson"     % "1.1"
)

bintrayRepository := "kamon"

licenses := Seq(("GPL-2.0", url("http://www.opensource.org/licenses/gpl-2.0.php")))

version := "0.1.0-SNAPSHOT"