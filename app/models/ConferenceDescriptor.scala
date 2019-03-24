package models

import java.util.Locale

import org.joda.time.{Period, DateTime, DateTimeZone}
import play.api.Play

/**
  * ConferenceDescriptor.
  * This might be the first file to look at, and to customize.
  * Idea behind this file is to try to collect all configurable parameters for a conference.
  *
  * For labels, please do customize messages and messages.fr
  *
  * Note from Nicolas : the first version of the CFP was much more "static" but hardly configurable.
  *
  * @author Frederic Camblor, BDX.IO 2014
  */


// USAGE: Used only inside this file
case class ConferenceUrls(faq: String, registration: String,confWebsite: String, cfpHostname: String){
    def cfpURL:String={
    if(Play.current.configuration.getBoolean("cfp.activateHTTPS").getOrElse(false)){
      s"https://$cfpHostname"
    }else{
      s"http://$cfpHostname"
    }
  }

}


// USAGE: used only inside this file
case class ConferenceTiming(
                             datesI18nKey: String,
                             speakersPassDuration: Integer,
                             preferredDayEnabled: Boolean,
                             firstDayFr: String,
                             firstDayEn: String,
                             datesFr: String,
                             datesEn: String,
                             cfpOpenedOn: DateTime,
                             cfpClosedOn: DateTime,
                             scheduleAnnouncedOn: DateTime,
                             days:Iterator[DateTime]
                           )
//USAGE: only inside this file
case class ConferenceSponsor(showSponsorProposalCheckbox: Boolean, sponsorProposalType: ProposalType = ProposalType.UNKNOWN)


//USAGE inside this file and returned in parse which is used by ApprovedProposal
case class ProposalConfiguration(id: String, slotsCount: Int,
                                 givesSpeakerFreeEntrance: Boolean,
                                 freeEntranceDisplayed: Boolean,
                                 htmlClass: String,
                                 hiddenInCombo: Boolean = false,
                                 chosablePreferredDay: Boolean = false,
                                 impliedSelectedTrack: Option[Track] = None)
//USAGE: used in many places
object ProposalConfiguration {

  val UNKNOWN = ProposalConfiguration(id = "unknown", slotsCount = 0, givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false,
    htmlClass = "", hiddenInCombo = true, chosablePreferredDay = false)

  def parse(propConf: String): ProposalConfiguration = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.find(p => p.id == propConf).getOrElse(ProposalConfiguration.UNKNOWN)
  }

  def totalSlotsCount = ConferenceDescriptor.ConferenceProposalConfigurations.ALL.map(_.slotsCount).sum

  def isDisplayedFreeEntranceProposals(pt: ProposalType): Boolean = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.freeEntranceDisplayed).headOption.getOrElse(false)
  }

  def getProposalsImplyingATrackSelection = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.impliedSelectedTrack.nonEmpty)
  }

  def getHTMLClassFor(pt: ProposalType): String = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.htmlClass).headOption.getOrElse("unknown")
  }

  def isChosablePreferredDaysProposals(pt: ProposalType): Boolean = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.chosablePreferredDay).headOption.getOrElse(false)
  }

  def doesProposalTypeGiveSpeakerFreeEntrance(pt: ProposalType): Boolean = {
    ConferenceDescriptor.ConferenceProposalConfigurations.ALL.filter(p => p.id == pt.id).map(_.givesSpeakerFreeEntrance).headOption.getOrElse(false)
  }
}

case class ConferenceDescriptor(eventCode: String,
                                fromEmail: String,
                                committeeEmail: String,
                                bccEmail: Option[String],
                                bugReportRecipient: String,
                                conferenceUrls: ConferenceUrls,
                                timing: ConferenceTiming,
                                hosterName: String,
                                hosterWebsite: String,
                                hashTag: String,
                                conferenceSponsor: ConferenceSponsor,
                                locale: List[Locale],
                                localisation: String,
                                maxProposalSummaryCharacters:Int=1200,
                                trackleadersEmail: String
                               )

object ConferenceDescriptor {

  /**
    * TODO configure here the kind of talks you will propose
    */
  object ConferenceProposalTypes {

    val CONF = ProposalType(id = "pbig", label = "palestradebate.label")

    val BOF = ProposalType(id = "pmeia", label = "meiapalestra.label")

    val QUICK = ProposalType(id = "pmini", label = "minipalestra.label")

    val IGNITE= ProposalType(id = "prel", label = "relampago.label")

    val ALL = List(CONF, BOF, QUICK)

    def valueOf(id: String): ProposalType = id match {
      case "pbig" => CONF
      case "pmeia" => BOF
      case "pmini" => QUICK
      //case "prel" => IGNITE
    }

  }

  // TODO Configure here the slot, with the number of slots available, if it gives a free ticket to the speaker, some CSS icons
  object ConferenceProposalConfigurations {

// ===================================== slotsCount doesn't apply to TDC
    val CONF = ProposalConfiguration(id = "pbig", slotsCount = 0, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-group",
      chosablePreferredDay = true)
    val BOF = ProposalConfiguration(id = "pmeia", slotsCount = 0, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-microphone",
      chosablePreferredDay = true)
    val QUICK = ProposalConfiguration(id = "pmini", slotsCount = 0, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-microphone",
      chosablePreferredDay = true)
    val IGNITE = ProposalConfiguration(id = "prel", slotsCount = 0, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-fast-forward",
      chosablePreferredDay = true)

    val ALL = List(CONF, BOF, QUICK )
  }

  // Configure here all your Conference's tracks.
  object ConferenceTracks {
    def ALL = Track.allTracks().sortBy(_.id)
  }

  // TODO If you want to use the Devoxx Scheduler, you can describe here the list of rooms, with capacity for seats
  object ConferenceRooms {
    val allRooms = List[Room]()
  }


  // TODO The idea here is to describe in term of Agenda, for each rooms, the slots. This is required only for the Scheduler
  object ConferenceSlots {

    // Registration, coffee break, lunch etc
    val wednesdayBreaks = List[Slot]()

    val thursdayBreaks = List[Slot]()

    val fridayBreaks = List[Slot]()

    val wednesday: List[Slot] = {
      wednesdayBreaks
    }

    val thursday: List[Slot] = {
      thursdayBreaks
    }

    val friday: List[Slot] = {
      fridayBreaks
    }

    // COMPLETE DEVOXX
    def all: List[Slot] = {
      wednesday ++ thursday ++ friday
    }
  }

  def dateRange(from: DateTime, to: DateTime, step: Period): Iterator[DateTime]      =Iterator.iterate(from)(_.plus(step)).takeWhile(!_.isAfter(to))

  val fromDay = new DateTime().withYear(2018).withMonthOfYear(12).withDayOfMonth(5)
  val toDay = new DateTime().withYear(2018).withMonthOfYear(12).withDayOfMonth(8)

  // TODO You might want to start here and configure first, your various Conference Elements
  def current() = ConferenceDescriptor(
    eventCode = "TDC2019FLP",
    fromEmail = Play.current.configuration.getString("mail.from").getOrElse("organizacao@thedevelopersconference.com.br"),
    committeeEmail = Play.current.configuration.getString("mail.committee.email").getOrElse("organizacao@thedevelopersconference.com.br"),
    bccEmail = Play.current.configuration.getString("mail.bcc"),
    bugReportRecipient = Play.current.configuration.getString("mail.bugreport.recipient").getOrElse("tdc@thedevelopersconference.com.br"),
    conferenceUrls = ConferenceUrls(
      faq = "http://cfp-poa.thedevconf.com.br/faq",
      registration = "http://thedevconf.com.br/tdc/2018/inscricoes",
      confWebsite = "http:/thedevconf.com.br",
      cfpHostname = Play.current.configuration.getString("cfp.hostname").getOrElse("cfp-poa.thedevconf.com.br")
    ),
    timing = ConferenceTiming(
      datesI18nKey = "5 a 8 de Dezembro de 2018",
      speakersPassDuration = 4,
      preferredDayEnabled = true,
      firstDayFr = "5 december",
      firstDayEn = "december 5th",
      datesFr = "du 5 au 8 december 2018",
      datesEn = "December 5th to 8th, 2018",
      cfpOpenedOn = DateTime.parse("2018-09-12T16:30:00-03:00"),
      cfpClosedOn = DateTime.parse("2018-10-01T23:59:59-03:00"),
      scheduleAnnouncedOn = DateTime.parse("2018-10-08T00:00:00-03:00"),
      days=dateRange(fromDay,toDay,new Period().withDays(1))
    ),
    hosterName = "AWS", hosterWebsite = "http://aws.amazon.com/",
    hashTag = "#TheDevConf",
    conferenceSponsor = ConferenceSponsor(showSponsorProposalCheckbox = true, sponsorProposalType = ConferenceProposalTypes.CONF)
    , locale = List(new Locale("pt","BR"))
    , localisation = "UniRitter, Porto Alegre, RS"
    , maxProposalSummaryCharacters = 700 // 1200 // French developers tends to be a bit verbose... we need extra space :-)
	  , trackleadersEmail = Play.current.configuration.getString("mail.trackleaders.email").getOrElse("coordenadores@thedevelopersconference.com.br")
  )

  // It has to be a def, not a val, else it is not re-evaluated
  def isCFPOpen: Boolean = {
    Play.current.configuration.getBoolean("cfp.isOpen").getOrElse(false)
  }

  def isGoldenTicketActive:Boolean = Play.current.configuration.getBoolean("goldenTicket.active").getOrElse(false)

  def isFavoritesSystemActive:Boolean = Play.current.configuration.getBoolean("cfp.activateFavorites").getOrElse(false)

  def isHTTPSEnabled = Play.current.configuration.getBoolean("cfp.activateHTTPS").getOrElse(false)

}