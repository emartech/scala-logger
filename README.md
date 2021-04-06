# Scala Logger [![Build Status](https://github.com/emartech/scala-logger/workflows/CI/badge.svg)](https://github.com/emartech/scala-logger/actions?query=workflow%3ACI) [![Maven Central](https://img.shields.io/maven-central/v/com.emarsys/scala-logger_2.12.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.emarsys%22%20AND%20a:%22scala-logger_2.12%22)

A simple logging wrapper library that provides scala idiomatic context propagation wrapping Logback logger.

## Usage (for 0.8.0 and up)

Add to sbt:

```sbt
libraryDependencies += "com.emarsys" %% "scala-logger" % "x.y.z"
```

The latest released version can be found on the maven badge above.

### Logging creation

#### Cats Effect 2.x

In order to use this library with cats effect series 2.x, add the `ce2` interop module to your dependencies.

```sbt
libraryDependencies += "com.emarsys" %% "scala-logger-ce2" % "x.y.z"
```

You create a `Logging` instance either for a generic `F[_]` that implements `Sync`, or specifically for `IO`

```scala
implicit val ioLogging: IO[Logging[IO]] = CatsEffectLogging.createEffectLogger[IO]("application")
implicit val fLogging: F[Logging[F]] = CatsEffectLogging.createEffectLogger[F]("application")
```

In some scenarios (e.g. when using ReaderT aka. Logged), it is necessary to create the logger in a different effect then
the one the logger will use. To do this, you can use the `CatsEffectLogging.createEffectLoggerG` method.

```scala
implicit val ioLogging: F[Logging[Logged[F]]] =
  CatsEffectLogging.createEffectLoggerG[F, Logged[F]]("application")
```

#### Cats Effect 3.x

There were several breaking changes in cats effect 3 which means a separate interop module is necessary:

```sbt
libraryDependencies += "com.emarsys" %% "scala-logger-ce2" % "x.y.z"
```

To create a `Logging` instance, you can use the exact same methods as in the case of cats effect 2.

#### Sync logging

If you need to log in a context where it is not possible to provide a `Logging` instance (e.g. in a JVM shutdown hook),
you can use the unsafe logger utilities to create a global `Logging[Id]` instance, which will be used to log instead. To
access this instance, import the contents of the unsafe package:

```scala
import com.emarsys.logger.unsafe._
```

#### Future logging

TODO

### Logging

Given an implicit `Logging` instance, you can use the expected functions in the `log` package to log messages. Most log
functions accept a `String` message, a [LoggingContext](#context-propagation) and warn or error accepts a `Throwable`.

```scala
import com.emarsys.logger.log

implicit val logging: Logging[F] = ???

val context = LoggingContext("main")

log.info("Hello there.")(context)
log.error("Oh snap!", new RuntimeException())(context)
```

### Context propagation

All log methods expect some form of `LoggingContext`. This can be propagated several ways depending on the
`F` you use as effect.

#### Implicit parameters

The most straightforward way is to manually propagate the `LoggingContext` across functions

```scala
def handleRequest[F[_]: Monad: Logging](request: Request)(implicit context: LoggingContext): F[Response] = {
  log.debug("Received request") *>
    doStuffInDb(request.user) *>
    respondWith200
}

def doStuffInDb[F[_]: Monad: Logging](user: User)(implicit context: LoggingContext): F[Unit] =
  accessDb *>
    log.info("User accessed database")(context.addParameters("user" -> user.name))

def main() = {
  CatsEffectLogging.createEffectLogger[IO]("application").flatMap { implicit logging
    val request = ???
    implicit val context = LoggingContext(request.id)

    handleRequest[IO](request)    
  }  
}
```

As you can see from the "Request received" log, you don't have to pass the context explicitly, but it certainly is an
option. Propagating this way is simple, but extending the context is not easy while keeping it implicit, as declaring
a new, modified `LoggingContext` implicit inside a function will cause ambiguous implicit error.

#### Kleisli (ReaderT)

Arguably the most complicated method of passing context around is via the 
[Kleisli](https://typelevel.org/cats/datatypes/kleisli.html) monad transformer. This allows passing context around
without any effect on the function signature.

> :bangbang: **Kleisli is not [stack safe](https://github.com/typelevel/cats/issues/2212) for all operations**

Accessing the request log is done through the `Context[F]` typeclass, which is an alias of 
`Local[F, LoggingContext]`.

```scala
def handleRequest[F[_]: Monad: Logging](request: Request): F[Response] = {
  log.debug("Received request") *>
    doStuffInDb(request.user) *>
    respondWith200
}

def doStuffInDb[F[_]: Monad: Logging](user: User): F[Unit] = for {
  context <- log.getContext
  _ <- accessDb *> log.info("User accessed database")(context.addParameters("user" -> user.name))
} yield ()

def main() = {
  CatsEffectLogging.createEffectLoggerG[LoggedIO, IO]("application").flatMap { implicit logging
    val request = ???
    val context = LoggingContext(request.id)

    handleRequest[LoggedIO](request).run(context)    
  }  
}
```

#### Fiber local data (Cats Effect 3 only)

Cats Effect 3 supports fiber local data through the new `IOLocal` class. This class allows storing globally accessible
data belonging to a single fiber.

```scala
def handleRequest[F[_]: Monad: Logging](request: Request): F[Response] = {
  log.debug("Received request") *>
    doStuffInDb(request.user) *>
    respondWith200
}

def doStuffInDb[F[_]: Monad: Logging](user: User): F[Unit] = for {
  context <- log.getContext
  _ <- accessDb *> log.info("User accessed database")(context.addParameters("user" -> user.name))
} yield ()

def main() = {
  CatsEffectLogging.createEffectLogger[IO]("application").flatMap { implicit logging
    val mainContext = LoggingContext("main")
    CatsEffectLogging.createIOLocalContext(context).flatMap { implicit context =>
      val request = ???
      val context = LoggingContext(request.id)
      
      log.setContext(context) {
        handleRequest[IO](request)
      }
    }
  }  
}
```

This method has the advantage of being faster than Kleisli and it is stack safe.

> :warning: Every time you call `createIOLocalContext`, an `IOLocal` gets permanently associated to the
> current running fiber. This means calling it several times on the same fiber will cause memory leak if
> you reuse that fiber. You should prefer creating one context and changing it's contents using log.setContext(...).

### Manipulating context

```scala
log.extendContext("user" -> user.name) {
  log.info("hello1") // will log user name
} *>
  log.info("hello2") // will not log user name
```
