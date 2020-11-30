package logic

import java.time.{LocalDateTime, ZoneId}
import java.util
import java.util.{ArrayList, Date, Set}

import model.ContractEventsModel.ContractCashFlows
import model.ContractTermsModel.ContractTerms
import org.actus.attributes.{ContractModel, ContractModelProvider}
import org.actus.contracts.ContractType
import org.actus.externals.RiskFactorModelProvider
import org.actus.states.StateSpace

import scala.jdk.CollectionConverters._


object Actus {

  val defaultZoneId = ZoneId.systemDefault()

  def runActus(contractTerms: ContractTerms, riskFactors: Map[String, Map[Date, Double]]): ContractCashFlows = {
    def convertDate(date: LocalDateTime) = Date.from(date.toLocalDate.atStartOfDay(defaultZoneId).toInstant())
    val model: ContractModelProvider = new ContractModel(null)
    val events = ContractType.schedule(null, model)
    val riskFactors = new RiskFactorModelProvider() {
      override def keys: util.Set[String] = null

      override def stateAt(id: String,
                           time: LocalDateTime,
                           states: StateSpace,
                           attributes: ContractModelProvider): Double = riskFactors(id)(convertDate(time))
    }

    val eventsWithPayoffs = ContractType.apply(events, model, riskFactors).asScala.map(e => convertDate(e.eventTime()) -> e.payoff())
    ContractCashFlows(eventsWithPayoffs.toMap)
  }


}
