package net.thang.helloscala

import net.thang.helloscala.repository.TodoRepository
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.clock.Clock
import zio.interop.catz._

object TodoApp extends App {
  type TodoAppEnvironment = Clock with TodoRepository

  type TodoTask[A] = RIO[TodoAppEnvironment, A]

  val server: ZIO[TodoAppEnvironment, Throwable, Unit] = ZIO
    .runtime[TodoAppEnvironment]
    .flatMap { implicit rts =>
      BlazeServerBuilder[TodoTask]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(CORS(TodoApi().route))
        .serve
        .compile
        .drain
    }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
      repo <- TodoRepository.mkInMemRepository
      program <- server
                  .provideSome[ZEnv] { _ =>
                    new Clock.Live with TodoRepository {
                      val todoRepository: TodoRepository.Service[Any] = repo.todoRepository
                    }
                  }
                  .fold(_ => 1, _ => 0)
    } yield program
}
