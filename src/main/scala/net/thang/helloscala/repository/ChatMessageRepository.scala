package net.thang.helloscala.repository

import net.thang.helloscala.{ChatMessage, Todo, TodoId, repository}
import zio.{Ref, ZIO}

trait ChatMessageRepository {
  val chatMessageRepository: TodoRepository.Service[Any]
}

object ChatMessageRepository {
  trait Service[R] {
    def add(message: ChatMessage): ZIO[R, Nothing, Unit]
    def getHistory: ZIO[R, Nothing, List[ChatMessage]]
  }

  def mkInMemRepository: ZIO[Any, Nothing, TodoRepositoryInMem] =
    for {
      ref     <- Ref.make(Map.empty[TodoId, Todo])
      counter <- Ref.make(0L)
    } yield new TodoRepositoryInMem(ref, counter)
}

final class ChatMessageRepositoryInMem(ref: Ref[List[ChatMessage]]) extends ChatMessageRepository {
  override val chatMessageRepository: ChatMessageRepository.Service[Any] = new ChatMessageRepository.Service[Any] {
    def add(message: ChatMessage): ZIO[Any, Nothing, Unit] =
      for {
        _ <- ref.update(message :: _)
      } yield ()

    def getAll: ZIO[Any, Nothing, List[Todo]] =
      ref.get.map(_.values.toList)

    def getById(id: TodoId): ZIO[Any, Nothing, Option[Todo]] =
      ref.get.map(_.get(id))

    def delete(id: TodoId): ZIO[Any, Nothing, Unit] =
      ref.update(_ - id).unit
  }
}
