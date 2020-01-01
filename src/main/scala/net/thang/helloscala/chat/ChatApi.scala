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
import io.circe.parser.decode
import io.circe.syntax._
import cats.syntax.semigroupk._

object ChatApi {
  val dsl: Http4sDsl[ChatTask] = Http4sDsl[ChatTask]
  import dsl._

  def websocket(eventsTopic: Topic[ChatTask, ChatEvent]): AuthedRoutes[AuthUser, ChatTask] =
    AuthedRoutes.of {
      case GET -> Root / "ws" as user =>
        val toClient: Stream[ChatTask, WebSocketFrame] = eventsTopic
          .subscribe(10)
          .map(event => Text(event.asJson.noSpaces))

        val fromClient: Pipe[ChatTask, WebSocketFrame, Unit] = _.evalMap {
          case Text(t, _) =>
            RIO
              .fromEither(decode[ChatCommand](t))
              .foldM(
                e => RIO.succeed(println(e)),
                command =>
                  command.payload match {
                    case SendChatMessage(message) => eventsTopic.publish1(ChatMessageSent(message))
                  }
              )
          case f => RIO.succeed(println(s"Unknown type: $f"))
        }

        WebSocketBuilder[ChatTask].build(toClient, fromClient)
    } <+> AuthApi.service

  def routes(eventsTopic: Topic[ChatTask, ChatEvent]): HttpRoutes[ChatTask] =
    HttpRoutes
      .of[ChatTask] {
        case GET -> Root / "token" => Ok(AuthApi.generateToken)
      } <+> AuthApi.middleware(websocket(eventsTopic)) <+> AuthApi.service
}
