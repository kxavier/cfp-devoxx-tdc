package controllers

import library.search.{DoIndexProposal, _}
import library.{DraftReminder, Redis, ZapActor}
import models._
import org.joda.time.Instant
import play.api.Play
import play.api.cache.EhCachePlugin
import play.api.data.Forms._
import play.api.data._
import play.api.libs.json.Json
import play.api.mvc.Action
import play.api.i18n.Messages
import models.TrackArea._

/**
  * Backoffice actions, for maintenance and validation.
  *
  * Author: nicolas martignole
  * Created: 02/12/2013 21:34
  */
object Backoffice extends SecureCFPController {

  def homeBackoffice() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      Ok(views.html.Backoffice.homeBackoffice())
  }

  // Add or remove the specified user from "cfp" security group
  def switchCFPAdmin(uuidSpeaker: String) = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      Webuser.findByUUID(uuidSpeaker).filterNot(_.uuid == "bd894205a7d579351609f8dcbde49b9ffc0fae13").map {
        webuser =>
          if (Webuser.hasAccessToCFP(uuidSpeaker)) {
            Event.storeEvent(Event(uuidSpeaker, request.webuser.uuid, s"removed ${webuser.cleanName} from CFP group"))
            Webuser.removeFromCFPAdmin(uuidSpeaker)
          } else {
            Webuser.addToCFPAdmin(uuidSpeaker)
            Event.storeEvent(Event(uuidSpeaker, request.webuser.uuid, s"added ${webuser.cleanName} to CFP group"))
          }
          Redirect(routes.CFPAdmin.allWebusers())
      }.getOrElse {
        NotFound("Webuser not found")
      }
  }

  // Authenticate on CFP on behalf of specified user.
  def authenticateAs(uuidSpeaker: String) = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      // Block redirect if the uuidSpeaker belongs to the ADMIN group and not you
      if (Webuser.isMember(uuidSpeaker, "admin") && Webuser.isNotMember(request.webuser.uuid, "admin")) {
        Unauthorized("Sorry, only admin user can become admin.")
      } else {
        Redirect(routes.CallForPaper.homeForSpeaker()).withSession("uuid" -> uuidSpeaker)
      }
  }

  def authenticateAndCreateTalk(uuidSpeaker: String) = SecuredAction(IsMemberOf("cfp")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      if (Webuser.isMember(uuidSpeaker, "admin") && Webuser.isNotMember(request.webuser.uuid, "admin")) {
        Unauthorized("Sorry, only admin user can become admin.")
      } else {
        Redirect(routes.CallForPaper.newProposal()).withSession("uuid" -> uuidSpeaker)
      }
  }

  def allProposals(proposalId: Option[String]) = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>

      proposalId match {
        case Some(id) =>
          val proposal = Proposal.findById(id)
          proposal match {
            case None => NotFound("Proposal not found")
            case Some(pr) => Ok(views.html.Backoffice.allProposals(List(pr)))
          }
        case None =>
          val proposals = Proposal.allProposals().sortBy(_.state.code)
      		Ok(views.html.Backoffice.allProposals(proposals))
      }
  }

  // This endpoint is deliberately *not* secured in order to transform a user into an admin
  // only if there isn't any admin in the application
  def bootstrapAdminUser(uuid: String) = Action {
    implicit request =>
      if (Webuser.noBackofficeAdmin()) {
        Webuser.addToBackofficeAdmin(uuid)
        Webuser.addToCFPAdmin(uuid)
        Redirect(routes.Application.index()).flashing("success" -> "Your UUID has been configured as super-admin")
      } else {
        Redirect(routes.Application.index()).flashing("error" -> "There is already an Admin user")
      }
  }

  def clearCaches() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      Play.current.plugin[EhCachePlugin].foreach(p => p.manager.clearAll);
      Ok(views.html.Backoffice.homeBackoffice())
  }

  def changeProposalState(proposalId: String, state: String) = SecuredAction(IsMemberOf("cfp")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>

      val proposalOption = Proposal.findById(proposalId)

      //Removes the proposal from the Approved or Refused Collections, to avoid inconsistencies in the state.
      //Without this we can have proposals for example in an Approved state and in a Refused* collection, since this method
      //is accessed for a manual change of state for the Admins.
      //It always cleans the relevant collections for safety. The performance impact should not be great since this operation
      //is rarely used.
      //A declined proposal can appear or not in the approvals collection, so it should not cause any change in this cleaning process.
      if (!isApprovalState(state)  && state != ProposalState.DECLINED.code) {
        proposalOption.map( proposal =>
          ApprovedProposal.cancelApprove(proposal)
        )
      }
      if (!isRefusalState(state)) {
        proposalOption.map( proposal =>
          ApprovedProposal.cancelRefuse(proposal)
        )
      }

      Proposal.changeProposalState(request.webuser.uuid, proposalId, ProposalState.parse(state))
      if (isApprovalState(state)) {
        proposalOption.map {
          proposal =>
            ApprovedProposal.approve(proposal)
        }
      }
      if (isRefusalState(state)) {
        proposalOption.map {
          proposal =>
            ApprovedProposal.refuse(proposal)
        }
      }
      Redirect(routes.Backoffice.allProposals()).flashing("success" -> ("Changed state to " + state))
  }

  /*
   * method that allows future addition of other states that represent an approval state (Ex: ACCEPTED or APPROVED)
   *
   * An approval state is a state in which the proposal is saved in the Approved* collections
   */
  private def isApprovalState(state:String):Boolean = {
    return (state == ProposalState.ACCEPTED.code || state == ProposalState.APPROVED.code || state == ProposalState.CANCELED.code)
  }

  /*
   * method that allows future modification of states that represent a refusal state.
   *
   * A refusal state is a state in which the proposal is saved in the Refused* collections
   */
  private def isRefusalState(state:String):Boolean = {
    return (state == ProposalState.REJECTED.code)
  }

  val formSecu = Form("secu" -> nonEmptyText())

  def deleteSpeaker(speakerUUIDToDelete: String) = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      val uuid = request.webuser.uuid
      if (Webuser.isMember(speakerUUIDToDelete, "cfp") || Webuser.isMember(speakerUUIDToDelete, "admin")) {
        Redirect(routes.CFPAdmin.index()).flashing("error" -> s"We cannot delete CFP admin user...")
      } else {
        formSecu.bindFromRequest.fold(invalid => {
          Redirect(routes.CFPAdmin.index()).flashing("error" -> "You did not enter DEL... are you drunk?")
        }, _ => {
          Speaker.delete(speakerUUIDToDelete)
          Webuser.findByUUID(speakerUUIDToDelete).foreach {
            w =>
              Webuser.delete(w)
              Event.storeEvent(Event(speakerUUIDToDelete, uuid, s"Deleted webuser ${w.cleanName} ${w.uuid}"))
          }
          Redirect(routes.CFPAdmin.index()).flashing("success" -> s"Speaker $speakerUUIDToDelete deleted")
        })
      }
  }

  def doIndexElasticSearch() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      ElasticSearchActor.masterActor ! DoIndexAllSpeakers
      ElasticSearchActor.masterActor ! DoIndexAllProposals
      ElasticSearchActor.masterActor ! DoIndexAllAccepted
      ElasticSearchActor.masterActor ! DoIndexAllHitViews
      Redirect(routes.Backoffice.homeBackoffice).flashing("success" -> "Elastic search actor started...")
  }

  def doResetAndConfigureElasticSearch() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      ElasticSearchActor.masterActor ! DoCreateConfigureIndex
      Redirect(routes.Backoffice.homeBackoffice).flashing("success" -> "Deleted and now creating [speakers] and [proposals] indexes. Please force an indexer in one or two minuts.")
  }

  // If a user is not a member of cfp security group anymore, then we need to delete all its votes.
  def cleanUpVotesIfUserWasDeleted = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      Proposal.allProposalIDs.foreach {
        proposalID: String =>
          Review.allVotesFor(proposalID).foreach {
            case (reviewerUUID, _) => {
              if (Webuser.doesNotExist(reviewerUUID)) {
                play.Logger.of("application.Backoffice").debug(s"Deleting vote on $proposalID for user $reviewerUUID")
                Review.removeVoteForProposal(proposalID, reviewerUUID)
              }
            }
          }
      }
      Ok("Done")
  }

  def deleteVotesForPropal(proposalId: String) = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      Review.allVotesFor(proposalId).foreach {
        case (reviewerUUID, score) => {
          play.Logger.of("application.Backoffice").info(s"Deleting vote on $proposalId by $reviewerUUID of score $score")
          Review.deleteVoteForProposal(proposalId)
          ReviewByGoldenTicket.deleteVoteForProposal(proposalId)
        }
      }
      Redirect(routes.CFPAdmin.showVotesForProposal(proposalId))
  }

  def submittedByDate() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>

      Redis.pool.withClient {
        client =>
          val toReturn = client.hgetAll("Proposal:SubmittedDate").map {
            case (proposal, submitted) =>
              (proposal, new Instant(submitted.toLong).toDateTime.toDateMidnight.toString("dd-MM-yyyy"))
          }.groupBy(_._2).map {
            tuple =>
              (tuple._1, tuple._2.size)
          }.toList.sortBy(_._1).map {
            s =>
              s._1 + ", " + s._2 + "\n"
          }

          Ok("Date, total\n" + toReturn.mkString).as("text/plain")
      }
  }

  def sanityCheckSchedule() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      val publishedConf = ScheduleConfiguration.loadAllPublishedSlots().filter(_.proposal.isDefined)

      val declined = publishedConf.filter(_.proposal.get.state == ProposalState.DECLINED)

      val approved = publishedConf.filter(_.proposal.get.state == ProposalState.APPROVED)

      val accepted = publishedConf.filter(_.proposal.get.state == ProposalState.ACCEPTED)

      val allSpeakersIDs = publishedConf.flatMap(_.proposal.get.allSpeakerUUIDs).toSet
      val onlySpeakersThatNeedsToAcceptTerms: Set[String] = allSpeakersIDs.filter(uuid => Speaker.needsToAccept(uuid))
      val allSpeakers = Speaker.loadSpeakersFromSpeakerIDs(onlySpeakersThatNeedsToAcceptTerms)

      // Speaker declined talk AFTER it has been published
      val acceptedThenChangedToOtherState = accepted.filter {
        slot: Slot =>
          val proposal = slot.proposal.get
          Proposal.findProposalState(proposal.id) != Some(ProposalState.ACCEPTED)
      }

      Ok(views.html.Backoffice.sanityCheckSchedule(declined, approved, acceptedThenChangedToOtherState, allSpeakers))

  }

  def sanityCheckProposals() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      Redis.pool.withClient {
        client =>
          val toReturn = client.hgetAll("Proposals").map {
            case (proposalId, json) =>
              (proposalId, Json.parse(json).asOpt[Proposal])
          }.filter(_._2.isEmpty).map(_._1)
          Ok(views.html.Backoffice.sanityCheckProposals(toReturn))
      }
  }

  def fixToAccepted(slotId: String, proposalId: String, talkType: String) = SecuredAction(IsMemberOf("admin")) {
    implicit request =>
      val maybeUpdated = for (
        scheduleId <- ScheduleConfiguration.getPublishedSchedule(talkType);
        scheduleConf <- ScheduleConfiguration.loadScheduledConfiguration(scheduleId);
        slot <- scheduleConf.slots.find(_.id == slotId).filter(_.proposal.isDefined).filter(_.proposal.get.id == proposalId)
      ) yield {
        val updatedProposal = slot.proposal.get.copy(state = ProposalState.ACCEPTED)
        val updatedSlot = slot.copy(proposal = Some(updatedProposal))
        val newListOfSlots = updatedSlot :: scheduleConf.slots.filterNot(_.id == slotId)
        newListOfSlots
      }

      maybeUpdated.map {
        newListOfSlots =>
          val newID = ScheduleConfiguration.persist(talkType, newListOfSlots, request.webuser)
          ScheduleConfiguration.publishConf(newID, talkType)

          Redirect(routes.Backoffice.sanityCheckSchedule()).flashing("success" -> s"Created a new scheduleConfiguration ($newID) and published a new agenda.")
      }.getOrElse {
        NotFound("Unable to update Schedule configuration, did not find the slot, the proposal or the scheduleConfiguraiton")
      }
  }

  def sendDraftReminder = SecuredAction(IsMemberOf("admin")) {
    implicit request =>
      ZapActor.actor ! DraftReminder()
      Redirect(routes.Backoffice.homeBackoffice()).flashing("success" -> "Sent draft reminder to speakers")
  }

  def showAllDeclined() = SecuredAction(IsMemberOf("admin")) {
    implicit request =>

      val allDeclined = Proposal.allDeclinedProposals()
      //      Proposal.decline(request.webuser.uuid, proposalId)
      Ok(views.html.Backoffice.showAllDeclined(allDeclined))

  }
  
  /**
  * Shows the page that allows adding or removing users from the admin list
  */
  def allAdminUsers() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
	  val allWebusers = Webuser.allSpeakers.sortBy(_.cleanName)
      Ok(views.html.Backoffice.allAdminUsers(allWebusers))
  }
  
  /**
  * Add or remove the specified user from "admin" security group
  */
  def switchBackoffice(uuidSpeaker: String) = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      Webuser.findByUUID(uuidSpeaker).map {
        webuser =>
          var success = true
          if (Webuser.hasAccessToAdmin(uuidSpeaker)) {
            if(Webuser.countAdmins > 1) {
              Event.storeEvent(Event(uuidSpeaker, request.webuser.uuid, s"removed ${webuser.cleanName} from Admin group"))
              Webuser.removeFromBackofficeAdmin(uuidSpeaker)
            } else {
              success = false
            }
          } else {
            Webuser.addToBackofficeAdmin(uuidSpeaker)
            Event.storeEvent(Event(uuidSpeaker, request.webuser.uuid, s"added ${webuser.cleanName} to Admin group"))
          }
          if(success) {
            Redirect(routes.Backoffice.allAdminUsers())
          } else {
            Redirect(routes.Backoffice.allAdminUsers()).flashing("error" -> Messages("backoffice.alladminusers.remove.error"))
          }
      }.getOrElse {
        NotFound("Webuser not found")
      }
  }

  /**
   * lists all the track areas
   */
  def allTrackAreas() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      val allAreas = TrackArea.allAreas().toList.sortBy(_.description)
      Ok(views.html.Backoffice.showTrackAreas(allAreas))
  } 

  /**
   * Saves a track area
   */
  def updateTracksForAreas() = SecuredAction(IsMemberOf("admin")) {
    implicit req: SecuredRequest[play.api.mvc.AnyContent] =>
       req.request.body.asFormUrlEncoded.map {
        tracksByArea =>
          TrackArea.updateAllAreas(tracksByArea)
          Redirect(routes.Backoffice.allTrackAreas).flashing("success" -> Messages("backoffice.area.msg.updated"))
      }.getOrElse {
        Redirect(routes.Backoffice.allTrackAreas).flashing("error" -> "No value received")
      }
  } 

  /**
  * opens the track area form page
  */
  def newOrEditArea(areaId:Option[String]) = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      areaId match {
        case Some(id) => 
          val optionArea = TrackArea.load(id)
          optionArea.map{ area =>
            val form = areaForm.fill(area)
            Ok(views.html.Backoffice.editTrackArea(form))
          }.getOrElse(NotFound("Area not found").as("text/html"))	
        case None => Ok(views.html.Backoffice.editTrackArea(areaForm))
      }
  }

  /**
  * saves an area
  */
  def saveArea() = SecuredAction(IsMemberOf("admin")) {
    implicit request: SecuredRequest[play.api.mvc.AnyContent] =>
      areaForm.bindFromRequest.fold(
        invalidForm => BadRequest(views.html.Backoffice.editTrackArea(invalidForm)),
        trackArea => {
          TrackArea.save(trackArea)	
          Redirect(routes.Backoffice.allTrackAreas)
        }  
      )
  }  

  
}
