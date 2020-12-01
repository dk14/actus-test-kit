package logic

import java.time.{LocalDateTime, ZoneId}
import java.util
import java.util.{ArrayList, Date, Set}

import model.ContractEventsModel.ContractCashFlows
import model.ContractTermsModel.{ContractTerms, Cycle}
import org.actus.attributes.{ContractModel, ContractModelProvider}
import org.actus.contracts.ContractType
import org.actus.externals.RiskFactorModelProvider
import org.actus.states.StateSpace
import org.actus.types._

import scala.jdk.CollectionConverters._


object Actus {

  val defaultZoneId = ZoneId.systemDefault()

  def render(dt: Date) = dt.formatted("dd/MM/yyyy")
  def render(cl: Cycle) = "1Y"
  def render(v: Double) = v.toString

  def extractAttributes(ct: ContractTerms): Map[String, String] = Map( //Map[String, AnyRef] for SWAPS, Map[String, String] for others
    "contractType" -> ct.contractType.get,
    "contractID" -> "0",
    "statusDate" -> render(ct.ct_SD),
    "contractRole" -> ct.ct_CNTRL,
    "CounterpartyID" -> "0",
    "CycleAnchorDateOfFee" -> ct.ct_FEANX.map(render).orNull,
    "cycleOfFee" -> ct.ct_FECL.map(render).orNull,
    "feeBasis" -> ct.ct_FEB,
    "feeRate" -> render(ct.ct_FER),
    "feeAccrued" -> ct.ct_FEAC.map(render).orNull,
    "cycleAnchorDateOfInterestPayment" -> ct.ct_IPANX.map(render).orNull,
    "cycleOfInterestPayment" -> ct.ct_IPCL.map(render).orNull,
    "nominalInterestRate" -> ct.ct_IPNR.map(render).orNull,
    "dayCountConvention" -> ct.ct_DCC,
    "accruedInterest" -> ct.ct_IPAC.map(render).orNull,
    "capitalizationEndDate" -> ct.ct_IPCED.map(render).orNull,
    "cyclePointOfInterestPayment" -> "B", //TODO IPPNT
    "currency" -> "ADA",
    "initialExchangeDate" -> render(ct.ct_IED),
    "premiumDiscountAtIED" -> render(ct.ct_PDIED),
    "notionalPrincipal" -> ct.ct_NT.map(render).orNull,
    "purchaseDate" -> ct.ct_PRD.map(render).orNull,
    "priceAtPurchaseDate" -> ct.ct_PPRD.map(render).orNull,
    "terminationDate" -> ct.ct_TD.map(render).orNull,
    "priceAtTerminationDate" -> null, //TODO PTD
    "marketObjectCodeOfScalingIndex" -> "SCALE1",
    "scalingIndexAtContractDealDate" -> render(ct.ct_SCIED),
    "notionalScalingMultiplier" -> null,
    "interestScalingMultiplier" -> null,
    "cycleAnchorDateOfScalingIndex" -> ct.ct_SCANX.map(render).orNull,
    "cycleOfScalingIndex" -> ct.ct_SCCL.map(render).orNull,
    "scalingEffect" -> ct.ct_SCEF,
    "cycleAnchorDateOfOptionality" -> ct.ct_OPANX.map(render).orNull,
    "cycleOfOptionality" -> ct.ct_OPCL.map(render).orNull,
    "penaltyType" -> ct.ct_PYTP,
    "penaltyRate" -> render(ct.ct_PYRT),
    "objectCodeOfPrepaymentModel" -> "CODE007",
    "cycleAnchorDateOfRateReset" -> ct.ct_RRANX.map(render).orNull,
    "cycleOfRateReset" -> ct.ct_RRCL.map(render).orNull,
    "rateSpread" -> render(ct.ct_RRSP),
    "marketObjectCodeOfRateReset" -> "CODE007",
    "lifeCap" -> render(ct.ct_RRLC),
    "lifeFloor" -> render(ct.ct_RRLF),
    "periodCap" -> render(ct.ct_RRPC),
    "periodFloor" -> render(ct.ct_RRPF),
    "cyclePointOfRateReset" -> null, //TODO RRPNT
    "fixingPeriod" -> null,
    "nextResetRate" -> ct.ct_RRNXT.map(render).orNull,
    "rateMultiplier" -> render(ct.ct_RRMLT),
    "maturityDate" -> ct.ct_MD.map(render).orNull
  )

  def runActus(contractTerms: ContractTerms, riskFactors: Map[String, Map[Date, Double]]): ContractCashFlows = {
    def convertDate(date: LocalDateTime) = Date.from(date.toLocalDate.atStartOfDay(defaultZoneId).toInstant())
    def convertToLocalDate(date: Date) = date.toInstant.atZone(defaultZoneId).toLocalDateTime
    //def parseDate(date: String) = new SimpleDateFormat("dd/MM/yyyy").parse(date)
    val attributes = extractAttributes(contractTerms).toMap[String, AnyRef]
    val model = new ContractModel(attributes.asJava)
    val events = ContractType.schedule(convertToLocalDate(contractTerms.ct_MD.getOrElse(contractTerms.ct_TD.get)), model)

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
