// Encapsulating state Examples

// in-memory counter

trait Counter[F[_]] { // interface with HKT F[_] representing abstract effect (usually ends up being IO)
  def incr: F[Unit]
  def get: F[Int]
}

import cats.effect.concurrent.Ref

class LiveCounter[F[_]] private(ref: Ref[F, Int]) extends Counter[F] { // interpreter with private constructor
  def incr: F[Unit] = ref.update(_ + 1)
  def get: F[Int] = ref.get
}

import cats.effect.Sync
import cats.implicits._

object LiveCounter { // smart constructor
  def make[F[_]: Sync]: F[Counter[F]] =
    Ref.of[F, Int](0).map(new LiveCounter(_))
}

// to avoid creating LiveCounter class, define as anonymous class in smart constructor (preferred - reduce boilerplate)
// can't define local variables (unless private) nor override defs as vals
object LiveCounter {
  def make[F[_]: Sync]: F[Counter[F]] =
    Ref.of[F, Int](0).map { ref =>
      new Counter[F] {
        def incr: F[Unit] = ref.update(_ + 1)
        def get: F[Int] = ref.get
      }
    }
}

// other programs interact with counter via interface
import cats.effect.IO

def program(counter: Counter[IO]): IO[Unit] =
  counter.incr *> otherStuff
