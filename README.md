# akka-http-file-server

A file server example to download/upload large files with akka-http.

## How to use
```sbt "run-main akkahttp.TestApp"```

Then Browser http://127.0.0.1:9112/

## How it is implemented?
We create a akka-http DSL for this, so it is super easy to create a http file server:

```
  val route: Route = {
    path("upload") {
      uploadFile { fileMap =>
        complete(ToResponseMarshallable(fileMap))
      }
    } ~
    path("download") {
      parameters("file") { file =>
        downloadFile(file)
      }
    } 
```

## Used by
Big data streaming engine over Akka:

Project Gearpump:  https://github.com/gearpump/gearpump
