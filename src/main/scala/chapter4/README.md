# Business logic
- A common way to get started is to decompose business requirements into small, self-contained algebras

### Identifying algebras
- List of the secured, admin, and open HTTP endpoints:
  - GET /brands
  - POST /brands
  - GET /categories
  - POST /categories
  - GET /items
  - GET /items?brand=gibson • POST /items
  - PUT /items
  - GET /cart
  - POST /cart
  - PUT /cart
  - DELETE /cart/{itemId}
  - GET /orders
  - GET /orders/{orderId}
  - POST /checkout
  - POST /auth/users
  - POST /auth/login
  - POST /auth/logout
- Identify common functionality between these and create a tagless algebra
- Permission details are not considered at the algebra level
- A good practice is to define a tagless algebra for remote clients

### Data access and storage
- What kind of state is needed for each algebra?
- What kind of storage to use?
- Persisting Brands, Categories, Items, Orders, and Users in PostgreSQL.
- Store Shopping Cart and authentication tokens in Redis for fast access.

### Defining programs
- Principle role of programs is to describe business login operations as a kind of DSL, without needing to know about implementation details

##### Checkout
- Simplest implementation of a program:
  - sequence of actions denoted as a for-comprehension (syntactic sugar for sequence of flatMap calls and a final map call)
- Consider failure cases and communicate back to the business

##### Retrying effects
- Can retry arbitrary effects using Cats Effect
- Complex retrying functions can be built the same way or can use a library
- [Cats Retry](https://github.com/cb372/cats-retry) offers different retry policies, powerful combinators, and a nice DSL
- Can manage effects in a purely functional way in Scala by composing retry policies using standard typeclasses and sequencing actions using monadic combinators