package model

import java.util.Date

import play.api.libs.json.{Json, OFormat}

object ContractEventsModel {

  final case class Payoff(event: String, payoff: Double)

  final case class ContractCashFlows(data: Map[String, Payoff])
  
  object ContractCashFlows {
    import play.api.data.format.Formats.dateFormat
    implicit val pfFormat: OFormat[Payoff] = Json.format[Payoff]
    implicit val cfFormat: OFormat[ContractCashFlows] = Json.format[ContractCashFlows]
  }

}
