package net.thang.helloscala.chat

final case class ChatCommand(
    userID: UserID,
    name: String,
    payload: Payload
)

sealed trait Payload

case class SendChatMessage(message: String) extends Payload
case class Foo(i: Int)                      extends Payload
case class Bar(i: Int)                      extends Payload

object ChatCommand {}
