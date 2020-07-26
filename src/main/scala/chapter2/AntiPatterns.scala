// Anti-patterns

// Seq: a base trait for sequences

trait Items[F[_]] {
  def getAll: F[Seq[Item]] // be more specific instead
}

class Program[F[_]](items: Items[F[_]]) {

  def calcTotalPrice: F[BigDecimal] =
    items.getAll.map { seq =>
      seq.toList // BUT how do we know if safe? What if this is a Stream/LazyList and possibly infinite??
        .map(_.price)
        .foldLeft(0)((acc, p) => acc + p)
    }
}

// About monad transformers

import java.util.UUID
import cats.data.OptionT

trait Users[F[_]] {
  def findUser(id: UUID): OptionT[F, User] // generally undesirable
}

trait Users[F[_]] {
  def findUser(id: UUID): F[Option[User]] // preferred to leave at abstract F
}