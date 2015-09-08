package akkahttp

import java.io.File

import akka.actor.ActorSystem
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.Await

object TestApp extends App {

  val testConf: Config = ConfigFactory.parseString("""
    akka.loglevel = INFO
    akka.http.client.parsing.max-content-length = 2048m
    akka.log-dead-letters = off""")

  implicit val system = ActorSystem("ServerTest", testConf)
  implicit val dispatcher = system.dispatcher

  val server = new FileServer(system, 9112)

  //start file server
  val binding = server.start

  val uri = server.uploadAddress

  val client = new FileServer.Client(system)

  client.download


  //val future = client.upload(new File("C:\\Users\\xzhong10\\Downloads\\ideaIU-14.1.4.exe"))
  //import scala.concurrent.duration._
  //println(Await.result(future, 10 seconds))

  //binding.foreach(_.unbind())

  system.awaitTermination()
}