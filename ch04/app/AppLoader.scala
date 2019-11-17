import play.api.ApplicationLoader.Context
import play.api._
import play.api.db.{DBComponents, HikariCPComponents}
import play.api.db.evolutions.{DynamicEvolutions, EvolutionsComponents}
import play.api.routing.Router
import router.Routes
import com.softwaremill.macwire._
import _root_.controllers._
import dao._
import play.api.mvc.DefaultControllerComponents
import scalikejdbc.config.DBs
import security._
import services._

import scala.concurrent.Future

class AppLoader extends ApplicationLoader {
  def load(context: Context) = {
    LoggerConfigurator(context.environment.classLoader).foreach { configurator =>
      configurator.configure(context.environment)
    }
    new AppComponents(context).application
  }
}

class AppComponents(context: Context) extends BuiltInComponentsFromContext(context)
  with EvolutionsComponents with DBComponents
  with HikariCPComponents with AssetsComponents {

  override lazy val controllerComponents = wire[DefaultControllerComponents]
  lazy val prefix: String = "/"
  lazy val router: Router = wire[Routes]
  lazy val maybeRouter = Option(router)

  override lazy val httpErrorHandler = wire[ProdErrorHandler]
  override lazy val httpFilters = Seq()

  lazy val mainController = wire[MainController]
  lazy val authController = wire[AuthController]
   
  
  lazy val sessionDao = wire[SessionDao]
  lazy val userDao = wire[UserDao]
  lazy val logDao = wire[LogDao]
  lazy val validationDao = wire[ValidationDao]

  lazy val userService = wire[UserService]
  lazy val authService = wire[AuthService]
  lazy val readService : ReadService = wire[ReadService]
  lazy val validationService: ValidationService = wire[ValidationService]
  lazy val tagEventProducer = wire[TagEventProducer]
  lazy val logRecordConsumer = wire[LogRecordConsumer]
  lazy val tagEventConsumer = wire[TagEventConsumer]

  lazy val userAuthAction = wire[UserAuthAction]
  lazy val userAwareAction = wire[UserAwareAction]
  lazy val tagController = wire[TagController]

  override lazy val dynamicEvolutions = new DynamicEvolutions

  applicationLifecycle.addStopHook { () =>
    DBs.closeAll()
    Future.successful(Unit)
  }

  val onStart = {
    DBs.setupAll()
    val evolutions = applicationEvolutions
    if (evolutions.upToDate) {
      readService.init()
    }
  }
}
