package net.thang.helloscala

import zio.ZIO

package object repository extends TodoRepository.Service[TodoRepository] {
  override def create(todoCreateData: TodoCreateData): ZIO[TodoRepository, Nothing, Todo] =
    ZIO.accessM(_.todoRepository.create(todoCreateData))

  override def getById(id: TodoId): ZIO[TodoRepository, Nothing, Option[Todo]] =
    ZIO.accessM(_.todoRepository.getById(id))

  override def getAll: ZIO[TodoRepository, Nothing, List[Todo]] =
    ZIO.accessM(_.todoRepository.getAll)

  override def delete(id: TodoId): ZIO[TodoRepository, Nothing, Unit] =
    ZIO.accessM(_.todoRepository.delete(id))
}
