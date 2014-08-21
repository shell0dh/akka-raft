import sbt._
import Keys._

import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.MultiJvm

object ApplicationBuild extends Build {

  val appName = "akka-raft"
  val appVersion = "1.0-SNAPSHOT"
  val appScalaVersion = "2.10.4"

  import Dependencies._
  import Resolvers._

  val debugInUse = SettingKey[Boolean]("debug-in-use", "debug is used")

  lazy val akkaRaft = Project(appName, file("."))
    .configs(MultiJvm)
    .settings(multiJvmSettings: _*)
    .settings(
      resolvers ++= additionalResolvers,
      libraryDependencies ++= generalDependencies,
      scalaVersion := appScalaVersion
    )

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    //disable parallel tests
    parallelExecution in Test := false,
    //make sure that MultiJvm tests are executed by the default test target
    executeTests in Test <<=
      ((executeTests in Test), (executeTests in MultiJvm)) map {
        case ((testResults), (multiJvmResults)) =>
          val overall =
            if (testResults.overall.id < multiJvmResults.overall.id)
              multiJvmResults.overall
            else
              testResults.overall
          Tests.Output(overall,
            testResults.events ++ multiJvmResults.events,
            testResults.summaries ++ multiJvmResults.summaries)
      }

  )

}

object Dependencies {
    val akkaVersion = "2.3.5"
    val generalDependencies = Seq(
      "com.typesafe.akka" %% "akka-actor"     % akkaVersion,
      "com.typesafe.akka" %% "akka-slf4j"     % akkaVersion,

      "com.typesafe.akka" %% "akka-cluster"     % akkaVersion,
      "com.typesafe.akka" %% "akka-persistence-experimental" % akkaVersion,

      "com.typesafe.akka" %% "akka-testkit"            % akkaVersion % "test",
      "com.typesafe.akka" %% "akka-multi-node-testkit" % akkaVersion % "test",

      "org.mockito"        % "mockito-core"   % "1.9.5"     % "test",
      "org.scalatest"     %% "scalatest"      % "2.1.7"       % "test"
    )
  }

object Resolvers {
  val additionalResolvers = Seq(
"Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
      "Spray repo" at "http://repo.spray.io",
      "Typesafe repo" at "http://repo.typesafe.com/typesafe/releases/",
      "releases" at "http://oss.sonatype.org/content/repositories/releases",
      "github-releases" at "http://oss.sonatype.org/content/repositories/github-releases/",
      "cloudera.repos" at "https://repository.cloudera.com/artifactory/libs-release",
      "maven2" at " http://repo1.maven.org/maven2/",
      "Apache repo" at "https://repository.apache.org/content/repositories/releases"
      //"360buy-develop.releases" at "http://artifactory.360buy-develop.com/libs-releases",
      //"360buy-develop.snapshots" at "http://artifactory.360buy-develop.com/libs-snapshots",
      //      "snapshots" at "http://oss.sonatype.org/content/repositories/snapshots",
      //      "plugins-cloudera" at "https://repository.cloudera.com/artifactory/plugins-release"
  )

}
