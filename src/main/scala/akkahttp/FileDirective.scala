package akkahttp

import java.io.File

import akka.http.scaladsl.model.{HttpEntity, MediaTypes, Multipart}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.stream.Materializer
import akka.stream.io.{SynchronousFileSink, SynchronousFileSource}

import scala.concurrent.{ExecutionContext, Future}


object FileDirective {

  //form field name
  type Name = String

  case class FileInfo(fileName: String, targetFile: String, length: Long)

  private def uploadFileImpl(implicit mat: Materializer, ec: ExecutionContext): Directive1[Future[Map[Name, FileInfo]]] = {
    Directive[Tuple1[Future[Map[Name, FileInfo]]]] { inner =>
      entity(as[Multipart.FormData]) { (formdata: Multipart.FormData) =>
        val fileNameMap = formdata.parts.mapAsync(1) { p =>
          if (p.filename.isDefined) {
            val targetPath = File.createTempFile(s"userfile_${p.name}_${p.filename.getOrElse("")}", "")
            val written = p.entity.dataBytes.runWith(SynchronousFileSink(targetPath))
            written.map(written => 
              Map(p.name -> FileInfo(p.filename.get, targetPath.getAbsolutePath, written)))
          } else {
            Future(Map.empty[Name, FileInfo])
          }
        }.runFold(Map.empty[Name, FileInfo])((set, value) => set ++ value)
        inner(Tuple1(fileNameMap))
      }
    }
  }

  def uploadFile: Directive1[Map[Name, FileInfo]] = {
    Directive[Tuple1[Map[Name, FileInfo]]] { inner =>
      extractMaterializer { mat =>
        extractExecutionContext { ec =>
          implicit val materializer = mat
          implicit val executionContext = ec
          uploadFileImpl(mat, ec) { filesFuture =>
            ctx => {
              filesFuture.map(map => inner(Tuple1(map))).flatMap(route => route(ctx))
            }
          }
        }
      }
    }
  }

  def downloadFile(file: String): Route = {
    val f = new File(file)
    val responseEntity = HttpEntity(
      MediaTypes.`application/octet-stream`,
      f.length,
      SynchronousFileSource(f, chunkSize = 262144))
    complete(responseEntity)
  }
}