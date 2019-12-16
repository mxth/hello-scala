package net.thang.helloscala

import io.circe.generic.auto._
import io.circe._
import net.thang.helloscala.repository._
import org.http4s._
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import zio.RIO
import zio.interop.catz._

final case class TodoApi[R <: TodoRepository]() {
  type TodoTask[A] = RIO[R, A]

  val dsl: Http4sDsl[TodoTask] = Http4sDsl[TodoTask]
  import dsl._

  implicit def circeJsonDecoder[A](implicit decoder: Decoder[A]): EntityDecoder[TodoTask, A] =
    jsonOf[TodoTask, A]

  implicit def circeJsonEncoder[A](implicit encoder: Encoder[A]): EntityEncoder[TodoTask, A] =
    jsonEncoderOf[TodoTask, A]

  def route =
    HttpRoutes
      .of[TodoTask] {
        case GET -> Root => getAll.flatMap(Ok(_))

        case req @ POST -> Root =>
          req.decode[TodoCreateData] { data =>
            create(data).flatMap(Created(_))
          }

        case DELETE -> Root / LongVar(id) =>
          for {
            todoOption <- getById(TodoId(id))
            response   <- todoOption.fold(NotFound())(todo => delete(todo.id).flatMap(Ok(_)))
          } yield response
      }
      .orNotFound
}
