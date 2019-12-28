package net.thang.helloscala

import zio.RIO
import zio.clock.Clock
import zio.console.Console
import io.circe.generic.extras.Configuration

package object chat {
  type ChatEnvironment = Console with Clock with ChatRepository

  type ChatTask[A] = RIO[ChatEnvironment, A]

  implicit val genDevConfig: Configuration =
    Configuration.default.withDiscriminator("_tag")
}
