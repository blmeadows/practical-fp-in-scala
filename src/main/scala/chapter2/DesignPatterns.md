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