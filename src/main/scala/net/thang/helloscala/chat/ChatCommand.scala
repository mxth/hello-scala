package net.thang.helloscala.chat

sealed trait ChatCommand

case class SendChatMessage(message: ChatMessage) extends ChatCommand
case class Foo(i: Int)                           extends ChatCommand
case class Bar(i: Int)                           extends ChatCommand

object ChatCommand {}
