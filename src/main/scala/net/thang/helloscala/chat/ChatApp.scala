package net.thang.helloscala.chat

import fs2.concurrent.Topic
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.CORS
import zio._
import zio.clock.Clock
import zio.console.Console
import zio.interop.catz._

object ChatApp extends CatsApp {
  val server: ChatTask[Unit] =
    for {
      topic <- Topic[ChatTask, ChatEvent](Initial())
      _ <- ZIO
            .runtime[ChatEnvironment]
            .flatMap { implicit rts =>
              BlazeServerBuilder[ChatTask]
                .bindHttp(8080, "0.0.0.0")
                .withHttpApp(CORS(ChatApi.route(topic).orNotFound))
                .serve
                .compile
                .drain
            }

    } yield ()

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
