name := "category-api"

version := "1.1.0"

scalaVersion := "2.12.3"

// Scala compiler options
scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-Xfatal-warnings",
  "-Xlint"
)

libraryDependencies ++= {
  val akkaVersion = "2.5.4"
  val akkaHttpVersion = "10.0.9"
  val akkaJsonSupportVersion = "1.17.0"
  val circeVersion = "0.8.0"
  val shapelessVersion = "2.3.2"
  val catsVersion = "0.9.0"
  val akkaCorsVersion = "0.2.1"
  val redisScalaVersion = "1.8.0"
  Seq(
    // Akka: http://akka.io/
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-testkit" % akkaVersion % "test",
    "com.typesafe.akka" %% "akka-actor" % akkaVersion,
    "com.typesafe.akka" %% "akka-stream" % akkaVersion,
    "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
    "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % "test",
    "de.heikoseeberger" %% "akka-http-circe" % akkaJsonSupportVersion,
    "ch.megard" %% "akka-http-cors" % akkaCorsVersion,
    // Circe JSON library: https://github.com/travisbrown/circe
    "io.circe" %% "circe-core" % circeVersion,
    "io.circe" %% "circe-generic" % circeVersion,
    "io.circe" %% "circe-parser" % circeVersion,
    // Shapeless generic programming library: https://github.com/milessabin/shapeless
    "com.chuusai" %% "shapeless" % shapelessVersion,
    // Cats functional programming library: https://github.com/typelevel/cats
    "org.typelevel" %% "cats" % catsVersion,
    // Redis Scala - https://github.com/etaty/rediscala/pulls
    "com.github.etaty" %% "rediscala" % redisScalaVersion
  )
}

/************    TEST    *************/

libraryDependencies ++= {
  val specs2Version = "3.9.4"
  val specs2ScalaMockVersion = "3.6.0"
  Seq(
    // Specs2 Test Framework - https://etorreborre.github.io/specs2/
    "org.specs2" %% "specs2-core" % specs2Version % "test",
    "org.specs2" %% "specs2-matcher-extra" % specs2Version % "test",
    "org.specs2" %% "specs2-junit" % specs2Version % "test",
    "org.scalamock" %% "scalamock-specs2-support" % specs2ScalaMockVersion % "test"
  )
}

// Test options
scalacOptions in Test ++= Seq("-Yrangepos")
parallelExecution in Test := true
testOptions += Tests.Argument(TestFrameworks.JUnit, "-q", "-v", "-s", "-a")

/************    WartRemover    *************/

wartremoverErrors ++= Warts.allBut(
  Wart.FinalCaseClass,
  Wart.ImplicitParameter,
  Wart.NonUnitStatements,
  Wart.Overloading,
  Wart.PublicInference,
  Wart.Nothing
)
