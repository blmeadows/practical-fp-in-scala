# Design patterns

### Strongly-typed functions

##### Value classes
- In vanilla Scala, we can wrap a single field and extend the AnyVal abstract class to avoid some runtime costs
- We can make the case class constructors private and give the user smart constructors to keep the order of the parameters.
- Smart constructors are functions that take a raw value and return an optional validated one (denoted by any HKT).
- Sealed abstract classes avoids the copy method.
- Using sealed abstract classes and smart constructors together will resolve issues at the cost of boilerplace and more memory allocation.
- [A value class is actually instantiated when:](https://docs.scala-lang.org/overviews/core/value-classes.html)
  - a value class is treated as another type.
  - a value class is assigned to an array.
  - doing runtime type tests, such as pattern matching.
  
##### [Newtypes](https://github.com/estatico/scala-newtype)
- Zero-cost wrappers with no runtime overhead
- Will eventually be replaced by [opaque types](https://docs.scala-lang.org/sips/opaque-types.html)
- Uses macros, which requires macro paradise compiler plugin for Scala < 2.13.0 and compiler flag -Ymacro-annotations for Scala >= 2.13.0
- Still need smart constructors to avoid invalid data

##### Refinement types
- [Refined library](https://github.com/fthomas/refined)
- Replace smart constructor (which adds boilerplate)
- Allows validating data in compile time
- Can create custom validation rules
- Can be used in conjuction with Newtype
- Integration with libraries like [Circe](https://github.com/circe/circe), [Doobie](https://github.com/tpolecat/doobie), and [Monocle](https://github.com/julien-truffaut/Monocle)
  - less need for custom refinement types
    
### Encapsulating state

- Can use MonadState, StateT, MVar, Ref
- Encapsulate state in the interpreter and only expose an abstract interface
- Interface should know nothing about state

##### In-memory counter
- Private constructor for interpreter to not lead state
- Provide a smart constructor
- Return interface wrapped in F (creation is effectful and counter is mutable state)

### Sequential vs concurrent state

##### State Monad
- Sequential state
- S => (S, A)
  - Initial state => (New state, Result of state transition(if any))

##### Atomic Ref
- Concurrent state
- Ref is a purely functional model of a concurrent mutable reference (provided by Cats Effect)
- Atomic update and modify functions allow compositionality and concurrency safety
- Uses compare-and-set (CAS) loop

### Shared State

##### Regions of sharing
- Denoted by a simple flatMap call
- One of the main reasons concurrent data structures are wrapped in F on creation
  - Ref, Deferred, Semaphore, and an HTTP Client (using http4s)
  
##### Leaky state
- Lose region of sharing and control

### Anti-patterns

##### Seq: a base trait for sequences
- Thou shalt not use Seq in your interface
- Use a more specific datatype depending on goal and desired performance characteristics
  - List, Vector, Chain, fs2.Stream ...
  
##### About monad transformers
- Thou shalt not use Monad Transformers in your interface
- Fine in local functions (like interpreters), but leave only abstract F in interface
- Kills compositionality otherwise

### Error handling
- author's biased recommendation here

##### MonadError and ApplicativeError
- Normally work in context of some parametric effect F[_]
- With Cats Effect, we can rely on MonadError / ApplicativeError
- Whether or not to include error type in signature
  - Recommendation of only when it's really necessary
  - Valid case for explicit error handling  is at HTTP layer with response codes (don't need to use Either)
  - Compromise of not stating error types at the interface level
 - Code the happy path and watch the frameworks do the right thing
 - Only worry about successful cases and business errors (let framework handle the rest)
 
 ##### Either Monad
 - What if business logic changes depending on an error?
 - Can be valid to use F[Either[E, A]]
 - ApplicativeError can be used to avoid having the error type in the interface
    - But won't get a compiler error if more error types are added to the ADT (Algebraic Data Type)
- Trade-off:
  - Either Monad - cumbersome composition (need EitherT monad transformer), compiler inference trouble (need to annotate each part)
  - ApplicativeError/MonadError - better ergonomics at cost of losing the error type
 
 ##### Classy prisms
 - Generically called *classy optics*
 - [Meow MTL](https://github.com/oleg-py/meow-mtl) library
 - Gives back typed errors and exhaustive pattern matching
    - without polluting interfaces with F[Either[E, A]] nor using monad transformers
 - Blog post [1](https://typelevel.org/blog/2018/08/25/http4s-error-handling-mtl.html) and [2](https://typelevel.org/blog/2018/11/28/http4s-error-handling-mtl-2.html) on making interface and error handler have a relationship
 - Meow MTL hierarchy import can resolve ambiguous implicit values issue
 - (coming back to classy optics later)
 