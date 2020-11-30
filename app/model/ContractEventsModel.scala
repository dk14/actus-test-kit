package model

import java.util.Date

object ContractEventsModel {

  final case class ContractCashFlows(data: Map[Date, Double])

}
