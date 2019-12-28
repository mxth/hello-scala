package net.thang.helloscala.chat

import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.interop.catz._

object ChatApp extends CatsApp {

  val server: ChatTask[Unit] = ZIO
    .runtime[ChatEnvironment]
    .flatMap { implicit rts =>
      BlazeServerBuilder[ChatTask]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(CORS(ChatApi.route(ChatEvent.eventStream).orNotFound))
        .serve
        .compile
        .drain
    }

  def run(args: List[String]): ZIO[ZEnv, Nothing, Int] =
    for {
      repo <- ChatRepository.InMem
      program <- server
                  .provideSome[ZEnv] { _ =>
                    new Console.Live with Clock.Live with ChatRepository {
                      val chatRepository: ChatRepository.Service = repo.chatRepository
                    }
                  }
                  .fold(_ => 1, _ => 0)
    } yield program
}
