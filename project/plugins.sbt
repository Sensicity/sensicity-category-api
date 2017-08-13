// -------- EXTRA REPOSITORIES --------- //
resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

// ----------- SBT PLUGINS -------------- //
// https://github.com/sbt/sbt-assembly
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.1")
// https://github.com/scoverage/sbt-scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
// https://github.com/codacy/sbt-codacy-coverage
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.8")
// https://github.com/puffnfresh/wartremover
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.1.1")

// ------------- SBT OPTIONS ----------- //
logLevel := Level.Warn
