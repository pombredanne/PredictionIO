package org.template.similarproduct

import io.prediction.controller.PDataSource
import io.prediction.controller.EmptyEvaluationInfo
import io.prediction.controller.EmptyActualResult
import io.prediction.controller.Params
import io.prediction.data.storage.Event
import io.prediction.data.storage.Storage

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import grizzled.slf4j.Logger

case class DataSourceParams(val appId: Int) extends Params

class DataSource(val dsp: DataSourceParams)
  extends PDataSource[TrainingData,
      EmptyEvaluationInfo, Query, EmptyActualResult] {

  @transient lazy val logger = Logger[this.type]

  override
  def readTraining(sc: SparkContext): TrainingData = {
    val eventsDb = Storage.getPEvents()

    // create a RDD of (entityID, User)
    val usersRDD: RDD[(String, User)] = eventsDb.aggregateProperties(
      appId = dsp.appId,
      entityType = "user"
    )(sc).map { case (entityId, properties) =>
      val user = try {
        User()
      } catch {
        case e: Exception => {
          logger.error(s"Failed to get properties ${properties} of" +
            s" user ${entityId}. Exception: ${e}.")
          throw e
        }
      }
      (entityId, user)
    }

    // create a RDD of (entityID, Item)
    val itemsRDD: RDD[(String, Item)] = eventsDb.aggregateProperties(
      appId = dsp.appId,
      entityType = "item"
    )(sc).map { case (entityId, properties) =>
      val item = try {
        // Assume categories is optional property of item.
        Item(categories = properties.getOpt[List[String]]("categories"))
      } catch {
        case e: Exception => {
          logger.error(s"Failed to get properties ${properties} of" +
            s" item ${entityId}. Exception: ${e}.")
          throw e
        }
      }
      (entityId, item)
    }

    // get all "user" "view" "item" events
    val viewEventsRDD: RDD[ViewEvent] = eventsDb.find(
      appId = dsp.appId,
      entityType = Some("user"),
      eventNames = Some(List("view")),
      // targetEntityType is optional field of an event.
      targetEntityType = Some(Some("item")))(sc)
      // eventsDb.find() returns RDD[Event]
      .map { event =>
        val viewEvent = try {
          event.event match {
            case "view" => ViewEvent(
              user = event.entityId,
              item = event.targetEntityId.get,
              t = event.eventTime.getMillis)
            case _ => throw new Exception(s"Unexpected event ${event} is read.")
          }
        } catch {
          case e: Exception => {
            logger.error(s"Cannot convert ${event} to ViewEvent." +
              s" Exception: ${e}.")
            throw e
          }
        }
        viewEvent
      }

    // ADDED
    // get all "user" "like" and "dislike" "item" events
    val likeEventsRDD: RDD[LikeEvent] = eventsDb.find(
      appId = dsp.appId,
      entityType = Some("user"),
      eventNames = Some(List("like", "dislike")),
      // targetEntityType is optional field of an event.
      targetEntityType = Some(Some("item")))(sc)
      // eventsDb.find() returns RDD[Event]
      .map { event =>
        val likeEvent = try {
          event.event match {
            case "like" | "dislike" => LikeEvent(
              user = event.entityId,
              item = event.targetEntityId.get,
              t = event.eventTime.getMillis,
              like = (event.event == "like"))
            case _ => throw new Exception(s"Unexpected event ${event} is read.")
          }
        } catch {
          case e: Exception => {
            logger.error(s"Cannot convert ${event} to LikeEvent." +
              s" Exception: ${e}.")
            throw e
          }
        }
        likeEvent
      }

    new TrainingData(
      users = usersRDD,
      items = itemsRDD,
      viewEvents = viewEventsRDD,
      likeEvents = likeEventsRDD // ADDED
    )
  }
}

case class User()

case class Item(val categories: Option[List[String]])

case class ViewEvent(val user: String, val item: String, val t: Long)

case class LikeEvent( // ADDED
  val user: String,
  val item: String,
  val t: Long,
  val like: Boolean // true: like. false: dislike
)

class TrainingData(
  val users: RDD[(String, User)],
  val items: RDD[(String, Item)],
  val viewEvents: RDD[ViewEvent],
  val likeEvents: RDD[LikeEvent] // ADDED
) extends Serializable {
  override def toString = {
    s"users: [${users.count()} (${users.take(2).toList}...)]" +
    s"items: [${items.count()} (${items.take(2).toList}...)]" +
    s"viewEvents: [${viewEvents.count()}] (${viewEvents.take(2).toList}...)" +
    // ADDED
    s"likeEvents: [${likeEvents.count()}] (${likeEvents.take(2).toList}...)"
  }
}
