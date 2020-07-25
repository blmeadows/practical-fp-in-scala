// Strongly-typed functions examples

// starting place
def lookup(username: String, email: String): F[Option[User]]

// $ lookup("aeinstein@research.com", "aeinstein")
// $ lookup("aeinstein", "123")
// $ lookup("", "")


/* Value classes */

case class Username(val value: String) extends AnyVal
case class Email(val value: String) extends AnyVal

def lookup(username: Username, email: Email): F[Option[User]]

//$ lookup(Username("aeinstein"), Email("aeinstein@research.com"))
//$ lookup(Username("aeinstein@research.com"), Email("aeinstein"))
//$ lookup(Username("aeinstein"), Email("123"))
//$ lookup(Username(""), Email(""))

case class Username private(val value: String) extends AnyVal
case class Email private(val value: String) extends AnyVal

def mkUsername(value: String): Option[Username] =
  if (value.nonEmpty) Username(value).some
  else none[Username]

def mkEmail(value: String): Option[Email] =
  if (value.contains("@")) Email(value).some
  else none[Email]

// still have copy method
//(
//  mkUsername("aeinstein"),
//  mkEmail("aeinstein@research.com")
//  ).mapN {
//  case (username, email) =>
//    lookup(username.copy(value = ""), email)
//}

// sealed abstract classes don't have the copy method
sealed abstract class Username(value: String)
sealed abstract class Email(value: String)


/* Newtypes */

import io.estatico.newtype.macros._

@newtype case class Username(value: String)
@newtype case class Email(value: String)

// also have coerce (anti-pattern since uses asInstanceOf), but can be used when writing generic JSON codecs

import io.estatico.newtype.ops._

"foo".coerce[Email]


/* Refinement types */

import eu.timepit.refined._
import eu.timepit.refined.auto._
import eu.timepit.refined.types.string.NonEmptyString

def lookup(username: NonEmptyString): F[Option[User]]

// can make a custom refinement type instead of using built-in one

import eu.timepit.refined.api.Refined
import eu.timepit.refined.collection.Contains

type Username = String Refined Contains['g']
def lookup(username: Username): F[Option[User]]

//$ lookup("") // error
//$ lookup("aeinstein") // error
//$ lookup("csagan") // compiles

// using Refined and Newtype together

@newtype case class Brand(value: NonEmptyString)
@newtype case class Category(value: NonEmptyString)

val brand: Brand = Brand("foo")