package model

import java.util.Date

object ContractEventsModel {

  final case class ContractEvent(date: Date, payoff: Double)

}
