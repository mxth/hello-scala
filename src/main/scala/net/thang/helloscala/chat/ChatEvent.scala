package net.thang.helloscala.chat

sealed trait ChatEvent
case class Initial()                             extends ChatEvent
case class ChatMessageSent(message: ChatMessage) extends ChatEvent

object ChatEvent {}
