package net.thang.helloscala.chat

import zio.{Ref, Task, ZIO}

case class UserID(value: String)

case class ChatMessage(userID: UserID, content: String)

object ChatRepository {
  trait Service {
    def addMessage(message: ChatMessage): Task[Unit]
    def getHistory: Task[List[ChatMessage]]
  }

  def InMem: ZIO[Any, Nothing, ChatRepository] =
    for {
      ref <- Ref.make(List.empty[ChatMessage])
    } yield new ChatRepositoryInMem(ref)
}

trait ChatRepository {
  def chatRepository: ChatRepository.Service
}

object repo {
  def addMessage(message: ChatMessage): ZIO[ChatRepository, Throwable, Unit] =
    ZIO.accessM(_.chatRepository.addMessage(message))

  def getHistory: ZIO[ChatRepository, Throwable, List[ChatMessage]] =
    ZIO.accessM(_.chatRepository.getHistory)
}

final class ChatRepositoryInMem(ref: Ref[List[ChatMessage]]) extends ChatRepository {
  override def chatRepository: ChatRepository.Service = new ChatRepository.Service {
    override def addMessage(message: ChatMessage): Task[Unit] =
      ref.update(message :: _).unit

    override def getHistory: Task[List[ChatMessage]] =
      ref.get
  }
}
