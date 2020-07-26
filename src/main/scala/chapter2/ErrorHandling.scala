// Error handling

// MonadError and ApplicativeError

trait Categories[F[_]] { // Categories algebra / interface
  def findAll: F[List[Category]]
}

import cats.data.Kleisli

import scala.util.control.NoStackTrace

// Algebraic Data Type (ADT) of error hierarchy
sealed trait BusinessError extends NoStackTrace
case object RandomError extends BusinessError

import cats.MonadError

class LiveCategories[
  F[_]: MonadError[*[_], Throwable]: Random
] extends Categories[F] { // interpreter

  def findAll: F[List[Category]] =
    Random[F].bool.ifM(
      List.empty[Category].pure[F],
      RandomError.raiseError[F, List[Category]]
    )

}

// can specify error type in interface if needed
trait Categories[F[_]] {
  def maybeFindAll: F[Either[RandomError, List[Category]]]
}


// Either Monad

class Program[F[_]: Functor](
  categories: Categories[F]
) {

  def findAll: F[List[Category]] = // can still eliminate F[Either[E, A]] and go back to F[A] here
    categories.maybeFindAll.map {
      case Right(c) => c
      case Left(RandomError) => List.empty[Category]
    }

}

import cats.ApplicativeError
// can use ApplicativeError to avoid having the error type in the interface
type ApThrow[F[_]] = ApplicativeError[F, Throwable]

class SameProgram[F[_]: ApThrow](
  categories: Categories[F]
) {

  def findAll: F[List[Category]] =
    categories.findAll.handleError { // BUT compiler would not warn if add another error care to BusinessError ADT
      case RandomError => List.empty[Category]
    }

}


// Classy prisms (classy optics)

import org.http4s.{HttpRoutes, Response}
import org.http4s.dsl.Http4sDsl
import cats.data.OptionT
import io.circe.syntax._

// start with defining hierarchy of errors as an ADT
sealed trait UserError extends NoStackTrace
final case class UserAlreadyExists(username: String) extends UserError
final case class UserNotFound(username: String) extends UserError
final case class InvalidUserAge(age: Int) extends UserError

// next define generic interface for any error that is a subtype of Throwable (to be compatible with Cats Effect IO)
trait HttpErrorHandler[F[_], E <: Throwable] { // for http4s but can be other kinds of error handler
  def handle(routes: HttpRoutes[F]): HttpRoutes[F]
}

// next define generic handler (for convenience) that wraps logic needed in all HTTP error handlers (to avoid repetition)
abstract class RoutesHttpErrorHandler[F[_], E <: Throwable]
  extends HttpErrorHandler[F, E]
  with Http4sDsl[F] {

  def A: ApplicativeError[F, E] // specific error type
  def handler: E => F[Response[F]]
  def handle(routes: HttpRoutes[F]): HttpRoutes[F] = // generic, handles possible errors of type E
    Kleisli { req =>
      OptionT {
        A.handleErrorWith(
          routes.run(req).value
        )(e =>
          A.map(handler(e))(Option(_))
        )
      }
    }
}

// specific error handler for UserError (using generic handler)
object UserHttpErrorHandler {
  def apply[F[_]: MonadError[*[_], UserError]]: HttpErrorHandler[F, UserError] =

    new RoutesHttpErrorHandler[F, UserError] {
      val A: ApplicativeError[F, UserError] = implicitly

      val handler: UserError => F[Response[F]] = {
        case InvalidUserAge(age) => BadRequest(s"Invalid age $age".asJson)
        case UserAlreadyExists(username) => Conflict(username.asJson)
        case UserNotFound(username) => NotFound(username.asJson)
      }
    }
}

// now can use the error handler in HTTP routes
import org.http4s.dsl.impl.Root

val users: Users[F] = ???

val httpRoutes: HttpRoutes[F] =
  HttpRoutes.of {
    case GET -> Root =>
      Ok(users.findAll)
  }

// no relationship between Users[F] and HttpErrorHandler[F, UserError] but we only care about UserErrors
// any other errors throw will be handled by HTTP framework as unexpected failures
def routes(ev: HttpErrorHandler[F, UserError]): HttpRoutes[F] =
  ev.handle(httpRoutes)

// why we need classy prisms
import cats.effect.Sync

class MyRoutes[F[_]: MonadError[*[_], MyError]] { ... }

def foo[F[_]: Sync] = new MyRoutes[F] {}
// ambiguous implicit values (Sync implies MonadError[F, Throwable]
// so can't have another MonadError instance of different error type in scope

// BUT Meow MTL library can derive any other MonadError instances if
// MonadError[F, Throwable] in scope and error type is subtype of Throwable

import com.olegpy.meow.hierarchy._ // only takes this one import
