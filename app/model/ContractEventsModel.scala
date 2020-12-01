package model

import java.util.Date

import play.api.libs.json.{Json, OFormat}

object ContractEventsModel {

  final case class ContractCashFlows(data: Map[String, Double])
  
  object ContractCashFlows {
    import play.api.data.format.Formats.dateFormat
    implicit val cfFormat: OFormat[ContractCashFlows] = Json.format[ContractCashFlows]
  }

}
