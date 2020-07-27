// Implicit vs explicit parameters

// tagless final has been misused in Scala
def program[ // anti-pattern
  F[_]: Cache: Console: Users: Monad: Parallel: Items: EventsManager: HttpClient: KafkaClient: EventsPublisher
]: F[Unit] = ???


def program[  // pass business defined algebras explicitly
  F[_]: Cache: Console: Monad: Parallel: EventsManager: HttpClient: KafkaClient: EventsPublisher
](
  users: Users[F],
  items: Items[F]
): F[Unit] = ???


def program[  // can assume all Events algebras are business-related and pass them explicitly
  F[_]: Cache: Console: Monad: Parallel: HttpClient: KafkaClient
](
  users: Users[F],
  items: Items[F],
  eventsManager: EventsManager[F],
  eventsPublisher: EventsPublisher[F]
 ): F[Unit] = ???


def program[  // clients have a lifecycle, best managed as resources, pass explicitly since creating resource is effectful
  F[_]: Console: Monad: Parallel
](
  users: Users[F],
  items: Items[F],
  eventsManager: EventsManager[F],
  eventsPublisher: EventsPublisher[F],
  cache: Cache[F], // may be backed by Redis, for example, so potentially effectful
  kafkaClient: KafkaClient[F],
  httpClient: HttpClient[F]
 ): F[Unit] = ???


/* Achieving modularity */

package modules

trait Algebras[F[_]] {
  def users: Users[F]
  def items: Items[F]
}

trait Events[F[_]] {
  def manager: EventsManager[F]
  def publisher: EventsPublisher[F]
}

trait Clients[F[_]] {
  def kafka: KafkaClient[F]
  def http: HttpClient[F]
}

trait Database[F[_]] {
  def cache: Cache[F]
}

// build modules by using a smart constructor in the interface's companion object
object Clients {

  def make[F[_]: Concurrent]: Resource[F, Clients[F]] =
  // requiring Concurrent since might be commonly req by KafkaClient or HttpClient
  // always remember principle of least power
    (KafkaClient.make[F], HttpClient.make[F]).mapN {
      case (k, h) =>
        new Clients[F] {
          def kafka = k
          def http = h
        }
    }

}


// final version of program
def program[
  F[_]: Console: Monad: Parallel
](
  algebras: Algebras[F],
  events: Events[F],
  cache: Cache[F],
  clients: Clients[F]
): F[Unit] = ???


/* Implicit convenience */
def program[F[_]: Console: Monad: Parallel]
// only valid and lawful typeclasses are Monad and Parallel
// Console convenient to pass implicitly since is a common effect that would rarely need more than a single instance
// (can still create another instance for testing purposes)
