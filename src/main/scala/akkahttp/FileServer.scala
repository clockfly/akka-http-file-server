package akkahttp

import java.io.File
import akka.http.scaladsl.model.Uri.Path
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import akka.stream.io.{SynchronousFileSink, SynchronousFileSource}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString

import scala.concurrent.{ExecutionContext, Future}

class FileServer(system: ActorSystem, port: Int) {
  import system.dispatcher
  implicit val actorSystem = system
  implicit val materializer = ActorMaterializer()

  import akka.http.scaladsl.server.Directives._
    val route: Route = {
    path("upload") {
      entity(as[Multipart.FormData]) { (formdata: Multipart.FormData) =>
        val fileNamesFuture = formdata.parts.mapAsync(1) { p =>
          val written = p.entity.dataBytes.runWith(SynchronousFileSink(new File(s"C:\\Users\\xzhong10\\Downloads\\${p.filename}")))
          written.map((p.filename.get, _))
        }.runFold(Seq.empty[(String, Long)])((set, value) => set :+ value).map(_.mkString(", "))
        fileNamesFuture

        complete {
          fileNamesFuture
        }
      }
    } ~
    path("download") {
      val f = new File("C:\\Users\\xzhong10\\Downloads\\ideaIU-14.1.4.exe")
      val responseEntity = HttpEntity(
        MediaTypes.`application/octet-stream`,
        f.length,
        SynchronousFileSource(f, chunkSize = 262144))
      complete(responseEntity)
    }
  }
  def start = {
    Http().bindAndHandle(Route.handlerFlow(route), "localhost", port = port)
  }
}

object FileServer {
  class Client(system: ActorSystem, serverAddress: String) {
    private implicit val actorSystem = system
    private implicit val materializer = ActorMaterializer()
    private implicit val ec = system.dispatcher

    val server = Uri(serverAddress)
    val httpClient = Http(system).outgoingConnection(server.authority.host.address(), server.authority.port)

    def upload(file: File): Future[String] = {
      val target = server.withPath(Path("upload"))

      val request = entity(file).map{entity =>
        HttpRequest(HttpMethods.POST, uri = target, entity = entity)
      }

      val response = Source(request).via(httpClient).runWith(Sink.head)
      response.flatMap(Unmarshal(_).to[String])
    }

    def download(saveAs: File): Unit = {
      val downoad = server.withPath(Path("download"))
      //download file to local
      val response = Source.single(HttpRequest(uri = downoad)).via(httpClient).runWith(Sink.head)
      val xx = response.flatMap { response =>
        response.entity.dataBytes.runWith(SynchronousFileSink(saveAs))
      }
      xx.foreach{some =>
        println("we downloaded file with size " + some)
      }
    }

    private def entity(file: File)(implicit ec: ExecutionContext): Future[RequestEntity] = {
      val entity =  HttpEntity(MediaTypes.`application/octet-stream`, file.length(), SynchronousFileSource(file, chunkSize = 100000))
      val body = Source.single(
        Multipart.FormData.BodyPart(
          "test",
          entity,
          Map("filename" -> file.getName)))
      val form = Multipart.FormData(body)

      Marshal(form).to[RequestEntity]
    }
  }
}