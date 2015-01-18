/** Copyright 2014 TappingStone, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package io.prediction.engines.base

import scala.collection.immutable.HashMap
import scala.collection.immutable.List

import org.apache.spark.rdd.RDD

/* this engine require following attributes */
case class AttributeNames(
  // entity types
  val user: String,
  val item: String,
  // event name of the u2i actions
  val u2iActions: Set[String],
  // properties
  val itypes: String,
  val starttime: String,
  val endtime: String,
  val inactive: String,
  val rating: String
)

/* Training Data */
class ItemTD(
  val iid: String,
  val itypes: Seq[String],
  val starttime: Option[Long] = None,
  val endtime: Option[Long] = None,
  val inactive: Boolean = false) extends Serializable {
    override def toString = s"${iid}"
  }

class UserTD(
  val uid: String
) extends Serializable {
  override def toString = s"${uid}"
}

class U2IActionTD(
  val uindex: Int,
  val iindex: Int,
  val action: String, // action name
  val v: Option[Int] = None,
  val t: Long // action time
) extends Serializable {
  override def toString = s"${uindex} ${iindex} ${action}"
}

class TrainingData(
    val users: HashMap[Int, UserTD], // uindex->uid
    val items: HashMap[Int, ItemTD], // iindex->itemTD
    val u2iActions: List[U2IActionTD]
  ) extends Serializable {
    override def toString = s"TrainingData:" +
      s"U: [${users.size}] (${users.take(2)}...) " +
      s"I: [${items.size}] (${items.take(2)}...) " +
      s"A: [${u2iActions.size}] (${u2iActions.take(2)}...)"
  }

// Parallel TrainingData
class PTrainingData(
  val users: RDD[(Int, UserTD)],
  val items: RDD[(Int, ItemTD)],
  val u2iActions: RDD[U2IActionTD]
) extends Serializable {
  override def toString = s"PTrainingData:" +
    s"U: [${users.count}] (${users.take(2).toList}...) " +
    s"I: [${items.count}] (${items.take(2).toList}...) " +
    s"A: [${u2iActions.count}] (${u2iActions.take(2).toList}...)"
}

class RatingTD(
  val uindex: Int,
  val iindex: Int,
  val rating: Int,
  val t: Long) extends Serializable {
    override def toString = s"RatingTD: ${uindex} ${iindex} ${rating}"
  }

class PreparedData(
  val users: Map[Int, UserTD],
  val items: Map[Int, ItemTD],
  val rating: Seq[RatingTD],
  val ratingOriginal: Seq[RatingTD], // Non-deduped ratings
  val seenU2IActions: Option[Seq[U2IActionTD]] // actions for unseen filtering
) extends Serializable {
  override def toString = s"U: ${users.take(2)}..." +
   s" I: ${items.take(2)}... R: ${rating.take(2)}..."
}

// Parallel PreparedData
class PPreparedData(
  val users: RDD[(Int, UserTD)],
  val items: RDD[(Int, ItemTD)],
  val rating: RDD[RatingTD],
  val ratingOriginal: RDD[RatingTD], // Non-deduped ratings
  val seenU2IActions: Option[RDD[U2IActionTD]] // actions for unseen filtering
) extends Serializable { }
