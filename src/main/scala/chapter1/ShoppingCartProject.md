# Shopping Cart project

### Business Requirements

- A guest user should be able to:
  - register into the system with a unique username and password. 
  - login into the system given some valid credentials.
  - see all the guitar catalog as well as to search per brand.

- A registered user should be able to:
  - add products to the shopping cart.
  - remove products from the shopping cart.
  - modify the quantity of a particular product in the shopping cart. 
  - check out the shopping cart, which involves:
    - sending the user Id and cart to an external payment system (see below). 
    - persisting order details including the Payment Id.
  - list existing orders as well as retrieving a specific one by Id. 
  - log out of the system.
    
- An admin user should be able to:
  - add brands.
  - add categories.
  - add products to the catalog. 
  - modify the prices of products.
    
- The frontend should be able to:
  - consume data using an HTTP API that we need to define.
    
    
#### Domain

- Item
  - uuid: a unique item identifier.
  - model: the item’s model (guitar model to start with).
  - brand: a relationship with a Brand entity.
  - category: a relationship with a Category entity.
  - description: more information about the item.
  - price: we will use USD as the currency with two decimal digits.
  
- Brand
  - name: the unique name of the brand (cannot be duplicated). Category
  - name: the name of the category (cannot be duplicated). Cart
  - uuid: a unique cart identifier.
  - items: a key-value store (Map) of item ids and quantities.
  
- Order
  - uuid: a unique order identifier.
  - paymentId: a unique payment identifier given by a 3rd party client.
  - items: a key-value store (Map) of item ids and quantities.
  - total: the total amount of the order, in USD.
  
- Card
  - name: card holder’s name.
  - number: a 16-digit number.
  - expiration: a 4-digit number as a string, to not lose zeros: the first two digits
indicate the month, and the last two, the year, e.g. “0821”.
  - cvv: a 3-digit number. CVV stands for Card Verification Value.

- Guest User (but will not be represented)

- User
  - uuid: a unique user identifier.
  - username: a unique username registered in the system.
  - password: the username’s password.
  
- Admin User
  - uuid: a unique admin user identifier.
  - username: a unique admin username.
  
  
### Architecture

- HTTP API with PostgreSQL DB and caching with Redis

### Technical stack


- [cats](https://typelevel.org/cats/): basic functional blocks. From typeclasses such as Functor to syntax and instances for some datatypes and monad transformers.
- [cats-effect](https://typelevel.org/cats-effect/): concurrency and functional effects. It ships the default IO monad.
  - ([cats-effect 3 discussion](https://github.com/typelevel/cats-effect/issues/634))
- [cats-mtl](https://typelevel.org/cats-mtl/getting-started.html): typeclasses for monad transformer capabilities.
- [cats-retry](https://github.com/cb372/cats-retry): retrying actions that can fail in a purely functional fashion.
- [circe](https://circe.github.io/circe/): standard JSON library to create encoders and decoders.
- [ciris](https://cir.is/): flexible configuration library with support for different environments.
- [fs2](https://fs2.io/guide.html): powerful streaming in constant memory and control flow.
- [http4s](https://http4s.org/): purely functional HTTP server and client, built on top of fs2.
- [http4s-jwt-auth](https://github.com/profunktor/http4s-jwt-auth): opinionated JWT authentication built on top of jwt-scala.
- [log4cats](https://christopherdavenport.github.io/log4cats/): standard logging framework for Cats.
- [meow-mtl](https://github.com/oleg-py/meow-mtl): classy optics for cats-mtl typeclasses.
- [newtype](https://github.com/estatico/scala-newtype): zero-cost wrappers for strongly typed functions.
- [redis4cats](https://github.com/profunktor/redis4cats): client for Redis compatible with cats-effect.
- [refined](https://github.com/fthomas/refined): refinement types for type-level validation.
- [skunk](https://tpolecat.github.io/skunk/): purely functional, non-blocking PostgreSQL client.
- [squants](https://github.com/typelevel/squants): strongly-typed units of measure such as “money”.