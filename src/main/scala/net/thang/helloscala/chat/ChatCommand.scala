package net.thang.helloscala.chat

import io.circe.{Decoder, Encoder}

sealed trait ChatCommand

case class SendChatMessage(message: ChatMessage) extends ChatCommand

object ChatCommand {
  import io.circe.shapes
  import shapeless.{Coproduct, Generic}

  implicit def encodeAdtNoDiscr[A, Repr <: Coproduct](
      implicit
      gen: Generic.Aux[A, Repr],
      encodeRepr: Encoder[Repr]
  ): Encoder[A] = encodeRepr.contramap(gen.to)

  implicit def decodeAdtNoDiscr[A, Repr <: Coproduct](
      implicit
      gen: Generic.Aux[A, Repr],
      decodeRepr: Decoder[Repr]
  ): Decoder[A] = decodeRepr.map(gen.from)

  def handle(command: ChatCommand): ChatTask[Unit] = command match {
    case SendChatMessage(message) => ChatEvent.publish(ChatMessageSent(message))
  }
}
