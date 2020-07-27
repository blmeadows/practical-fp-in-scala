// Programs

import cats.Apply

class ItemsProgram[F[_]: Apply]( // Apply needed for *> (alias for productR)
  counter: Counter[F],
  items: Items[F]
) {

  def addItem(item: Item): F[Unit] =
    items.add(item) *> counter.incr

}

// program is pure business logic and holds no state and typeclass constraint of Apply (instead of in tagless algebra)
// parallelism with Parallel typeclass and concurrency with Concurrent typeclass
// Concurrent implies Async and Sync (which allow encapsulating arbitrary side-effects
// this will improve in Cats Effect 3
// could have used Applicative or Monad, but principle of least power
// could use FlatMap instead of Apply to ensure composition is sequential

import cats.effect.Console
import cats.Monad

def program[F[_]: Console: Monad]: F[Unit] = // programs can be directly encoded as functions
  for {
    _ <- Console[F].putStrLn("Enter your name: ")
    n <- Console[F].readLn
    _ <- Console[F].putStrLn(s"Hello $n!")
  } yield ()


class BiggerProgram[F[_]: Console: Monad]( // can have programs composed of other programs
  items: ItemsProgram[F],
  counter: Counter[F]
) {

  def logic(item: Item): F[Unit] =
    for {
      _ <- items.addItem(item)
      c <- counter.get
      _ <- Console[F].putStrLn(s"Number of items: $c")
    } yield ()

}

