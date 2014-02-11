import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm
  ) 


}
