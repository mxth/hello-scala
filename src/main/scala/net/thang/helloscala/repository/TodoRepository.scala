package net.thang.helloscala.repository

import net.thang.helloscala.{Todo, TodoId}
import zio.{Ref, ZIO}

final case class TodoCreateData(content: String) {
  def asTodo(id: TodoId): Todo = Todo(id, this.content)
}

trait TodoRepository {
  val todoRepository: TodoRepository.Service[Any]
}

object TodoRepository {
  trait Service[R] {
    def create(todoCreateData: TodoCreateData): ZIO[R, Nothing, Todo]
    def getAll: ZIO[R, Nothing, List[Todo]]
    def getById(id: TodoId): ZIO[R, Nothing, Option[Todo]]
    def delete(id: TodoId): ZIO[R, Nothing, Unit]
  }

  def mkInMemRepository: ZIO[Any, Nothing, TodoRepositoryInMem] =
    for {
      ref     <- Ref.make(Map.empty[TodoId, Todo])
      counter <- Ref.make(0L)
    } yield new TodoRepositoryInMem(ref, counter)
}

final class TodoRepositoryInMem(ref: Ref[Map[TodoId, Todo]], counter: Ref[Long]) extends TodoRepository {
  override val todoRepository: TodoRepository.Service[Any] = new TodoRepository.Service[Any] {
    def create(todoCreateData: TodoCreateData): ZIO[Any, Nothing, Todo] =
      for {
        newId <- counter.update(_ + 1).map(TodoId)
        todo  = todoCreateData.asTodo(newId)
        _     <- ref.update(_ + (newId -> todo))
      } yield todo

    def getAll: ZIO[Any, Nothing, List[Todo]] =
      ref.get.map(_.values.toList)

    def getById(id: TodoId): ZIO[Any, Nothing, Option[Todo]] =
      ref.get.map(_.get(id))

    def delete(id: TodoId): ZIO[Any, Nothing, Unit] =
      ref.update(_ - id).unit
  }
}
