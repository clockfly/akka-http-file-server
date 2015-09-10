# akka-http-file-server

A file server example to download/upload large files with akka-http.

## How to use
```sbt "run-main akkahttp.TestApp"```

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

