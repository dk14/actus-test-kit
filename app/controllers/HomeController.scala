package controllers

import javax.inject._
import logic.Actus
import model.ContractEventsModel.ContractCashFlows
import model.ContractTermsModel.ModelInput
import play.api._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents) extends BaseController {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  def actus() = Action { implicit request =>
    val input = Json.fromJson[ModelInput](request.body.asJson.get).fold(x => sys.error(x.toString()), identity)
    val output = Actus.runActus(input.ct, input.rf)
    Ok(Json.toJson(output))
  }
}
