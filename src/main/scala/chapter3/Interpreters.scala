// Interpreters

import cats.Functor
import cats.implicits._
import dev.profunktor.redis4cats.RedisCommands
import io.estatico.newtype.macros.newtype

// default interpreter using Redis

@newtype case class RedisKey(value: String)

class LiveCounter[F[_]: Functor](
  key: RedisKey,
  cmd: RedisCommands[F, String, Int]
) extends Counter[F] {

  def incr: F[Unit] = cmd.incr(key.value).void

  def get: F[Int] = cmd.get(key.value).map(_.getOrElse(0))

}

import cats.effect.concurrent.Ref

// test interpreter using in-memory data structure
class TestCounter[F[_]](
  ref: Ref[F, Int]
) extends Counter[F] {
  def incr: F[Unit] = ref.update(_ + 1)
  def get: F[Int] = ref.get
}

/* Building interpreters */
import cats.effect.Sync
import cats.effect.Resource
import cats.effect.IO

class LiveCounter[F[_]: Functor] private ( // making constructor private
  key: RedisKey,
  cmd: RedisCommands[F, String, Int]
) extends Counter[F] { ... }

object LiveCounter {
  def make[F[_]: Sync]: Resource[F, Counter[F]] = // providing smart constructor
  // since LiveCounter requires Redis connection, which is treated as a resource,
  // then need to make smart constructor a resource itself, instead of returning F[Counter[F]]
  // this is a common practice

    cmdApi.map { cmd =>
      new LiveCounter(RedisKey("myKey"), cmd)
    }

  private val cmdApi: Resource[IO, RedisCommands[IO, String, Int]] = ???
}

// usage of the smart constructor
LiveCounter.make[IO].use { counter =>
  p1(counter) *> p2(counter) *> sthElse
}

// Redis connection will only live with the Resource's `use` block
// Resource datatype guaranteed clean up of the resource (closing Redis connection) on termination, failure, or interruption
