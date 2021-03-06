import com.wordnik.petstore.api._
import com.wordnik.petstore.model._

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import scala.collection.mutable.{ ListBuffer, HashMap }
import scala.collection.JavaConversions._
import scala.reflect.BeanProperty

@RunWith(classOf[JUnitRunner])
class UserApiTest extends FlatSpec with ShouldMatchers {
  behavior of "UserApi"
  val api = new UserApi
  api.apiInvoker.defaultHeaders += "api_key" -> "special-key"

  it should "fetch a user" in {
    api.getUserByName("user1") match {
      case Some(user) => {
        user.id should be(1)
        user.username should be("user1")
        user.password should be("XXXXXXXXXXX")
        user.email should be("email1@test.com")
        user.firstName should be("first name 1")
        user.lastName should be("last name 1")
        user.phone should be("123-456-7890")
        user.userStatus should be(1)
      }
      case None =>
    }
  }

  it should "authenticate a user" in {
    api.loginUser("user1", "XXXXXXXXXXX") match {
      case Some(status) => status.startsWith("logged in user session") match {
        case true => // success!
        case _ => fail("didn't get expected message " + status)
      }
      case None => fail("not able to login")
    }
  }

  it should "log out a user" in {
    api.logoutUser
  }

  it should "create a user" in {
    val user = new User
    user.id = 1002
    user.username = "johnny"
    user.password = "XXXXXXXXXXX"
    user.email = "johnny@fail.com"
    user.firstName = "Johnny"
    user.lastName = "Rocket"
    user.phone = "408-867-5309"
    user.userStatus = 1

    api.createUser(user)

    api.getUserByName("johnny") match {
      case Some(user) => {
        user.id should be (1002)
        user.username should be ("johnny")
      }
      case None =>
    }
  }

  it should "create 2 users" in {
    val userArray = (for (i <- (1 to 2)) yield {
      val user = new User
      user.id = 2000 + i
      user.username = "johnny-" + i
      user.password = "XXXXXXXXXXX"
      user.email = "johnny-" + i + "@fail.com"
      user.firstName = "Johnny"
      user.lastName = "Rocket-" + i
      user.phone = "408-867-5309"
      user.userStatus = 1
      user
    }).toArray
    api.createUsersWithArrayInput(userArray)
    
    for (i <- (1 to 2)) {
      api.getUserByName("johnny-" + i) match {
        case Some(user) => {
          user.id should be (2000 + i)
          user.email should be ("johnny-" + i + "@fail.com")
        }
        case None => fail("didn't find user " + i)
      }
    }
  }
  
  it should "create 3 users" in {
    val userList = (for (i <- (1 to 3)) yield {
      val user = new User
      user.id = 3000 + i
      user.username = "fred-" + i
      user.password = "XXXXXXXXXXX"
      user.email = "fred-" + i + "@fail.com"
      user.firstName = "Johnny"
      user.lastName = "Rocket-" + i
      user.phone = "408-867-5309"
      user.userStatus = 1
      user
    }).toList
    api.createUsersWithListInput(userList)

    for (i <- (1 to 3)) {
      api.getUserByName("fred-" + i) match {
        case Some(user) => {
          user.id should be (3000 + i)
          user.email should be ("fred-" + i + "@fail.com")
        }
        case None => fail("didn't find user " + i)
      }
    }
  }

  it should "update a user" in {
    val user = new User
    user.id = 4000
    user.username = "tony"
    user.password = "XXXXXXXXXXX"
    user.email = "tony@fail.com"
    user.firstName = "Tony"
    user.lastName = "Tiger"
    user.phone = "408-867-5309"
    user.userStatus = 1

    api.createUser(user)

    api.getUserByName("tony") match {
      case Some(user) => {
        user.id should be (4000)
        user.username should be ("tony")
      }
      case None =>
    }
    user.email = "tony@succeed.com"

    api.updateUser("tony", user)
    api.getUserByName("tony") match {
      case Some(user) => {
        user.email should be ("tony@succeed.com")
      }
      case None =>
    }
  }
}