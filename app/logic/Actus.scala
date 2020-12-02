package logic

import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, ZoneId}
import java.util
import java.util.{ArrayList, Date, Set => JavaSet}

import model.ContractEventsModel.{ContractCashFlows, Payoff}
import model.ContractTermsModel.{ContractTerms, Cycle}
import org.actus.attributes.{ContractModel, ContractModelProvider}
import org.actus.contracts.ContractType
import org.actus.externals.RiskFactorModelProvider
import org.actus.states.StateSpace
import org.actus.types._

import scala.jdk.CollectionConverters._


object Actus {

  val defaultZoneId = ZoneId.systemDefault()
  val outputFormat = new SimpleDateFormat("dd/MM/yyyy")

  private def render(dt: Date) =
    DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(dt.toInstant.atZone(defaultZoneId))
  private def render(cl: Cycle) = "P" + cl.n + adjustHaskellEnum(cl.p) + (if (cl.stub == "ShortStub") "0" else "1")
  private def render(v: Double) = v.toString

  private def adjustHaskellEnum(haskell: String) = haskell.replaceAll("^.*?_", "")

  private def adjustDayCount(haskell: String) = haskell match {
    case "DCC_A_AISDA" => "AA"
    case "DCC_A_360"   => "A360"
    case "DCC_A_365"   => "A365"
    case "DCC_E30_360ISDA" => "30E360ISDA"
    case "DCC_E30_360" => "30E360"
    case "DCC_B_252" => "B252"
  }

  def extractAttributes(ct: ContractTerms): Map[String, String] = Map( //Map[String, AnyRef] for SWAPS, Map[String, String] for others
    "contractType" -> ct.contractType.get,
    "contractID" -> "0",
    "statusDate" -> render(ct.ct_SD),
    "contractRole" -> adjustHaskellEnum(ct.ct_CNTRL),
    "counterpartyID" -> "0",
    "cycleAnchorDateOfFee" -> ct.ct_FEANX.map(render).orNull,
    "cycleOfFee" -> ct.ct_FECL.map(render).orNull,
    "feeBasis" -> adjustHaskellEnum(ct.ct_FEB),
    "feeRate" -> render(ct.ct_FER),
    "feeAccrued" -> ct.ct_FEAC.map(render).orNull,
    "cycleAnchorDateOfInterestPayment" -> ct.ct_IPANX.map(render).orNull,
    "cycleOfInterestPayment" -> ct.ct_IPCL.map(render).orNull,
    "nominalInterestRate" -> ct.ct_IPNR.map(render).orNull,
    "dayCountConvention" -> adjustDayCount(ct.ct_DCC),
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
    "scalingEffect" -> adjustHaskellEnum(ct.ct_SCEF).replaceAll("0", "O"),
    "cycleAnchorDateOfOptionality" -> ct.ct_OPANX.map(render).orNull,
    "cycleOfOptionality" -> ct.ct_OPCL.map(render).orNull,
    "penaltyType" -> adjustHaskellEnum(ct.ct_PYTP),
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
  ).view.mapValues(v => if (v == null) "NULL" else v).toMap

  def runActus(contractTerms: ContractTerms, riskFactors: Map[String, Map[String, Double]]): ContractCashFlows = {
    def convertDate(date: LocalDateTime) = Date.from(date.toLocalDate.atStartOfDay(defaultZoneId).toInstant())
    def convertToLocalDate(date: Date) = date.toInstant.atZone(defaultZoneId).toLocalDateTime
    //def parseDate(date: String) = new SimpleDateFormat("dd/MM/yyyy").parse(date)
    val attributes = extractAttributes(contractTerms).toMap[String, AnyRef]
    println(attributes)
    val model = ContractModel.parse(attributes.asJava)
    val events = ContractType.schedule(convertToLocalDate(contractTerms.ct_MD.getOrElse(contractTerms.ct_TD.get)), model)

    val riskFactorsProvider = new RiskFactorModelProvider() {
      override def keys: util.Set[String] = Set.empty[String].asJava

      override def stateAt(id: String,
                           time: LocalDateTime,
                           states: StateSpace,
                           attributes: ContractModelProvider): Double = riskFactors(id)(render(convertDate(time)))
    }

    val eventsWithPayoffs = ContractType
      .apply(events, model, riskFactorsProvider)
      .asScala.map(e => render(convertDate(e.eventTime())) -> Payoff(e.eventType().toString, e.payoff()))
    ContractCashFlows(eventsWithPayoffs.toMap)
  }


}
