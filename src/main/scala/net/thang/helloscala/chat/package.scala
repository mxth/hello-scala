package net.thang.helloscala

import zio.RIO
import zio.clock.Clock
import zio.console.Console

package object chat {
  type ChatEnvironment = Console with Clock with ChatRepository

  type ChatTask[A] = RIO[ChatEnvironment, A]
}
