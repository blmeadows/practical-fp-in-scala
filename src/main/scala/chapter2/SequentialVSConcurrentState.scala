// Sequential vs concurrent state

/* State Monad */

import cats.data.State

val nextInt: State[Int, Int] =
  State(s => (s + 1, s * 2))

def seq: State[Int, Int] =
  for {
    n1 <- nextInt
    n2 <- nextInt
    n3 <- nextInt
  } yield n1 + n2 + n3 // state is threaded sequentially after each flatMap call and used for next call

/* Atomic Ref */

// used in previous counter