// Algebras

// tagless final encoded algebra (tagless algebra)
trait Counter[F[_]] {
  def incr: F[Unit]
  def get: F[Int]
}

// algebra responsible for managing items
trait Items[F[_]] {
  def getAll: F[List[Item]]
  def add(item: Item): F[Unit]
}

