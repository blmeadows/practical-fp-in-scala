// Defining programs

/* Checkout */

import cats.Monad
import cats.implicits._
import cats.syntax.flatMap._
import squants.market.Money

final class CheckoutProgram[F[_]: Monad](
    paymentClient: PaymentClient[F],
    shoppingCart: ShoppingCart[F],
    orders: Orders[F]
) {

  def checkout(userId: UserId, card: Card): F[OrderId] =
    for {
      cart      <- shoppingCart.get(userId)
      paymentId <- paymentClient.process(Payment(userId, cart.total, card))
      orderId   <- orders.create(userId, paymentId, cart.items, cart.total)
      _         <- shoppingCart.delete(userId)
    } yield orderId

}

// What about error cases?

// for `cart <- shoppingCart.get(userId)`
  // if this fails, either user's cart is empty or issue communicating to database
  // not much to do and not critical, let it fail and return some error message

// for `paymentId <- paymentClient.process(Payment(userId, cart.total, card))`
  // handled by third-party HTTP API (that is idempotent)
  // worst possible scenarios (besides duplication, which is handled by the API): server crashing, network going down, etc
  // Payment Failure #1:
    // error response code (distinct from 409) from external API OR
    // request didn't complete as expected (network issues, request timeouts, etc)
    // In this case, log error and make request again, using a specific retry policy
  // Payment Failure #2:
    // in case of 409 during retry (success on external API side but fail to get a response on first try)
    // PaymentId included in 409 response, so only need to handle 409, extract PaymentId, and continue

// for `orderId <- orders.create(userId, paymentId, cart.items, cart.total)`
  // Order Failure #1:
    // database or connection failures, retry a limited amount of times
  // Order Failure #2:
    // consistent db or connection failures, can try to revert the payment and return an error
    // but if remote payment system doesn't support this:
      // can reschedule order creation to run in background and tell user payment was successful and order should be available soon
      // but what if unrecoverable? (db server on fire), can have limited retries
    // going to go with first option and inform business of choices made

// for `_ <- shoppingCart.delete(userId)`
  // not critical, but should `.attempt` the action (changing Monad constraint to MonadError[F, Throwable])
    // to make program resilient to possible failures
  // can also add explicit `.void` to discard its result (auth believes dicarding in for-comp should be rejected by
    // the compiler, but it is not


/* Retrying effects */

// retrying arbitrary effects using Cats Effect

import cats.effect._
import scala.concurrent.duration._
import cats.implicits._

def retry[A](fa: F[A]): F[A] =
  Timer[F].sleep(50.milliseconds) >> fa

// using Cats Retry library

import retry.RetryDetails
import retry.RetryDetails.{WillDelayAndRetry, GivingUp}
import io.chrisdavenport.log4cats.Logger

def logError(action: String)( // common function to log errors for both processing the payment and persisting the order
  e: Throwable,
  details: RetryDetails
): F[Unit] =
  details match {
    case r: WillDelayAndRetry =>
      Logger[F].error(s"Failed on $action. We retried${r.retriesSoFar} times.")
    case g: GivingUp =>
      Logger[F].error(s"Giving up on $action after ${g.totalRetries} retries.")
  }

import retry.RetryPolicies._

val retryPolicy = limitRetries[F](3) |+| exponentialBackoff[F](10.milliseconds)
// retry policies have a Semigroup instance that makes combining straightforward

import retry.retryingOnAllErrors

def processPayment(payment: Payment): F[PaymentId] = { // helper function that retries payments
  val action = retryingOnAllErrors[PaymentId](
    policy = retryPolicy,
    onError = logError("Payments")
  )(paymentClient.process(payment))

  action.adaptError { // adapt error (if retry function gives up) into custom PaymentError
    case e => PaymentError(Option(e.getMessage).getOrElse("Unknown"))
      // need to wrap e.getMessage in Option since it may be null (dealing with java.lang.Throwable)
  }
}

def createOrder(
  userId: UserId,
  paymentId: PaymentId,
  items: List[CartItem],
  total: Money
): F[OrderId] = { // helper function for creating and persisting orders
  val action = retryingOnAllErrors[OrderId](
    policy = retryPolicy,
    onError = logError("Order")
  )(orders.create(userId, paymentId, items, total))

  def bgAction(fa: F[OrderId]): F[OrderId] =
    fa.adaptError {
      case e => OrderError(e.getMessage)
    }
    .onError {
      case _ => Logger[F].error(s"Failed to create order for ${paymentId}")
                  *> Background[F].schedule(bgAction(fa), 1.hour)
    }

  bgAction(action)
}

trait Background[F[_]] { // new effect's interface
  def schedule[A](
    fa: F[A],
    duration: FiniteDuration
  ): F[Unit]
}

// could have directly used Concurrent and Timer (which is what this default impl does)
// few reasons why having custom interface is better:
  // gain more control by restricting what final user can do
  // avoid having Concurrent as a constraint (which allows arbitrary side-effects
  // achieve better testability (chapter 7)

// default Background instance

import cats.effect.syntax.all._

implicit def concurrentBackground[
  F[_]: Concurrent: Timer
]: Background[F] =
  new Background[F] { // simplest implementation with desired semantics
    def schedule[A](fa: F[A], duration: FiniteDuration): F[Unit] =
      (Timer[F].sleep(duration) *> fa).start.void
  }

// could have used a Queue or used native background method provided by Cats Effect (safer alternative to `start`)
// but more than acceptable if its trade-offs are understood

// final checkout implementation

import cats.MonadError
import retry.RetryPolicy

type MonadThrow[F[_]] = MonadError[F, Throwable]

final class CheckoutProgramFinal[
  F[_]: Background: Logger: MonadThrow: Timer
](
    paymentClient: PaymentClient[F],
    shoppingCart: ShoppingCart[F],
    orders: Orders[F],
    retryPolicy: RetryPolicy[F]
) {

  def checkout(userId: UserId, card: Card): F[OrderId] =
    shoppingCart.get(userId)
      .ensure(EmptyCartError)(_.items.nonEmpty)
      .flatMap {
        case CartTotal(items, total) =>
          for {
            pid   <- processPayment(Payment(userId, total, card))
            order <- createOrder(userId, pid, items, total)
            _     <- shoppingCart.delete(userId).attempt.void
          } yield order
      }

}
