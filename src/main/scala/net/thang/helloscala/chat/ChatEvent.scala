package net.thang.helloscala.chat

import fs2._
import fs2.concurrent._
import cats.effect.ConcurrentEffect
import zio.interop.catz._

sealed trait ChatEvent

case class ChatMessageSent(message: ChatMessage) extends ChatEvent

object ChatEvent {
  val queue: ChatTask[Queue[ChatTask, Either[Throwable, ChatEvent]]] =
    Queue.unbounded[ChatTask, Either[Throwable, ChatEvent]]

  def publish(event: ChatEvent): ChatTask[Unit] =
    queue.flatMap(q => q.enqueue1(Right(event)))

  def eventStream(implicit F: ConcurrentEffect[ChatTask]): Stream[ChatTask, ChatEvent] =
    for {
      q     <- Stream.eval(queue)
      event <- q.dequeue.rethrow
    } yield event
}
