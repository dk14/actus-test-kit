package model

import java.util.Date

object ContractTermsModel {
  
  final case class  Cycle(n: Int, p: Int, stub: String)
  
  case class ScheduleConfig(calendar: List[Date], includeEndDay: Boolean, eomc: String, bdc: String)
  
  final case class ContractTerms(
    contractId: String
  , contractType : Option[String]
  , ct_IED       : String 
  , ct_SD        : String 
  , ct_MD        : Option[Date] 
  , ct_TD        : Option[Date]
  , ct_PRNXT     : Option[Double]
  , ct_PRD       : Option[Date]
  , ct_CNTRL     : String
  , ct_PDIED     : Double 
  , ct_NT        : Option[Double]
  , ct_PPRD      : Option[Double]
  , ct_PTD       : Option[Double]
  , ct_DCC       : String
  , ct_PREF      : String
  , ct_PRF       : String
  , scfg         : ScheduleConfig
  , ct_PYRT      : Double
  , ct_PYTP      : String
  , ct_cPYRT     : Double
  , ct_OPCL      : Option[Cycle]
  , ct_OPANX     : Option[Date]
  , ct_SCIED     : Double
  , ct_SCEF      : String
  , ct_SCCL      : Option[Cycle]
  , ct_SCANX     : Option[Date]
  , ct_SCIXSD    : Double
  , ct_RRCL      : Option[Cycle]
  , ct_RRANX     : Option[Date]
  , ct_RRNXT     : Option[Double]
  , ct_RRSP      : Double
  , ct_RRMLT     : Double
  , ct_RRPF      : Double
  , ct_RRPC      : Double
  , ct_RRLC      : Double
  , ct_RRLF      : Double
  , ct_IPCED     : Option[Date]
  , ct_IPCL      : Option[Cycle]
  , ct_IPANX     : Option[Date]
  , ct_IPNR      : Option[Double]
  , ct_IPAC      : Option[Double]
  , ct_PRCL      : Option[Cycle]
  , ct_PRANX     : Option[Date]
  , ct_IPCB      : Option[String]
  , ct_IPCBA     : Option[Double]
  , ct_IPCBCL    : Option[Cycle]
  , ct_IPCBANX   : Option[Date]
  , ct_FECL      : Option[Cycle]
  , ct_FEANX     : Option[Date]
  , ct_FEAC      : Option[Double]
  , ct_FEB       : String
  , ct_FER       : Double
  )

}
