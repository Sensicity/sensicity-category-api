// -------- EXTRA REPOSITORIES --------- //
resolvers += "Typesafe Repository" at "https://repo.typesafe.com/typesafe/releases/"

// ----------- SBT PLUGINS -------------- //
// https://github.com/sbt/sbt-assembly
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")
// https://github.com/rtimush/sbt-updates
addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.0")
// https://github.com/scoverage/sbt-scoverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")
// https://github.com/codacy/sbt-codacy-coverage
addSbtPlugin("com.codacy" % "sbt-codacy-coverage" % "1.3.7")
// https://github.com/sbt/sbt-native-packager
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.1.4")
// https://github.com/puffnfresh/wartremover
addSbtPlugin("org.wartremover" % "sbt-wartremover" % "2.0.2")

// ------------- SBT OPTIONS ----------- //
logLevel := Level.Warn
