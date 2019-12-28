package net.thang.helloscala.chat

import fs2.{Pipe, Stream}
import fs2.concurrent.Topic
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._
import zio.RIO
import zio.interop.catz._

import io.circe.generic.extras.auto._
import io.circe.parser.decode, io.circe.syntax._

object ChatApi {
  val dsl: Http4sDsl[ChatTask] = Http4sDsl[ChatTask]
  import dsl._

  def route(eventsTopic: Topic[ChatTask, ChatEvent]): HttpRoutes[ChatTask] =
    HttpRoutes
      .of[ChatTask] {
        case GET -> Root / "ws" =>
          val toClient: Stream[ChatTask, WebSocketFrame] = eventsTopic
            .subscribe(10)
            .map(event => Text(event.asJson.noSpaces))

          val fromClient: Pipe[ChatTask, WebSocketFrame, Unit] = _.evalMap {
            case Text(t, _) =>
              RIO
                .fromEither(decode[ChatCommand](t))
                .foldM(
                  e => RIO.succeed(println(e)), {
                    case SendChatMessage(message) => eventsTopic.publish1(ChatMessageSent(message))
                  }
                )
            case f => RIO.succeed(println(s"Unknown type: $f"))
          }

          WebSocketBuilder[ChatTask].build(toClient, fromClient)
      }
}
