package controllers

import javax.inject._
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.mvc._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  private val userRepository = new UserRepository()
  private val userLoginsRepository = new UserLoginsRepository()

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def plainText(): Action[AnyContent] = Action { implicit request: Request[AnyContent] =>
    Ok("string")
  }

  def json(): Action[AnyContent] = Action { implicit request =>
    Ok(Json.toJson("{}"))
  }

  // /users/{user_id}/logins/count
  def getUserLogins(userId: Int): Action[AnyContent] = Action { implicit request =>
    val userLoginCount = userLoginsRepository.findLoginCountByUserId(userId)
    val userLoginCountResponse = UserLoginCount(userId = userId, loginCount = userLoginCount)
    Ok(Json.toJson(userLoginCountResponse)(Json.writes[UserLoginCount]))
  }

  def listUserLogins(userId: Int): Action[AnyContent] = Action { implicit request =>
    val userLoginList = userLoginsRepository.sequenceLoginsByUserId(userId)
    val userLoginInstancesResponse = UserLoginInstances(userId = userId, logins = userLoginList)
    Ok(Json.toJson(userLoginInstancesResponse)(Json.writes[UserLoginInstances]))
  }

}

// Response Models
case class UserLoginCount(userId: Int, loginCount: Int)
case class UserLoginInstances(userId: Int, logins: Seq[Long])

// DB Models
class User(val id: Int, val name: String)

class UserLogin(val id: Int, val userId: Int, val loggedInAt: Long)

class UserRepository {
  private def users: List[User] = List(
    new User(id = 1, name = "kenny"),
    new User(id = 2, name = "topher")
  )

  def findAll(): List[User] = users

  def find(id: Int) = users.head
}

class UserLoginsRepository {
  private def userLogins: List[UserLogin] = List(
    new UserLogin(id = 1, userId = 1, loggedInAt = 1000),
    new UserLogin(id = 2, userId = 1, loggedInAt = 2000),
    new UserLogin(id = 3, userId = 1, loggedInAt = 3000),
    new UserLogin(id = 4, userId = 1, loggedInAt = 4000),
    new UserLogin(id = 5, userId = 2, loggedInAt = 5000),
    new UserLogin(id = 6, userId = 2, loggedInAt = 6000),
  )

  def findLoginCountByUserId(userId: Int): Int = userLogins.count(userLogin => userLogin.userId == userId)

  def findLoginsByUserId(userId: Int): Seq[UserLogin] = userLogins.filter(userLogin => userLogin.userId == userId)

  def sequenceLoginsByUserId(userId: Int): Seq[Long] = {
    val userLogins = findLoginsByUserId(userId)
    var logins = ArrayBuffer[Long]()
    for (login <- userLogins) {
      logins += login.loggedInAt
    }
    logins
  }

  def sequenceLoginsByUserIdSmart(userId: Int): Seq[Long] = findLoginsByUserId(userId)
    .map(userLogin => userLogin.loggedInAt)

//  @Query("select count(*) from user_logins where userId = :userId")
//  def findLoginCountByUserId(@Param("userId") userId: Int)
//
//  def findLoginCountByUserId(@Param("userId") userId: Int) = db.count(userId => userId)


}