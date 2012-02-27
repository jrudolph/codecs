libraryDependencies ++= Seq(
    "commons-codec" % "commons-codec" % "1.5+" % "provided; test",
    "org.specs2" %% "specs2" % "1.7.1" % "test",
    "org.scala-tools.testing" % "scalacheck_2.9.1" % "1.9" % "test"
)

scalacOptions := Seq("-deprecation", "-encoding", "utf8")