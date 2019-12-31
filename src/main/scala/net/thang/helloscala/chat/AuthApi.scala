package net.thang.helloscala.chat

import cats.data._
import org.http4s._
import org.http4s.dsl.Http4sDsl
import org.http4s.server._
import org.http4s.headers.Authorization
import pdi.jwt.{Jwt, JwtAlgorithm}
import io.circe.generic.extras.auto._
import io.circe.parser.decode
import io.circe.syntax._
import pdi.jwt.algorithms.JwtHmacAlgorithm
import zio.RIO
import zio.interop.catz._

case class AuthUser(id: Long, name: String)

object AuthApi {
  val dsl: Http4sDsl[ChatTask] = Http4sDsl[ChatTask]
  import dsl._

  val key                    = "secretKey"
  val algo: JwtHmacAlgorithm = JwtAlgorithm.HS256

  val authUser: Kleisli[ChatTask, Request[ChatTask], Either[String, AuthUser]] = Kleisli({ request =>
    val user = for {
      header  <- request.headers.get(Authorization).toRight("Couldn't find an Authorization header")
      content <- Jwt.decodeRaw(header.value.replace("Bearer ", ""), key, Seq(algo)).toOption.toRight("Decode failed")
      user    <- decode[AuthUser](content).left.map(_ => "Invalid token")
    } yield user
    RIO.succeed(user)
  })

  val onFailure: AuthedRoutes[String, ChatTask] = Kleisli(
    req =>
      OptionT.liftF {
        Forbidden(req.context)
      }
  )

  val middleware: AuthMiddleware[ChatTask, AuthUser] = AuthMiddleware(authUser, onFailure)

  val authRoutes: AuthedRoutes[AuthUser, ChatTask] =
    AuthedRoutes.of {
      case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.name}")
    }

  val service: HttpRoutes[ChatTask] = middleware(authRoutes)

  def generateToken: String = Jwt.encode(AuthUser(0, "thang").asJson.noSpaces, key, algo)
}
