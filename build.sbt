libraryDependencies ++= Seq(
    "commons-codec" % "commons-codec" % "1.5+" % "provided",
    "org.specs2" %% "specs2" % "1.7.1" % "test"
)

scalacOptions := Seq("-deprecation", "-encoding", "utf8")