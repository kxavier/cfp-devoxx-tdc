/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2013 Association du Paris Java User Group.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package models

import library.{Dress, Redis}
import play.api.data.Form
import play.api.data.Forms._

/**
 * A track leader is the association between a user and a track.
 * A Track can have more than one track leader.
 * A webuser can lead more than one Track.
 * This simple use-case demonstrates how to implement a one-to-many relationship with Redis
 * Created by @nmartignole on 15/05/2014 for Devoxx BE.
 */


object TrackLeader {

  def conferenceId = ConferenceDescriptor.current().eventCode
  
  def assign(trackId: String, webuserId: String) {
    if (Webuser.hasAccessToCFP(webuserId)) {
      Redis.pool.withClient {
        client =>
          client.hset(s"TrackLeaders:${conferenceId}:", trackId, webuserId)
      }
    }
  }

  def unassign(trackId: String, webuserId: String) {
    Redis.pool.withClient {
      client =>
        client.hdel(s"TrackLeaders:${conferenceId}:", trackId)
    }
  }

  def isTrackLeader(trackId: String, webuserId: String): Boolean = Redis.pool.withClient {
    client =>
      client.sismember(s"TrackLeaders:${conferenceId}:${trackId}", webuserId)
  }

  /*
  * Returns all the UUIDs for the trackleaders of for the requested track
  */
  def findAll(trackId: String): Set[String] = Redis.pool.withClient {
    client =>
      client.smembers(s"TrackLeaders:${conferenceId}:${trackId}")
  }

  def updateAllTracks(mapsByTrack: Map[String, Seq[String]]) = Redis.pool.withClient{
    client=>
    val tx = client.multi()
    mapsByTrack.foreach {
      case (trackId, seqUUIDs) =>
        Redis.pool.withClient {
          client =>
            tx.del(s"TrackLeaders:${conferenceId}:${trackId}")
            seqUUIDs.filterNot(_ == "no_track_lead").foreach {
              uuid: String =>
                tx.sadd(s"TrackLeaders:${conferenceId}:${trackId}", uuid)
            }
        }
    }
    tx.exec()
  }

  def deleteWebuser(webuserUUID: String) = {
    Track.allIDs.foreach {
      trackId: String =>
        unassign(trackId, webuserUUID)
    }
  }

  /*
  * cleans the trackleaders for all the tracks
  */
  def resetAll():Unit = Redis.pool.withClient{ client =>
    val tracks = client.keys(s"TrackLeaders:${conferenceId}:*").toList
    if(!tracks.isEmpty) {
      client.del(tracks: _*)
    }
  }
}
