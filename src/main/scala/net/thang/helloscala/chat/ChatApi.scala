package net.thang.helloscala.chat

import io.circe.generic.auto._
import io.circe.parser.decode
import io.circe.syntax._
import fs2._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server.websocket._
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame._
import zio.RIO
import zio.interop.catz._

object ChatApi {
  val dsl: Http4sDsl[ChatTask] = Http4sDsl[ChatTask]
  import dsl._

  def route(eventStream: Stream[ChatTask, ChatEvent]): HttpRoutes[ChatTask] =
    HttpRoutes
      .of[ChatTask] {
        case GET -> Root / "ws" =>
          val toClient: Stream[ChatTask, WebSocketFrame] = eventStream
            .map {
              case ChatMessageSent(message) => message
            }
            .map(json => Text(json.content))

          val fromClient: Pipe[ChatTask, WebSocketFrame, Unit] = _.evalMap {
            case Text(t, _) =>
              RIO
                .fromEither(decode[ChatCommand](t))
                .foldM(e => RIO.succeed(println(e)), ChatCommand.handle)
            case f => RIO.succeed(println(s"Unknown type: $f"))
          }

          WebSocketBuilder[ChatTask].build(toClient, fromClient)
      }
}
