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

case class ConferenceUrls(faq: String, registration: String,confWebsite: String, cfpHostname: String){
    def cfpURL:String={
    if(Play.current.configuration.getBoolean("cfp.activateHTTPS").getOrElse(false)){
      s"https://$cfpHostname"
    }else{
      s"http://$cfpHostname"
    }
  }

}

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

case class ConferenceSponsor(showSponsorProposalCheckbox: Boolean, sponsorProposalType: ProposalType = ProposalType.UNKNOWN)

case class TrackDesc(id: String, imgSrc: String, i18nTitleProp: String, i18nDescProp: String)

case class ProposalConfiguration(id: String, slotsCount: Int,
                                 givesSpeakerFreeEntrance: Boolean,
                                 freeEntranceDisplayed: Boolean,
                                 htmlClass: String,
                                 hiddenInCombo: Boolean = false,
                                 chosablePreferredDay: Boolean = false,
                                 impliedSelectedTrack: Option[Track] = None)

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
                                confUrlCode: String,
                                frLangEnabled: Boolean,
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
                                notifyProposalSubmitted:Boolean,
                                maxProposalSummaryCharacters:Int=1200
                               )

object ConferenceDescriptor {

  /**
    * TODO configure here the kind of talks you will propose
    */
  object ConferenceProposalTypes {

  
   // val CONF = ProposalType(id = "conf", label = "conf.label")

    val UNI = ProposalType(id = "uni", label = "uni.label")

    val TIA = ProposalType(id = "tia", label = "tia.label")

    val LAB = ProposalType(id = "lab", label = "lab.label")

   // val QUICK = ProposalType(id = "quick", label = "quick.label")

   // val BOF = ProposalType(id = "bof", label = "bof.label")

    val KEY = ProposalType(id = "key", label = "key.label")

    // val IGNITE = ProposalType(id = "ignite", label = "ignite.label")

    val OTHER = ProposalType(id = "other", label = "other.label")

 
    val CONF = ProposalType(id = "pbig", label = "palestradebate.label")

    val BOF = ProposalType(id = "pmeia", label = "meiapalestra.label")

    val QUICK = ProposalType(id = "pmini", label = "minipalestra.label")

    val IGNITE= ProposalType(id = "prel", label = "relampago.label")

    val ALL = List(CONF, BOF, QUICK, IGNITE)

    def valueOf(id: String): ProposalType = id match {

 /******************************* comentando os tipos de propostas originais do devoxx
      case "conf" => CONF
      case "uni" => UNI
      case "tia" => TIA
      case "lab" => LAB
      case "quick" => QUICK
      case "bof" => BOF
      case "key" => KEY
      case "ignite" => IGNITE
      case "other" => OTHER
*/

      case "pbig" => CONF
      case "pmeia" => BOF
      case "pmini" => QUICK
      case "prel" => IGNITE
    }

  }

  // TODO Configure here the slot, with the number of slots available, if it gives a free ticket to the speaker, some CSS icons
  object ConferenceProposalConfigurations {
 //   val CONF = ProposalConfiguration(id = "conf", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.CONF.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-microphone",
 //     chosablePreferredDay = true)
    val UNI = ProposalConfiguration(id = "uni", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.UNI.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-laptop",
      chosablePreferredDay = true)
    val TIA = ProposalConfiguration(id = "tia", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.TIA.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-legal",
      chosablePreferredDay = true)
    val LAB = ProposalConfiguration(id = "lab", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.LAB.id)), givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-beaker",
      chosablePreferredDay = true)
//    val QUICK = ProposalConfiguration(id = "quick", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.QUICK.id)), givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-fast-forward",
//      chosablePreferredDay = true)
//    val BOF = ProposalConfiguration(id = "bof", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.BOF.id)), givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-group",
//      chosablePreferredDay = false)
    val KEY = ProposalConfiguration(id = "key", slotsCount = 7, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = false, htmlClass = "icon-microphone",
      chosablePreferredDay = true)
//    val IGNITE = ProposalConfiguration(id = "ignite", slotsCount = ConferenceSlots.all.count(_.name.equals(ConferenceProposalTypes.IGNITE.id)), givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-microphone",
//      chosablePreferredDay = false)
    val OTHER = ProposalConfiguration(id = "other", slotsCount = 5, givesSpeakerFreeEntrance = false, freeEntranceDisplayed = false, htmlClass = "icon-microphone",
      hiddenInCombo = true, chosablePreferredDay = false)



    val CONF = ProposalConfiguration(id = "pbig", slotsCount = 1, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-group",
      chosablePreferredDay = true)
    val BOF = ProposalConfiguration(id = "pmeia", slotsCount = 1, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-microphone",
      chosablePreferredDay = true)
    val QUICK = ProposalConfiguration(id = "pmini", slotsCount = 1, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-microphone",
      chosablePreferredDay = true)
    val IGNITE = ProposalConfiguration(id = "prel", slotsCount = 1, givesSpeakerFreeEntrance = true, freeEntranceDisplayed = true, htmlClass = "icon-fast-forward",
      chosablePreferredDay = true)

    val ALL = List(CONF, BOF, QUICK, IGNITE )

    def doesItGivesSpeakerFreeEntrance(proposalType: ProposalType): Boolean = {
      ALL.filter(_.id == proposalType.id).exists(_.givesSpeakerFreeEntrance)
    }
  }

  // Configure here all your Conference's tracks.
  object ConferenceTracks {

    val DOTNET = Track("dotnet", "dotnet.label")
    val AGILE = Track("agile", "agile.label")
    val ANALISE = Track("analise", "analise.label")
    val ANDROID = Track("android", "android.label")
    //val ARDUINO = Track("arduino", "arduino.label")
    //val ARQUITDOTNET = Track("arquitetdotnet", "arquitetdotnet.label")
    val ARQUITEMPRES = Track("arquitetempres", "arquitetempres.label")
    val ARQUITJAVA = Track("arquitetjava", "arquitetjava.label")
    //val ARQUITPHP = Track("arquitetphp", "arquitetphp.label")

    //val BIGDATA = Track("bigdata", "bigdata.label")
    val BANCO = Track("banco", "banco.label")
    val CLOUD = Track("cloud", "cloud.label")
    //val CHATBOTS = Track("chatbots", "chatbots.label")

    //val DATASCIENCE = Track("datascience", "datascience.label")
    val DTHINKING = Track("dthinking", "dthinking.label")
    //val DEVOPS = Track("devops", "devops.label")
    val DEVTES = Track("devtest", "devtest.label")
    val DINAMICAS = Track("dinamicas", "dinamicas.label")

    val FINTECH = Track("fintech", "fintech.label")
    val GAMES = Track("games", "games.label")
    //val GOLANG = Track("golang", "golang.label")

    //val IMPRESSAO = Track("impressao", "impressao.label")
    //val INFRAAGIL = Track("infraagil", "infraagil.label")
    val IOS = Track("ios", "ios.label")
    //val IOT = Track("iot", "iot.label")

    val JAVA = Track("java", "java.label")
    //val JAVAEE = Track("javaee", "javaee.label")
    //val JAVASCRIPT = Track("javascript", "javascript.label")

    val MACHINE = Track("machine", "machine.label")
    val MANAGEMENT = Track("management", "management.label")
    val MICROSERVICES = Track("microservices", "microservices.label")
    val MOBILE = Track("mobile", "mobile.label")
    val NODEJS = Track("nodejs", "nodejs.label")

    val PHP = Track("php", "php.label")
    val PYTHON = Track("python", "python.label")
    val DELPHIC = Track("delphic", "delphic.label")
    val REALIDADE = Track("realidade", "realidade.label")
    val RUBY = Track("ruby", "ruby.label")
    val STARTUPS = Track("startups", "startups.label")

    val TESTES = Track("testes", "testes.label")
    val TRANSFDIGITAL = Track("transfdigital", "transfdigital.label")
    //val UXDESIGN = Track("uxdesign", "uxdesign.label")
    val XAMARIN = Track("xamarin", "xamarin.label")

    /*
    val PFUNCIONAL = Track("pfuncional", "pfuncional.label")
    val FRAMEWORKSJS = Track("frameworksjs", "frameworksjs.label")
    */

    val ALL = List(DOTNET, AGILE, ANALISE, ANDROID, ARQUITEMPRES, ARQUITJAVA, BANCO,
      CLOUD, DTHINKING, DEVTEST, DINAMICAS, FINTECH, GAMES, IOS,
      JAVA, MACHINE, MANAGEMENT, MICROSERVICES, MOBILE, NODEJS,
      PHP, PYTHON, DELPHIC, REALIDADE, RUBY, STARTUPS, TESTES, TRANSFDIGITAL, XAMARIN)

  }

  // TODO configure the description for each Track
  object ConferenceTracksDescription {

    val DOTNET = TrackDesc(ConferenceTracks.DOTNET.id, "/assets/tdc2016poa/images/icon_dotnet.png", "track.dotnet.title", "track.dotnet.desc")
    val AGILE = TrackDesc(ConferenceTracks.AGILE.id, "/assets/tdc2016poa/images/icon_agile.png", "track.agile.title", "track.agile.desc")
    val ANALISE = TrackDesc(ConferenceTracks.ANALISE.id, "/assets/tdc2016poa/images/icon_analise.png", "track.analise.title", "track.analise.desc")
    val ANDROID = TrackDesc(ConferenceTracks.ANDROID.id, "/assets/tdc2016poa/images/icon_android.png", "track.android.title", "track.android.desc")
    //val ARDUINO = TrackDesc(ConferenceTracks.ARDUINO.id, "/assets/tdc2016poa/images/icon_arduino.png", "track.arduino.title", "track.arduino.desc")
    //val ARQUITDOTNET = TrackDesc(ConferenceTracks.ARQUITDOTNET.id, "/assets/tdc2016poa/images/icon_arquitetura.png", "track.arquitetura.title", "track.arquitetdotnet.desc")
    val ARQUITEMPRES = TrackDesc(ConferenceTracks.ARQUITEMPRES.id, "/assets/tdc2016poa/images/icon_arquitetura.png", "track.arquitetura.title", "track.arquitetempres.desc")
    val ARQUITJAVA = TrackDesc(ConferenceTracks.ARQUITJAVA.id, "/assets/tdc2016poa/images/icon_arquitetura.png", "track.arquitetura.title", "track.arquitetjava.desc")
    //val ARQUITPHP = TrackDesc(ConferenceTracks.ARQUITPHP.id, "/assets/tdc2016poa/images/icon_arquitetura.png", "track.arquitetura.title", "track.arquitetphp.desc")

    val BANCO = TrackDesc(ConferenceTracks.BANCO.id, "/assets/tdc2016poa/images/icon_bd.png", "track.banco.title", "track.banco.desc")
    //val BIGDATA = TrackDesc(ConferenceTracks.BIGDATA.id, "/assets/tdc2016poa/images/icon_bigdata.png", "track.bigdata.title", "track.bigdata.desc")
    val CLOUD = TrackDesc(ConferenceTracks.CLOUD.id, "/assets/tdc2016poa/images/icon_cloud.png", "track.cloud.title", "track.cloud.desc")
    //val CHATBOTS = TrackDesc(ConferenceTracks.CHATBOTS.id, "/assets/tdc2016poa/images/icon_cloud.png", "track.chatbots.title", "track.chatbots.desc")

    //val DATASCIENCE = TrackDesc(ConferenceTracks.DATASCIENCE.id, "/assets/tdc2016poa/images/icon_datascience.png", "track.datascience.title", "track.datascience.desc")
    //val DEVOPS = TrackDesc(ConferenceTracks.DEVOPS.id, "/assets/tdc2016poa/images/icon_devops.png", "track.devops.title", "track.devops.desc")
    val DEVTEST = TrackDesc(ConferenceTracks.DEVTEST.id, "/assets/tdc2016poa/images/icon_devtest.png", "track.devtet.title", "track.devtet.desc")
    val DINAMICAS = TrackDesc(ConferenceTracks.DINAMICAS.id, "/assets/tdc2016poa/images/icon_dinamicas.png", "track.dinamicas.title", "track.dinamicas.desc")
    val DTHINKING = TrackDesc(ConferenceTracks.DTHINKING.id, "/assets/tdc2016poa/images/icon_dthinking.png", "track.dthinking.title", "track.dthinking.desc")

    val FINTECH = TrackDesc(ConferenceTracks.FINTECH.id, "/assets/tdc2016poa/images/icon_fintech.png", "track.fintech.title", "track.fintech.desc")
    val GAMES = TrackDesc(ConferenceTracks.GAMES.id, "/assets/tdc2016poa/images/icon_games.png", "track.games.title", "track.games.desc")
    //val GOLANG = TrackDesc(ConferenceTracks.GOLANG.id, "/assets/tdc2016poa/images/icon_education.png", "track.golang.title", "track.golang.desc")

    val JAVA = TrackDesc(ConferenceTracks.JAVA.id, "/assets/tdc2016poa/images/icon_java.png", "track.java.title", "track.java.desc")
    //val JAVAEE = TrackDesc(ConferenceTracks.JAVAEE.id, "/assets/tdc2016poa/images/icon_java.png", "track.javaee.title", "track.javaee.desc")
    //val JAVASCRIPT = TrackDesc(ConferenceTracks.JAVASCRIPT.id, "/assets/tdc2016poa/images/icon_javascript.png", "track.javascript.title", "track.javascript.desc")

    //val IMPRESSAO = TrackDesc(ConferenceTracks.IMPRESSAO.id, "/assets/tdc2016poa/images/icon_impressao3d.png", "track.impressao.title", "track.impressao.desc")
    //val INFRAAGIL = TrackDesc(ConferenceTracks.INFRAAGIL.id, "/assets/tdc2016poa/images/icon_infraagil.png", "track.infraagil.title", "track.infraagil.desc")
    val IOS = TrackDesc(ConferenceTracks.IOS.id, "/assets/tdc2016poa/images/icon_ios.png", "track.ios.title", "track.ios.desc")
    //val IOT = TrackDesc(ConferenceTracks.IOT.id, "/assets/tdc2016poa/images/icon_iot.png", "track.iot.title", "track.iot.desc")

    val MACHINE = TrackDesc(ConferenceTracks.MACHINE.id, "/assets/tdc2016poa/images/icon_management.png", "track.machine.title", "track.machine.desc")
    val MANAGEMENT = TrackDesc(ConferenceTracks.MANAGEMENT.id, "/assets/tdc2016poa/images/icon_management.png", "track.management.title", "track.management.desc")
    val MICROSERVICES = TrackDesc(ConferenceTracks.MICROSERVICES.id, "/assets/tdc2016poa/images/icon_bd.png", "track.microservices.title", "track.microservices.desc")
    val MOBILE = TrackDesc(ConferenceTracks.MOBILE.id, "/assets/tdc2016poa/images/icon_mobile.png", "track.mobile.title", "track.mobile.desc")
    val NODEJS = TrackDesc(ConferenceTracks.NODEJS.id, "/assets/tdc2016poa/images/icon_mobile.png", "track.nodejs.title", "track.nodejs.desc")

    val PHP = TrackDesc(ConferenceTracks.PHP.id, "/assets/tdc2016poa/images/icon_php.png", "track.php.title", "track.php.desc")
    val PYTHON = TrackDesc(ConferenceTracks.PYTHON.id, "/assets/tdc2016poa/images/icon_python.png", "track.python.title", "track.python.desc")
    val DELPHIC = TrackDesc(ConferenceTracks.REALIDADE.id, "/assets/tdc2016poa/images/icon_delphic.png", "track.delphic.title", "track.delphic.desc")
    val REALIDADE = TrackDesc(ConferenceTracks.REALIDADE.id, "/assets/tdc2016poa/images/icon_realidade.png", "track.realidade.title", "track.realidade.desc")
    val RUBY = TrackDesc(ConferenceTracks.RUBY.id, "/assets/tdc2016poa/images/icon_ruby.png", "track.ruby.title", "track.ruby.desc")
    val STARTUPS = TrackDesc(ConferenceTracks.STARTUPS.id, "/assets/tdc2016poa/images/icon_empreende.png", "track.startups.title", "track.startups.desc")

    val TESTES = TrackDesc(ConferenceTracks.TESTES.id, "/assets/tdc2016poa/images/icon_testes.png", "track.testes.title", "track.testes.desc")
    val TRANSFDIGITAL = TrackDesc(ConferenceTracks.TRANSFDIGITAL.id, "/assets/tdc2016poa/images/icon_testes.png", "track.transfdigital.title", "track.transfdigital.desc")

    //val UXDESIGN = TrackDesc(ConferenceTracks.UXDESIGN.id, "/assets/tdc2016poa/images/icon_uxdesign.png", "track.uxdesign.title", "track.uxdesign.desc")
    val XAMARIN = TrackDesc(ConferenceTracks.XAMARIN.id, "/assets/tdc2016poa/images/icon_pfuncional.png", "track.xamarin.title", "track.xamarin.desc")

    /*
    val PFUNCIONAL = TrackDesc(ConferenceTracks.PFUNCIONAL.id, "/assets/tdc2016poa/images/icon_pfuncional.png", "track.pfuncional.title", "track.pfuncional.desc")
    val FRAMEWORKSJS = TrackDesc(ConferenceTracks.FRAMEWORKSJS.id, "/assets/tdc2016poa/images/icon_javascript.png", "track.frameworksjs.title", "track.frameworksjs.desc")

        val BANCO = TrackDesc(ConferenceTracks.BANCO.id, "/assets/tdc2016poa/images/icon_bd.png", "track.banco.title", "track.banco.desc")
        val WEB = TrackDesc(ConferenceTracks.WEB.id, "/assets/tdc2016poa/images/icon_web.png", "track.web.title", "track.web.desc")
        val GROWTH = TrackDesc(ConferenceTracks.GROWTH.id, "/assets/tdc2016poa/images/icon_growth.png", "track.growth.title", "track.growth.desc")
        val EMPREENDE = TrackDesc(ConferenceTracks.EMPREENDE.id, "/assets/tdc2016poa/images/icon_empreende.png", "track.empreende.title", "track.empreende.desc")
    */

    val ALL = List(DOTNET, AGILE, ANALISE, ANDROID, ARQUITEMPRES, ARQUITJAVA, BANCODADOS,
      CLOUD, DTHINKING, DEVTEST, DINAMICAS, FINTECH, GAMES, IOS,
      JAVA, MACHINE, MANAGEMENT, MICROSERVICES, MOBILE, NODEJS,
      PHP, PYTHON, RADDELPHIC, REALIDADE, RUBY, STARTUPS, TESTES, TRANSFDIGITAL, XAMARIN)

    def findTrackDescFor(t: Track): TrackDesc = {
      ALL.find(_.id == t.id).getOrElse(DOTNET)
    }
  }

  // TODO If you want to use the Devoxx Scheduler, you can describe here the list of rooms, with capacity for seats
  object ConferenceRooms {

    // Tip : I use the ID to sort-by on the view per day... So if the exhibition floor id is "aaa" it will be
    // the first column on the HTML Table

    // Do not change the ID's once the program is published
    val HALL_EXPO = Room("a_hall", "Exhibition floor", 2300, "special", "")
    val HALL_A = Room("x_hall_a", "Open Data Camp", 100, "special", "")

    val AMPHI_BLEU = Room("b_amphi", "Amphi Bleu", 826, "theatre", "camera")
    val MAILLOT = Room("c_maillot", "Maillot", 380, "theatre", "camera")
    val NEUILLY_251 = Room("f_neu251", "Neuilly 251", 220, "theatre", "camera")
    val NEUILLY_252AB = Room("e_neu252", "Neuilly 252 AB", 380, "theatre", "camera")
    val NEUILLY_253 = Room("neu253", "Neuilly 253 Lab", 60, "classroom", "rien")
    val NEUILLY_253_T = Room("neu253_t", "Neuilly 253", 120, "theatre", "son")

    val PARIS_241 = Room("d_par241", "Paris 241", 220, "theatre", "camera")
    val PARIS_242AB_T = Room("par242AB", "Paris 242-AB", 280, "theatre", "camera")
    val PARIS_242A = Room("par242A", "Paris 242A Lab", 60, "classroom", "rien")
    val PARIS_242B = Room("par242B", "Paris 242B Lab", 60, "classroom", "rien")
    val PARIS_242A_T = Room("par242AT", "Paris 242A", 120, "theatre", "rien")
    val PARIS_242B_T = Room("par242BT", "Paris 242B", 120, "theatre", "rien")

    val PARIS_243 = Room("par243", "Paris 243", 60, "classroom", "rien")
    val PARIS_243_T = Room("par243_t", "Paris 243", 120, "theatre", "son")

    val PARIS_202_203 = Room("par202_203", "Paris 202-203 Lab", 32, "classroom", "rien")
    val PARIS_221M_222M = Room("par221M-222M", "Paris 221M-222M Lab", 32, "classroom", "rien")
    val PARIS_224M_225M = Room("par224M-225M", "Paris 224M-225M Lab", 26, "classroom", "rien")

    val NEUILLY_212_213 = Room("neu_212_213", "Neuilly 212-213M Lab", 32, "classroom", "rien")
    val NEUILLY_231_232 = Room("neu_232_232", "Neuilly 231-232M Lab", 32, "classroom", "rien")
    val NEUILLY_234_235 = Room("neu_234_235", "Neuilly 234_234M Lab", 32, "classroom", "rien")

    val PARIS_204 = Room("par204", "Paris 204", 16, "classroom", "rien")
    val PARIS_201 = Room("par201", "Paris 201", 14, "classroom", "rien")

    val ROOM_OTHER = Room("other_room", "Autres salles", 100, "classroom", "rien")

    val allRooms = List(HALL_EXPO, HALL_A, AMPHI_BLEU, MAILLOT, PARIS_241, NEUILLY_251, NEUILLY_252AB,
      PARIS_242A, PARIS_242B, PARIS_243, PARIS_243_T, NEUILLY_253, NEUILLY_253_T,
      PARIS_202_203, PARIS_221M_222M, PARIS_224M_225M, PARIS_204, PARIS_201)

    val allRoomsUni = List(AMPHI_BLEU, MAILLOT, NEUILLY_251, PARIS_241,NEUILLY_252AB)

    val allRoomsTIAWed = allRoomsUni ++ List(PARIS_242A_T, PARIS_242B_T, PARIS_243, NEUILLY_253)
    val allRoomsTIAThu = allRoomsUni ++ List(PARIS_242AB_T, PARIS_243_T, NEUILLY_253_T)

    val allRoomsLabsWednesday = List(PARIS_242A, PARIS_242B, PARIS_243, NEUILLY_253) ++ List(PARIS_202_203, PARIS_221M_222M, PARIS_224M_225M,  NEUILLY_231_232, NEUILLY_234_235)
    val allRoomsLabThursday = List(PARIS_202_203, PARIS_221M_222M, PARIS_224M_225M)
    val allRoomsLabFriday = List(PARIS_202_203, PARIS_221M_222M, PARIS_224M_225M, NEUILLY_212_213, NEUILLY_231_232, NEUILLY_234_235 )

    val allRoomsBOF = List(PARIS_242AB_T, NEUILLY_252AB, PARIS_241, NEUILLY_251, PARIS_243_T, NEUILLY_253_T, PARIS_202_203, PARIS_221M_222M, PARIS_224M_225M)

    val keynoteRoom = List(AMPHI_BLEU)

    val allRoomsConf = List(AMPHI_BLEU, MAILLOT, PARIS_242AB_T, NEUILLY_252AB, PARIS_241, NEUILLY_251, PARIS_243_T, NEUILLY_253_T)

    val allRoomsQuickiesThu = allRoomsConf.filterNot(r => r.id == AMPHI_BLEU.id || r.id == MAILLOT.id) ++ List(PARIS_202_203) // Retire les Ignites
    val allRoomsQuickiesFriday = allRoomsConf.filterNot(r => r.id == AMPHI_BLEU.id || r.id == MAILLOT.id)
  }


  // TODO if you want to use the Scheduler, you can configure the breaks
  object ConferenceSlotBreaks {
    val registration = SlotBreak("reg", "Registration, Welcome and Breakfast", "Accueil", ConferenceRooms.HALL_EXPO)
    val petitDej = SlotBreak("dej", "Breakfast", "Accueil et petit-déjeuner", ConferenceRooms.HALL_EXPO)
    val coffee = SlotBreak("coffee", "Coffee Break", "Pause café", ConferenceRooms.HALL_EXPO)
    val lunch = SlotBreak("lunch", "Lunch", "Pause déjeuner", ConferenceRooms.HALL_EXPO)
    val shortBreak = SlotBreak("chgt", "Break", "Pause courte", ConferenceRooms.HALL_EXPO)
    val exhibition = SlotBreak("exhib", "Exhibition", "Exhibition", ConferenceRooms.HALL_EXPO)
    val meetAndGreet = SlotBreak("meet", "Meet & Greet", "Exhibition", ConferenceRooms.HALL_EXPO)
  }

  // TODO The idea here is to describe in term of Agenda, for each rooms, the slots. This is required only for the Scheduler
  object ConferenceSlots {

    // UNIVERSITY
    val universitySlotsWednesday: List[Slot] = {
      val universityWednesdayMorning = ConferenceRooms.allRoomsUni.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.UNI.id, "wednesday",
            new DateTime("2016-04-20T09:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-20T12:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val universityWednesdayAfternoon = ConferenceRooms.allRoomsUni.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.UNI.id, "wednesday",
            new DateTime("2016-04-20T13:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-20T16:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      universityWednesdayMorning ++ universityWednesdayAfternoon
    }

    // TOOLS IN ACTION
    val tiaSlotsWednesday: List[Slot] = {

      val toolsWednesdayAfternoonSlot1 = ConferenceRooms.allRoomsTIAWed.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "wednesday",
            new DateTime("2016-04-20T17:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-20T17:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val toolsWednesdayAfternoonSlot2 = ConferenceRooms.allRoomsTIAWed.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "wednesday",
            new DateTime("2016-04-20T17:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-20T18:25:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      val toolsWednesdayAfternoonSlot3 = ConferenceRooms.allRoomsTIAWed.map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "wednesday",
            new DateTime("2016-04-20T18:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-20T19:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r3)
      }
      toolsWednesdayAfternoonSlot1 ++ toolsWednesdayAfternoonSlot2 ++ toolsWednesdayAfternoonSlot3
    }

    val tiaSlotsThursday: List[Slot] = {
      val toolsThursdayAfternoonSlot1 = ConferenceRooms.allRoomsTIAThu.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "thursday",
            new DateTime("2016-04-21T18:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T18:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val toolsThursdayAfternoonSlot2 = ConferenceRooms.allRoomsTIAThu.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.TIA.id, "thursday",
            new DateTime("2016-04-21T18:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T19:25:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      toolsThursdayAfternoonSlot1 ++ toolsThursdayAfternoonSlot2
    }

    // HANDS ON LABS
    val labsSlotsWednesday: List[Slot] = {
      val labsWednesdayMorning = ConferenceRooms.allRoomsLabsWednesday.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "wednesday",
            new DateTime("2016-04-20T09:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-20T12:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val labsWednesdayAfternoon = ConferenceRooms.allRoomsLabsWednesday.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "wednesday",
            new DateTime("2016-04-20T13:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-20T16:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      labsWednesdayMorning ++ labsWednesdayAfternoon
    }

    val labsSlotsThursday: List[Slot] = {

      val labsThursdayMorning = ConferenceRooms.allRoomsLabThursday.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "thursday",
            new DateTime("2016-04-21T12:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T15:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val labsThursdayAfternoon = ConferenceRooms.allRoomsLabThursday.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "thursday",
            new DateTime("2016-04-21T16:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T19:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      labsThursdayMorning ++ labsThursdayAfternoon
    }

    val labsSlotsFriday: List[Slot] = {

      val labsFridayMorning = ConferenceRooms.allRoomsLabFriday.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.LAB.id, "friday",
            new DateTime("2016-04-22T11:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T13:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      labsFridayMorning
    }

    // BOFS
    val bofSlotsThursday: List[Slot] = {

      val bofThursdayEveningSlot1 = ConferenceRooms.allRoomsBOF.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.BOF.id, "thursday",
            new DateTime("2016-04-21T19:45:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T20:35:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val bofThursdayEveningSlot2 = ConferenceRooms.allRoomsBOF.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.BOF.id, "thursday",
            new DateTime("2016-04-21T20:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T21:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      val bofThursdayEveningSlot3 = ConferenceRooms.allRoomsBOF.map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.BOF.id, "thursday",
            new DateTime("2016-04-21T21:35:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T22:25:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r3)
      }
      bofThursdayEveningSlot1 ++ bofThursdayEveningSlot2 ++ bofThursdayEveningSlot3
    }

    // QUICKIES
    val quickiesSlotsThursday: List[Slot] = {
      val quickiesThursdayLunch1 = ConferenceRooms.allRoomsQuickiesThu.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.QUICK.id, "thursday",
            new DateTime("2016-04-21T12:05:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T12:20:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val quickiesThursdayLunch2 = ConferenceRooms.allRoomsQuickiesThu.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.QUICK.id, "thursday",
            new DateTime("2016-04-21T12:25:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T12:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      quickiesThursdayLunch1 ++ quickiesThursdayLunch2
    }

    val quickiesSlotsFriday: List[Slot] = {
      val quickFriday1 = ConferenceRooms.allRoomsQuickiesFriday.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.QUICK.id, "friday",
            new DateTime("2016-04-22T12:05:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T12:20:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val quickFriday2 = ConferenceRooms.allRoomsQuickiesFriday.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.QUICK.id, "friday",
            new DateTime("2016-04-22T12:25:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T12:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      quickFriday1 ++ quickFriday2
    }

    // CONFERENCE KEYNOTES
    val keynoteSlotsThursday: List[Slot] = {
     val keynoteThursdayWelcome = ConferenceRooms.keynoteRoom.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "thursday",
            new DateTime("2016-04-21T09:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T09:15:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val keynoteThursdaySlot1 = ConferenceRooms.keynoteRoom.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "thursday",
            new DateTime("2016-04-21T09:20:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T09:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val keynoteThursdaySlot2 = ConferenceRooms.keynoteRoom.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "thursday",
            new DateTime("2016-04-21T09:45:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T10:05:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      val keynoteThursdaySlot3 = ConferenceRooms.keynoteRoom.map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "thursday",
            new DateTime("2016-04-21T10:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T10:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r3)
      }

      keynoteThursdayWelcome ++keynoteThursdaySlot1 ++ keynoteThursdaySlot2 ++ keynoteThursdaySlot3
    }

    val keynoteSlotsFriday: List[Slot] = {
      val keynoteFridaySlot1 = ConferenceRooms.keynoteRoom.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "friday",
            new DateTime("2016-04-22T09:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T09:20:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val keynoteFridaySlot2 = ConferenceRooms.keynoteRoom.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "friday",
            new DateTime("2016-04-22T09:25:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T09:45:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      val keynoteFridaySlot3 = ConferenceRooms.keynoteRoom.map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "friday",
            new DateTime("2016-04-22T09:50:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T10:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r3)
      }
      val keynoteFridaySlot4 = ConferenceRooms.keynoteRoom.map {
        r4 =>
          SlotBuilder(ConferenceProposalTypes.KEY.id, "friday",
            new DateTime("2016-04-22T10:15:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T10:35:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r4)
      }

      keynoteFridaySlot1 ++ keynoteFridaySlot2 ++ keynoteFridaySlot3 ++ keynoteFridaySlot4

    }

    // CONFERENCE SLOTS
    val conferenceSlotsThursday: List[Slot] = {

      val conferenceThursdaySlot1 = ConferenceRooms.allRoomsConf.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-04-21T11:15:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T12:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val conferenceThursdaySlot2 = ConferenceRooms.allRoomsConf.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-04-21T12:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T13:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      val conferenceThursdaySlot3 = ConferenceRooms.allRoomsConf.map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-04-21T13:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T14:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r3)
      }
      val conferenceThursdaySlot4 = ConferenceRooms.allRoomsConf.map {
        r4 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-04-21T14:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T15:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r4)
      }
      val conferenceThursdaySlot5 = ConferenceRooms.allRoomsConf.map {
        r5 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-04-21T16:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T16:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r5)
      }
      val conferenceThursdaySlot6 = ConferenceRooms.allRoomsConf.map {
        r6 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "thursday",
            new DateTime("2016-04-21T17:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-21T17:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r6)
      }
      conferenceThursdaySlot1 ++ conferenceThursdaySlot2 ++ conferenceThursdaySlot3 ++ conferenceThursdaySlot4 ++ conferenceThursdaySlot5 ++ conferenceThursdaySlot6
    }

    val conferenceSlotsFriday: List[Slot] = {

      val conferenceFridaySlot1 = ConferenceRooms.allRoomsConf.map {
        r1 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-04-22T11:15:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T12:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r1)
      }
      val conferenceFridaySlot2 = ConferenceRooms.allRoomsConf.map {
        r2 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-04-22T12:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T13:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r2)
      }
      val conferenceFridaySlot3 = ConferenceRooms.allRoomsConf.map {
        r3 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-04-22T13:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T14:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r3)
      }
      val conferenceFridaySlot4 = ConferenceRooms.allRoomsConf.map {
        r4 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-04-22T14:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T15:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r4)
      }
      val conferenceFridaySlot5 = ConferenceRooms.allRoomsConf.map {
        r5 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-04-22T16:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T16:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r5)
      }
      val conferenceFridaySlot5extra = ConferenceRooms.allRoomsConf.map {
        r5 =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-04-22T17:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T17:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), r5)
      }

      // Cast codeur
      val conferenceFridaySlot6 = List(ConferenceRooms.NEUILLY_252AB).map {
        rcc =>
          SlotBuilder(ConferenceProposalTypes.CONF.id, "friday",
            new DateTime("2016-04-22T18:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
            new DateTime("2016-04-22T18:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")), rcc)
      }

      conferenceFridaySlot1 ++ conferenceFridaySlot2 ++ conferenceFridaySlot3 ++ conferenceFridaySlot4 ++ conferenceFridaySlot5 ++ conferenceFridaySlot5extra ++ conferenceFridaySlot6
    }

    // Registration, coffee break, lunch etc
    val wednesdayBreaks = List(
      SlotBuilder(ConferenceSlotBreaks.registration, "wednesday",
        new DateTime("2016-04-20T08:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-20T09:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")))
      , SlotBuilder(ConferenceSlotBreaks.lunch, "wednesday",
        new DateTime("2016-04-20T12:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-20T13:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")))
      , SlotBuilder(ConferenceSlotBreaks.coffee, "wednesday",
        new DateTime("2016-04-20T16:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-20T17:10:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")))
    )

    val thursdayBreaks = List(
      SlotBuilder(ConferenceSlotBreaks.registration, "thursday",
        new DateTime("2016-04-21T07:30:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-21T09:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")))
      , SlotBuilder(ConferenceSlotBreaks.shortBreak, "thursday",
        new DateTime("2016-04-21T10:45:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-21T11:15:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")))
      , SlotBuilder(ConferenceSlotBreaks.lunch, "thursday",
        new DateTime("2016-04-21T12:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-21T12:55:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")))
      , SlotBuilder(ConferenceSlotBreaks.coffee, "thursday",
        new DateTime("2016-04-21T15:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-21T16:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")))
    )

    val fridayBreaks = List(
      SlotBuilder(ConferenceSlotBreaks.petitDej, "friday",
        new DateTime("2016-04-22T08:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-22T09:00:00.000+02:00"))
      , SlotBuilder(ConferenceSlotBreaks.coffee, "friday",
        new DateTime("2016-04-22T10:45:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-22T11:15:00.000+02:00"))
      , SlotBuilder(ConferenceSlotBreaks.lunch, "friday",
        new DateTime("2016-04-22T12:00:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-22T12:55:00.000+02:00"))
      , SlotBuilder(ConferenceSlotBreaks.coffee, "friday",
        new DateTime("2016-04-22T15:40:00.000+02:00").toDateTime(DateTimeZone.forID("America/Sao_Paulo")),
        new DateTime("2016-04-22T16:10:00.000+02:00"))
    )

    val wednesday: List[Slot] = {
      wednesdayBreaks ++ universitySlotsWednesday ++ tiaSlotsWednesday ++ labsSlotsWednesday
    }

    val thursday: List[Slot] = {
      thursdayBreaks ++ keynoteSlotsThursday ++ conferenceSlotsThursday ++ quickiesSlotsThursday ++ bofSlotsThursday ++ labsSlotsThursday ++ tiaSlotsThursday
    }

    val friday: List[Slot] = {
      fridayBreaks ++ keynoteSlotsFriday ++ conferenceSlotsFriday ++ quickiesSlotsFriday ++ labsSlotsFriday
    }

    // COMPLETE DEVOXX
    def all: List[Slot] = {
      wednesday ++ thursday ++ friday
    }
  }

  def dateRange(from: DateTime, to: DateTime, step: Period): Iterator[DateTime]      =Iterator.iterate(from)(_.plus(step)).takeWhile(!_.isAfter(to))

  val fromDay = new DateTime().withYear(2017).withMonthOfYear(7).withDayOfMonth(11)
  val toDay = new DateTime().withYear(2017).withMonthOfYear(7).withDayOfMonth(15)

  // TODO You might want to start here and configure first, your various Conference Elements
  def current() = ConferenceDescriptor(
    eventCode = "TDC2017SP",
    // You will need to update conf/routes files with this code if modified
    confUrlCode = "tdc2017sp",
    frLangEnabled = false,
    fromEmail = Play.current.configuration.getString("mail.from").getOrElse("organizacao@thedevelopersconference.com.br"),
    committeeEmail = Play.current.configuration.getString("mail.committee.email").getOrElse("organizacao@thedevelopersconference.com.br"),
    bccEmail = Play.current.configuration.getString("mail.bcc"),
    bugReportRecipient = Play.current.configuration.getString("mail.bugreport.recipient").getOrElse("tdc@globalcode.com.br"),
    conferenceUrls = ConferenceUrls(
      faq = "http://cfp-sp.thedevconf.com.br/faq",
      registration = "http://thedevconf.com.br/tdc/2017/inscricoes",
      confWebsite = "http:/thedevconf.com.br",
      cfpHostname = Play.current.configuration.getString("cfp.hostname").getOrElse("cfp-sp.thedevconf.com.br")
    ),
    timing = ConferenceTiming(
      datesI18nKey = "18 a 22 de Julho de 2017",
      speakersPassDuration = 4,
      preferredDayEnabled = true,
      firstDayFr = "18 july",
      firstDayEn = "july 18th",
      datesFr = "du 18 au 22 july 2017",
      datesEn = "July 18th to 22th, 2017",
      cfpOpenedOn = DateTime.parse("2017-05-16T18:00:00-03:00"),
      cfpClosedOn = DateTime.parse("2017-05-30T23:59:59-03:00"),
      scheduleAnnouncedOn = DateTime.parse("2017-06-06T00:00:00-03:00"),
      days=dateRange(fromDay,toDay,new Period().withDays(1))
    ),
    hosterName = "AWS", hosterWebsite = "http://aws.amazon.com/",
    hashTag = "#TheDevConf",
    conferenceSponsor = ConferenceSponsor(showSponsorProposalCheckbox = true, sponsorProposalType = ConferenceProposalTypes.CONF)
    ,  List(new Locale("pt","BR"))
    , "AnhembiMorumbi, São Paulo, SP"
    , notifyProposalSubmitted = false // Do not send an email for each talk submitted for France
    , 700 // 1200 // French developers tends to be a bit verbose... we need extra space :-)
  )

  // It has to be a def, not a val, else it is not re-evaluated
  def isCFPOpen: Boolean = {
    Play.current.configuration.getBoolean("cfp.isOpen").getOrElse(false)
  }

  def isGoldenTicketActive:Boolean = Play.current.configuration.getBoolean("goldenTicket.active").getOrElse(false)

  def isFavoritesSystemActive:Boolean = Play.current.configuration.getBoolean("cfp.activateFavorites").getOrElse(false)

  def isHTTPSEnabled = Play.current.configuration.getBoolean("cfp.activateHTTPS").getOrElse(false)

}

