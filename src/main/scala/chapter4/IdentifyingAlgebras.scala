// Identifying algebras

/* Brands */
// GET to retrieve a list of brands and POST to create new brands

import io.estatico.newtype.macros.newtype
import java.util.UUID

trait Brands[F[_]] { // tagless algebra
  def findAll: F[List[Brand]]
  def create(name: BrandName): F[Unit]
}

// representing the model with case classes and Newtype library
@newtype case class BrandId(value: UUID)
@newtype case class BrandName(value: String)

case class Brand(uuid: BrandId, name: BrandName) // datatype representing the business domain

/* Categories */
// GET to retrieve a list of categories and POST to create new categories

trait Categories[F[_]] {
  def findAll: F[List[Category]]
  def create(name: CategoryName): F[Unit]
}

@newtype case class CategoryId(value: UUID)
@newtype case class CategoryName(value: String)

case class Category(uuid: CategoryId, name: CategoryName)

/* Items */
// GET to retrieve a list of all items, GET to retrieve items filtered by brand, POST to create an item,
// and PUT to update an item

trait Items[F[_]] {
  def findAll: F[List[Item]]
  def findBy(brand: BrandName): F[List[Item]]
  def findById(itemId: ItemId): F[Option[Item]]
  def create(item: CreateItem): F[Unit]
  def update(item: UpdateItem): F[Unit]
}

import squants.market.Money

@newtype case class ItemId(value: UUID)
@newtype case class ItemName(value: String)
@newtype case class ItemDescription(value: String)

case class Item(
  uuid: ItemId,
  name: ItemName,
  description: ItemDescription,
  price: Money, // can convert between currencies if needed
  brand: Brand,
  category: Category
)

case class CreateItem(
  name: ItemName,
  description: ItemDescription,
  price: Money,
  brandId: BrandId,
  categoryId: CategoryId
)

case class UpdateItem(
  id: ItemId,
  price: Money
)

/* Shopping cart */
// GET to retrieve shopping cart of current user, POST to add items to cart, PUT to edit quantity of any item,
// and DELETE to remove item from cart

trait ShoppingCart[F[_]] {
  def add(
    userId: UserId,
    itemId: ItemId,
    quantity: Quantity
  ): F[Unit]
  def delete(userId: UserId): F[Unit]
  def get(userId: UserId): F[CartTotal]
  def removeItem(userId: UserId, itemId: ItemId): F[Unit]
  def update(userId: UserId, cart: Cart): F[Unit]
}

@newtype case class Quantity(value: Int)
@newtype case class Cart(items: Map[ItemId, Quantity]) // key-value store to avoid duplicates
@newtype case class CartId(value: UUID) // looks like this is never used in the book or example app

case class CartItem(item: Item, quantity: Quantity)
case class CartTotal(items: List[CartItem], total: Money)

/* Orders */
// GET to retrieve all orders for current user, and GET to retrieve a specific order for current user

trait Orders[F[_]] {
  def get(userId: UserId, orderId: OrderId): F[Option[Order]]
  def findBy(userId: UserId): F[List[Order]]
  def create(
    userId: UserId,
    paymentId: PaymentId,
    items: List[CartItem],
    total: Money
  ): F[OrderId]
}

@newtype case class OrderId(uuid: UUID)
@newtype case class PaymentId(uuid: UUID)

case class Order( // persisting in PostgreSQL
  id: OrderId,
  pid: PaymentId, // returned by the external payment system
  items: Map[ItemId, Quantity],
  total: Money // specified in US Dollars
)

/* Users */
// need to store basic info on users

trait Users[F[_]] {
  def find(username: UserName, password: Password): F[Option[User]]
  def create(username: UserName, password: Password): F[UserId]
}

/* Authentication */
// Using JSON Web Tokens (JWT) as the authentication method (covered in Chapter 5)
// WARNING: Interface subject to change in future iterations

trait Auth[F[_]] {
  def findUser(token: JwtToken): F[Option[User]]
  def newUser(username: UserName, password: Password): F[JwtToken]
  def login(username: UserName, password: Password): F[JwtToken]
  def logout(token: JwtToken, username: UserName): F[Unit]
}

// will have guest users (no auth, so not represented), common users, and admin users

@newtype case class UserId(value: UUID)
@newtype case class UserName(value: String)
@newtype case class Password(value: String) // will be encrypted
@newtype case class JwtToken(value: String) // coming back to in Chapter 5

case class User(id: UserId, name: UserName)

/* Payments */
// for external payments API

trait PaymentClient[F[_]] {
  def process(payment: Payment): F[PaymentId]
}

case class Payment(
  id: UserId,
  total: Money,
  card: Card
)
