// Shared state

/* Regions of sharing */

import cats.effect._
import cats.effect.concurrent.Semaphore
import cats.effect.Console.io._
import cats.implicits._
import scala.concurrent.duration._

object SharedState extends IOApp {

  def someExpensiveTask: IO[Unit] =
    IO.sleep(1.second) >>
      putStrLn("expensive task") >>
      someExpensiveTask // TODO: come back to describe why `someExpensiveTask` is being called here

  def p1(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p1(sem)

  def p2(sem: Semaphore[IO]): IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p2(sem)

  // >> is also fa.flatMap(_ => fb) (like *>) BUT
  // independent from productR and second operand invoked call-by-name for stack safety

  def run(args: List[String]): IO[ExitCode] =
    Semaphore[IO](1).flatMap { sem => // 1 == number of permits, one semaphore created to share with p1 and p2
      p1(sem).start.void *> // *> is an alias for productR or fa.flatMap(_ => fb)
        p2(sem).start.void
    } *> IO.never.as(ExitCode.Success)
}

/* Leaky state */

// example if shared state (Semaphore) not wrapped in IO (or any other abstract effect)

object LeakySharedState extends IOApp {

  //global access
  val sem: Semaphore[IO] =
    Semaphore[IO](1).unsafeRunSync()

  def someExpensiveTask: IO[Unit] =
    IO.sleep(1.second) >>
      putStrLn("expensive task") >>
      someExpensiveTask

  new LaunchMissiles(sem).run // Unit, who knows what this is doing. Could block p1 and p2

  val p1: IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p1

  val p2: IO[Unit] =
    sem.withPermit(someExpensiveTask) >> p2

  def run(args: List[String]): IO[ExitCode] = // lost flatMap-denoted region of sharing
    p1.start.void *> p2.start.void *> // no longer control where data structure is being shared
      IO.never.as(ExitCode.Success)
}