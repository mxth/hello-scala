package net.thang.helloscala

final case class TodoId(value: Long) extends AnyVal

final case class Todo(id: TodoId, content: String)
