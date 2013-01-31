resolvers ++= Seq(
  Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
  "coda hale's repo" at "http://repo.codahale.com"
)

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.5")

addSbtPlugin("me.lessis" % "ls-sbt" % "0.1.1")
