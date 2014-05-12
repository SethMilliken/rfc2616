name := "RFC2616"

version := "0.1"

scalaVersion := "2.11.0"

resolvers += "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += "Sonatype" at "https://oss.sonatype.org/content/groups/public"

libraryDependencies += "com.typesafe.akka" %% "akka-actor" % "2.3.2"

libraryDependencies += "com.typesafe.akka" %% "akka-kernel" % "2.3.2"

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.5" % "test"

// Show durations
testOptions in Test += Tests.Argument("-oD")
