package net.thang.helloscala.chat

final case class ChatEvent(
    userID: UserID,
    payload: Payload
)

sealed trait Payload

case class Initial()                        extends Payload
case class ChatMessageSent(message: String) extends Payload

object ChatEvent {}
