# Tagless final encoding
- AKA: finally tagless
- Method of embedding domain-specific languages (DSLs) in a typed functional host language
- An alternative to initial encoding promoted by Free Monads
- [Oleg Kiselyov's papers](http://okmij.org/ftp/tagless-final/index.html) (but diverged into a more ergonomic encoding in Scala)

### Algebras
- Describes a new language (DSL) within a host language
- So far have been calling them interfaces with a higher-kinded type
- Tagless final encoded algebra (or tagless algebra)
  - Simple interface that abstracts over effect type using a type constructor F[_]
- Algebras != typeclasses (share the same encoding in Scala)
  - Typeclasses should have coherent instances, but tagless algebras could have many implementations (interpreters)
- Perfect for encoding business concepts
- Tagless algebras should not have typeclass constraints
    - If typeclass constraint (like Monad) is needed, should be a program instead
    
##### Naming conventions
- Algebra and interpreter naming is a matter of preference, just be consistent throughout the application
  - (Author prefers `Live` prefix for interpreters) 

### Interpreters
- Normally have two interpreters per algebra: testing and real
- Help encapsulate state and allow separation of concerns
  - Interface knows nothing about implementation details
- Can be written either using a concrete datatype (IO) or polymorphic all the way (F)

##### Building interpreters
- Programs interact with the algebra (and not the interpreter)
- Encapsulate state by making constructor private and providing a smart constructor
- Used [Redis4Cats](https://redis4cats.profunktor.dev/) library in example

### Programs
- Tagless final is all about algebras and interpreters, but we need to use algebras to describe business logic.
  - This logic belongs in what the author likes to call "programs"
- Programs can make use of algebras and other programs
- (Not an official name and not in the original tagless final paper)
- When adding a typeclass constraint, remember about the principle of least power
- What is pure business logic? 
  - Determined and agreed upon by team. But possible rules, allowed to:
  - Combine pure computations in terms of tagless algebras and programs
    - Only doing what effect constraints allow
  - Perform logging (or console stuff) only via a tagless algebra
 
### Implicit vs explicit parameters
- Business logic algebras should always be passed explicitly
- Algebras resulting in an effectful action should be passed explicitly

##### Achieving modularity
- By grouping tagless algebras that share commonality in a higher-level interface
  - To avoid ending up with tons of arguments per function
  - Author calls this "modules"
- Represent with traits

##### Implicit convenience
- Some implementations are passed as implicits that are not typeclasses
  - For example, ContextShift, Clock, and Timer in Cats Effect
- Common effects do not hold business Logic
- Passing these implicitly allows different instances for testing purposes
  - This would not be possible if using typeclasses (would create orphan instances)
- Convenient and doesn't break anything
- All about common sense, consistency, and good practices